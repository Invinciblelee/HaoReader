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
import com.monke.monkeybook.utils.MD5Utils;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.script.SimpleBindings;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FindBookPresenterImpl extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {

    private static final int THREADS_NUM = 8;

    private CompositeDisposable mDisposableMgr;
    private Disposable mUpdateDispose;

    private ExecutorService mExecutor;

    private SimpleJavaExecutor mJavaExecutor;

    private FindGroupIterator mGroupIterator;

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }

        if (mDisposableMgr != null) {
            mDisposableMgr.dispose();
            mDisposableMgr = null;
        }

        if (mUpdateDispose != null) {
            mUpdateDispose.dispose();
            mUpdateDispose = null;
        }
    }

    @Override
    public void initData() {
        resetDispose();
        Observable.create((ObservableOnSubscribe<List<FindKindGroupBean>>) e -> {
            List<FindKindGroupBean> groupList = obtainFindGroupList();
            e.onNext(groupList);
            e.onComplete();
        })
                .subscribeOn(getScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(findKindGroupBeans -> {
                    mView.updateUI(findKindGroupBeans);
                    mView.hideProgress();
                })
                .observeOn(getScheduler())
                .flatMap(groupBeans -> Observable.fromIterable(groupBeans)
                        .observeOn(getScheduler())
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
                .subscribeOn(Schedulers.single())
                .flatMap(s -> {
                    BookSourceBean sourceBean = BookSourceManager.getByUrl(s);
                    FindKindGroupBean groupBean = getFromBookSource(sourceBean);
                    if (groupBean == null) {
                        return Observable.error(new Exception("can not get FindKindGroupBean from: " + s));
                    }
                    return Observable.just(groupBean);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(groupBean -> mView.updateItem(groupBean))
                .flatMap(findKindGroupBean -> {
                    FindKindBean kindBean = findKindGroupBean.getChildren().get(0);
                    return WebBookModel.getInstance().findBook(kindBean.getTag(), kindBean.getKindUrl(), 1)
                            .subscribeOn(getScheduler())
                            .timeout(30, TimeUnit.SECONDS)
                            .flatMap(searchBookBeans -> {
                                findKindGroupBean.setBooks(searchBookBeans);
                                return Observable.just(findKindGroupBean);
                            });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<FindKindGroupBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mUpdateDispose = d;
                    }

                    @Override
                    public void onNext(FindKindGroupBean findKindGroupBean) {
                        mView.updateItem(findKindGroupBean);
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
        Observable.just(mGroupIterator.next())
                .flatMap(findKindGroupBean -> {
                    if (findKindGroupBean.getBooks() != null && !findKindGroupBean.getBooks().isEmpty()) {
                        return Observable.error(new Exception("cached"));
                    }
                    FindKindBean kindBean = findKindGroupBean.getChildren().get(0);
                    return WebBookModel.getInstance().findBook(kindBean.getTag(), kindBean.getKindUrl(), 1)
                            .subscribeOn(getScheduler())
                            .timeout(30, TimeUnit.SECONDS)
                            .flatMap(searchBookBeans -> {
                                findKindGroupBean.setBooks(searchBookBeans);
                                return Observable.just(findKindGroupBean);
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
                        mView.updateItem(value);
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

    private String evalFindJs(String url, String js) {
        SimpleBindings bindings = new SimpleBindings() {{
            this.put("baseUrl", url);
            this.put("java", getJavaExecutor());
        }};
        String findRule = String.valueOf(Assistant.evalObjectScript(js, bindings));
        putJsCache(url, findRule);
        return findRule;
    }

    private Scheduler getScheduler() {
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newFixedThreadPool(THREADS_NUM);
        }
        return Schedulers.from(mExecutor);
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
            String json = Assistant.GSON.toJson(groupBean.getBooks());
            ACache.get(mView.getContext()).put(groupBean.getTag(), json, ACache.TIME_DAY);
        } catch (Exception ignore) {
        }
    }

    private List<SearchBookBean> getFromBookCache(String url) {
        try {
            String json = ACache.get(mView.getContext()).getAsString(url);
            return Assistant.GSON.fromJson(json, new TypeToken<List<SearchBookBean>>() {
            }.getType());
        } catch (Exception ignore) {
        }
        return null;
    }

    private List<FindKindGroupBean> obtainFindGroupList() {
        List<BookSourceBean> bookSourceBeans;
        if (AppConfigHelper.get().getBoolean(mView.getContext().getString(R.string.pk_show_all_find), true)) {
            bookSourceBeans = BookSourceManager.getAll();
        } else {
            bookSourceBeans = BookSourceManager.getEnabled();
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