//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
import com.monke.monkeybook.model.analyzeRule.assit.SimpleJavaExecutor;
import com.monke.monkeybook.model.analyzeRule.assit.SimpleJavaExecutorImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.MD5Utils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.script.SimpleBindings;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class FindBookPresenterImpl extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {

    private static final int THREADS_NUM = 8;

    private CompositeDisposable mDisposableMgr;
    private Disposable mUpdateDispose;

    private final Scheduler mScheduler = RxExecutors.newScheduler(THREADS_NUM);

    private SimpleJavaExecutor mJavaExecutor;

    private FindGroupIterator mGroupIterator;

    private boolean mShowAllFind;

    private final Function<List<SearchBookBean>, List<SearchBookBean>> mBookFilter = searchBookBeans -> {
        ListUtils.removeDuplicate(searchBookBeans);
        return searchBookBeans;
    };

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        super.detachView();
        RxBus.get().unregister(this);
        if (mDisposableMgr != null) {
            mDisposableMgr.dispose();
            mDisposableMgr = null;
        }

        if (mUpdateDispose != null) {
            mUpdateDispose.dispose();
            mUpdateDispose = null;
        }

        mScheduler.shutdown();
    }

    @Override
    public void initData() {
        mShowAllFind = AppConfigHelper.get().getBoolean(mView.getContext().getString(R.string.pk_show_all_find), true);

        resetDispose();
        Observable.create((ObservableOnSubscribe<List<FindKindGroupBean>>) e -> {
            List<FindKindGroupBean> groupList = obtainFindGroupList();
            e.onNext(groupList);
            e.onComplete();
        })
                .subscribeOn(mScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(findKindGroupBeans -> {
                    mView.updateUI(findKindGroupBeans);
                    mView.hideProgress();
                })
                .observeOn(mScheduler)
                .flatMap(groupBeans -> Observable.fromIterable(groupBeans)
                        .observeOn(mScheduler)
                        .map(groupBean -> {
                            groupBean.setBooks(getFromBookCache(groupBean.getTag()));
                            return groupBean;
                        }).observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(groupBean -> mView.updateItem(groupBean))
                        .toList()
                        .toObservable())
                .subscribe(new SimpleObserver<List<FindKindGroupBean>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposableMgr.add(d);
                    }

                    @Override
                    public void onNext(List<FindKindGroupBean> groupBeans) {
                        startFindBooks(groupBeans);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideProgress();
                    }
                });
    }

    @Override
    public void updateData(String url) {
        if (StringUtils.isBlank(url)) return;
        if (mUpdateDispose != null) {
            mUpdateDispose.dispose();
        }
        Observable.just(url)
                .subscribeOn(mScheduler)
                .flatMap(s -> {
                    BookSourceBean sourceBean = BookSourceManager.getByUrl(s);
                    return Observable.just(sourceBean);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(sourceBean -> {
                    FindKindGroupBean groupBean = new FindKindGroupBean();
                    groupBean.setGroupName(sourceBean.getBookSourceName());
                    groupBean.setTag(sourceBean.getBookSourceUrl());
                    mView.updateItem(groupBean);
                })
                .observeOn(mScheduler)
                .flatMap(sourceBean -> {
                    FindKindGroupBean groupBean = getFromBookSource(sourceBean);
                    if (groupBean == null) {
                        return Observable.error(new Exception("can not get FindKindGroupBean from: " + sourceBean.getBookSourceUrl()));
                    }
                    return Observable.just(groupBean);
                })
                .flatMap(findKindGroupBean -> {
                    FindKindBean kindBean = findKindGroupBean.getChildren().get(0);
                    return WebBookModel.getInstance().findBook(kindBean.getTag(), kindBean.getKindUrl(), 1)
                            .subscribeOn(mScheduler)
                            .timeout(30, TimeUnit.SECONDS)
                            .map(mBookFilter)
                            .flatMap(searchBookBeans -> {
                                findKindGroupBean.setBooks(searchBookBeans);
                                return Observable.just(findKindGroupBean);
                            }).onErrorResumeNext(throwable -> {
                                if (throwable instanceof IOException || throwable instanceof TimeoutException) {
                                    return Observable.error(throwable);
                                }
                                return Observable.just(findKindGroupBean);
                            });
                })
                .doOnNext(this::putBookCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<FindKindGroupBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mUpdateDispose = d;
                    }

                    @Override
                    public void onNext(FindKindGroupBean findKindGroupBean) {
                        if (isFindInvalid(findKindGroupBean)) {
                            mView.removeItem(findKindGroupBean);
                        } else {
                            mView.updateItem(findKindGroupBean);
                        }
                    }
                });
    }


    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.SOURCE_LIST_CHANGE), @Tag(RxBusTag.FIND_LIST_CHANGE)})
    public void updateBookShelf(Boolean change) {
        initData();
    }


    private void startFindBooks(List<FindKindGroupBean> groupBeans) {
        if (groupBeans == null || groupBeans.isEmpty()) return;
        mGroupIterator = new FindGroupIterator(groupBeans);

        resetDispose();

        for (int i = 0, size = Math.min(THREADS_NUM, groupBeans.size()); i < size; i++) {
            findBooks();
        }
    }

    private void findBooks() {
        if (mGroupIterator == null || !mGroupIterator.hasNext()) return;
        final FindKindGroupBean kindGroupBean = mGroupIterator.next();
        Observable.just(kindGroupBean)
                .flatMap(findKindGroupBean -> {
                    if (findKindGroupBean.getBooks() != null && !findKindGroupBean.getBooks().isEmpty()) {
                        return Observable.error(new Exception("cached"));
                    }
                    FindKindBean kindBean = findKindGroupBean.getChildren().get(0);
                    return WebBookModel.getInstance().findBook(kindBean.getTag(), kindBean.getKindUrl(), 1)
                            .subscribeOn(mScheduler)
                            .timeout(30, TimeUnit.SECONDS)
                            .map(mBookFilter)
                            .flatMap(searchBookBeans -> {
                                findKindGroupBean.setBooks(searchBookBeans);
                                return Observable.just(findKindGroupBean);
                            })
                            .onErrorResumeNext(throwable -> {
                                if (throwable instanceof TimeoutException) {
                                    return Observable.error(throwable);
                                }
                                return Observable.just(findKindGroupBean);
                            })
                            .doOnNext(groupBean -> {
                                if (isFindInvalid(groupBean)) {
                                    BookSourceBean sourceBean = BookSourceManager.getByUrl(groupBean.getTag());
                                    sourceBean.setEnableFind(false);
                                    BookSourceManager.save(sourceBean);
                                }
                            });
                })
                .doOnNext(this::putBookCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<FindKindGroupBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposableMgr.add(d);
                    }

                    @Override
                    public void onNext(FindKindGroupBean value) {
                        if (isFindInvalid(value)) {
                            mView.removeItem(value);
                        } else {
                            mView.updateItem(value);
                        }

                        findBooks();
                    }

                    @Override
                    public void onError(Throwable e) {
                        findBooks();
                    }
                });
    }

    private void resetDispose() {
        if (mDisposableMgr != null) {
            mDisposableMgr.dispose();
        }
        mDisposableMgr = new CompositeDisposable();
    }

    private SimpleJavaExecutor getJavaExecutor() {
        if (mJavaExecutor == null) {
            mJavaExecutor = new SimpleJavaExecutorImpl();
        }
        return mJavaExecutor;
    }

    private boolean isFindInvalid(FindKindGroupBean groupBean) {
        if (mShowAllFind) {
            return false;
        }
        return groupBean == null || groupBean.getBooks() == null || groupBean.getBooks().isEmpty();
    }

    private String evalFindJs(String url, String js) {
        SimpleBindings bindings = new SimpleBindings() {{
            this.put("baseUrl", url);
            this.put("java", getJavaExecutor());
        }};
        String findRule = String.valueOf(Assistant.evalObjectScript(js, bindings));
        putJsCache(url, findRule);
        return findRule;
    }


    private void putJsCache(String url, String findRule) {
        ACache.get(mView.getContext()).put(MD5Utils.strToMd5By16(url), findRule);
    }

    private String getFromJsCache(String url) {
        return ACache.get(mView.getContext()).getAsString(MD5Utils.strToMd5By16(url));
    }

    private void putBookCache(FindKindGroupBean groupBean) {
        if (groupBean.getBooks() == null || groupBean.getBooks().isEmpty()) {
            return;
        }
        try {
            String json = Assistant.toJson(groupBean.getBooks());
            ACache.get(mView.getContext()).put(groupBean.getTag(), json, ACache.TIME_DAY);
        } catch (Exception ignore) {
        }
    }

    private List<SearchBookBean> getFromBookCache(String url) {
        try {
            String json = ACache.get(mView.getContext()).getAsString(url);
            return Assistant.fromJson(json, new TypeToken<List<SearchBookBean>>() {
            }.getType());
        } catch (Exception ignore) {
        }
        return null;
    }

    private List<FindKindGroupBean> obtainFindGroupList() {
        final List<BookSourceBean> bookSourceBeans;
        if (mShowAllFind) {
            bookSourceBeans = BookSourceManager.getAll();
        } else {
            bookSourceBeans = BookSourceManager.getFindEnabled();
        }
        final List<FindKindGroupBean> group = new ArrayList<>();
        for (BookSourceBean sourceBean : bookSourceBeans) {

            FindKindGroupBean groupBean = getFromBookSource(sourceBean);
            if (groupBean != null) {
                group.add(groupBean);
            }

        }
        return group;
    }

    private FindKindGroupBean getFromBookSource(BookSourceBean sourceBean) {
        String findRule = sourceBean.getRuleFindUrl();
        if (StringUtils.isBlank(findRule)) {
            return null;
        }
        try {
            boolean isJavaScript = StringUtils.startWithIgnoreCase(sourceBean.getRuleFindUrl(), "<js>");
            if (isJavaScript) {
                String cacheRule = getFromJsCache(sourceBean.getBookSourceUrl());
                if (cacheRule != null) {
                    findRule = cacheRule;
                } else {
                    findRule = evalFindJs(sourceBean.getBookSourceUrl(), findRule.substring(4, sourceBean.getRuleFindUrl().lastIndexOf("<")));
                }
            }

            if (findRule != null) {
                String[] kindA = findRule.split("(&&|\n)+");
                List<FindKindBean> children = new ArrayList<>();
                for (String kindB : kindA) {
                    if (kindB.trim().isEmpty()) continue;
                    String[] kind = kindB.split("::");
                    FindKindBean findKindBean = new FindKindBean();
                    findKindBean.setGroup(sourceBean.getBookSourceName());
                    findKindBean.setTag(sourceBean.getBookSourceUrl());
                    findKindBean.setKindName(kind[0]);
                    findKindBean.setKindUrl(kind[1]);
                    children.add(findKindBean);
                }
                FindKindGroupBean groupBean = new FindKindGroupBean();
                groupBean.setGroupName(sourceBean.getBookSourceName());
                groupBean.setTag(sourceBean.getBookSourceUrl());
                groupBean.setChildrenCount(children.size());
                groupBean.setChildren(children);
                return groupBean;
            }
        } catch (Exception ignore) {
            sourceBean.setBookSourceGroup("发现规则语法错误");
            BookSourceManager.save(sourceBean);
        }
        return null;
    }

    private class FindGroupIterator implements Iterator<FindKindGroupBean> {

        final List<FindKindGroupBean> groupBeans;
        final int limit;
        int cursor;

        FindGroupIterator(List<FindKindGroupBean> bookShelfBeans) {
            this.groupBeans = bookShelfBeans;
            this.limit = bookShelfBeans == null ? 0 : bookShelfBeans.size();
            this.cursor = 0;
        }

        @Override
        public synchronized boolean hasNext() {
            if (limit == 0) {
                return false;
            }
            return cursor < limit;
        }

        @Override
        public synchronized FindKindGroupBean next() {
            int i = cursor;
            if (i >= limit)
                return null;
            cursor = i + 1;
            return groupBeans.get(i);
        }

    }
}