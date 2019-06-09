package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.model.impl.IBookRefreshModel;
import com.monke.monkeybook.utils.NetworkUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookRefreshModelImpl implements IBookRefreshModel {

    private static final int THREADS_NUM = 6;

    private List<BookShelfBean> bookShelfBeans;
    private List<String> errBooks = new ArrayList<>();
    private CompositeDisposable refreshingDisps = new CompositeDisposable();
    private RefreshingIterator refreshingIterator;

    private final Scheduler scheduler;
    private AtomicInteger loadingCount = new AtomicInteger();

    private OnBookRefreshListener refreshListener;

    private BookRefreshModelImpl() {
        scheduler = Schedulers.from(Executors.newFixedThreadPool(THREADS_NUM));
    }

    public static BookRefreshModelImpl newInstance() {
        return new BookRefreshModelImpl();
    }

    public void setOnBookRefreshListener(OnBookRefreshListener listener) {
        this.refreshListener = listener;
    }

    @Override
    public void queryBooks(int group, boolean refresh, boolean autoClean) {
        Single.create((SingleOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfBeans = BookshelfHelp.queryBooksByGroup(group);
            e.onSuccess(bookShelfBeans == null ? new ArrayList<>() : bookShelfBeans);
        }).subscribeOn(Schedulers.single())
                .flatMap((Function<List<BookShelfBean>, SingleSource<List<BookShelfBean>>>) bookShelfBeans -> {
                    if (group == Constant.GROUP_BENDI && autoClean) {
                        return Single.create((SingleOnSubscribe<List<BookShelfBean>>) emitter -> {
                            final List<BookShelfBean> remove = new ArrayList<>();
                            if (bookShelfBeans != null) {
                                for (BookShelfBean bookShelfBean : bookShelfBeans) {
                                    if (bookShelfBean.isLocalBook()) {
                                        File file = new File(bookShelfBean.getNoteUrl());
                                        if (!file.exists()) {
                                            BookshelfHelp.removeFromBookShelf(bookShelfBean);
                                            remove.add(bookShelfBean);
                                        }
                                    }
                                }
                            }
                            emitter.onSuccess(remove);
                        }).observeOn(AndroidSchedulers.mainThread())
                                .onErrorReturnItem(Collections.emptyList())
                                .map(remove -> {
                                    if (!remove.isEmpty()) {
                                        bookShelfBeans.removeAll(remove);
                                        dispatchMessageEvent("发现" + remove.size() + "本失效书籍，已自动清理");
                                    }
                                    return bookShelfBeans;
                                });
                    } else {
                        return Single.just(bookShelfBeans);
                    }
                })
                .map(ensureNotLoading())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<BookShelfBean> value) {
                        dispatchResultEvent(value);
                        if (refresh && group != Constant.GROUP_BENDI) {
                            if (!NetworkUtil.isNetworkAvailable()) {
                                dispatchMessageEvent("无网络，请打开网络后再试");
                            } else {
                                bookShelfBeans = value;
                                startRefreshBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dispatchResultEvent(Collections.emptyList());
                    }
                });
    }


    @Override
    public void startRefreshBook() {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            errBooks.clear();
            refreshingDisps.clear();

            refreshingIterator = new RefreshingIterator(new Vector<>(bookShelfBeans));

            for (int i = 0, size = Math.min(THREADS_NUM, bookShelfBeans.size()); i < size; i++) {
                newRefreshTask(i);
            }
        }
    }

    @Override
    public void stopRefreshBook() {
        if (refreshingDisps != null) {
            refreshingDisps.dispose();
            refreshingDisps = null;
        }
        scheduler.shutdown();
    }

    private Function<List<BookShelfBean>, List<BookShelfBean>> ensureNotLoading() {
        return bookShelfBeans -> {
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                bookShelfBean.withFlag(false);
            }
            return bookShelfBeans;
        };
    }

    private void newRefreshTask(int index) {
        final BookShelfBean bookShelfBean = refreshingIterator.next();
        if (bookShelfBean == null) {
            if (refreshingIterator.hasNext()) {
                newRefreshTask(index);
            } else if (loadingCount.get() == 0) {
                finishRefresh();
            }
            return;
        }

        if (bookShelfBean.getUpdateOff()) {
            if (refreshingIterator.hasNext()) {
                newRefreshTask(index);
            } else if (loadingCount.get() == 0) {
                dispatchFinishEvent();
            }
        } else {
            dispatchRefreshEvent(bookShelfBean, true);
            refreshBookShelf(index, bookShelfBean);
        }
    }

    private void refreshBookShelf(int index, BookShelfBean bookShelfBean) {
        WebBookModel.getInstance().getChapterList(bookShelfBean)
                .subscribeOn(scheduler)
                .timeout(1, TimeUnit.MINUTES)
                .flatMap(this::saveBookToShelfO)
                .delay(index * 100L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        loadingCount.incrementAndGet();
                        refreshingDisps.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean value) {
                        whenRefreshNext(index, bookShelfBean, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        whenRefreshNext(index, bookShelfBean, true);
                    }
                });
    }

    private void whenRefreshNext(int index, BookShelfBean bookShelfBean, boolean error) {
        dispatchRefreshEvent(bookShelfBean, false);
        if (error) {
            errBooks.add(bookShelfBean.getBookInfoBean().getName());
        }

        if (loadingCount.decrementAndGet() == 0 && !refreshingIterator.hasNext()) {
            finishRefresh();
        } else {
            newRefreshTask(index);
        }
    }

    private void finishRefresh() {
        if (errBooks.size() > 0) {
            dispatchMessageEvent(TextUtils.join("、", errBooks) + " 更新失败");
            errBooks.clear();
        }
        dispatchFinishEvent();
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {//移出了书架
                BookshelfHelp.saveBookToShelf(bookShelfBean);
            }
            bookShelfBean.setChapterList(null, false);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private void dispatchResultEvent(List<BookShelfBean> bookShelfBeans) {
        if (refreshListener != null) {
            refreshListener.onResult(bookShelfBeans);
        }
    }

    private void dispatchRefreshEvent(BookShelfBean bookShelfBean, boolean loading) {
        if (refreshListener != null) {
            refreshListener.onRefresh(bookShelfBean.withFlag(loading));
        }
    }

    private void dispatchMessageEvent(String msg) {
        if (refreshListener != null) {
            refreshListener.onMessage(msg);
        }
    }

    private void dispatchFinishEvent() {
        if (refreshListener != null) {
            refreshListener.onRefreshFinish();
        }
    }

    private class RefreshingIterator implements Iterator<BookShelfBean> {

        final List<BookShelfBean> bookShelfBeans;
        final int limit;
        int cursor;

        RefreshingIterator(List<BookShelfBean> bookShelfBeans) {
            this.bookShelfBeans = bookShelfBeans;
            this.limit = bookShelfBeans == null ? 0 : bookShelfBeans.size();
            this.cursor = 0;
        }

        @Override
        public boolean hasNext() {
            if (limit == 0) {
                return false;
            }
            return cursor < limit;
        }

        @Override
        public BookShelfBean next() {
            int i = cursor;
            if (i >= limit)
                return null;
            cursor = i + 1;
            return bookShelfBeans.get(i);
        }

    }

    public interface OnBookRefreshListener {
        void onResult(List<BookShelfBean> bookShelfBeans);

        void onMessage(String msg);

        void onRefresh(BookShelfBean bookShelfBean);

        void onRefreshFinish();
    }
}
