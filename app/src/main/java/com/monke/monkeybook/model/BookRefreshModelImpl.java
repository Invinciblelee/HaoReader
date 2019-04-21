package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.impl.IBookRefreshModel;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.RxUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookRefreshModelImpl implements IBookRefreshModel {

    private static final int THREADS_NUM = 4;

    private List<BookShelfBean> bookShelfBeans;
    private List<String> errBooks = new ArrayList<>();
    private CompositeDisposable refreshingDisps = new CompositeDisposable();
    private RefreshingIterator refreshingIterator;

    private AtomicInteger loadingCount = new AtomicInteger();

    private OnBookRefreshListener refreshListener;

    private BookRefreshModelImpl() {
    }

    public static BookRefreshModelImpl newInstance() {
        return new BookRefreshModelImpl();
    }

    public void setOnBookRefreshListener(OnBookRefreshListener listener) {
        this.refreshListener = listener;
    }

    @Override
    public void queryBooks(int group, boolean refresh) {
        Single.create((SingleOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfBeans = BookshelfHelp.queryBooksByGroup(group);
            e.onSuccess(bookShelfBeans == null ? new ArrayList<>() : bookShelfBeans);
        }).compose(RxUtils::toSimpleSingle)
                .map(ensureNotLoading())
                .subscribe(new SingleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<BookShelfBean> value) {
                        dispatchResultEvent(value);
                        if (refresh) {
                            if (!NetworkUtil.isNetworkAvailable()) {
                                dispatchErrorEvent("无网络，请打开网络后再试");
                            } else {
                                bookShelfBeans = value;
                                startRefreshBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dispatchErrorEvent(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }


    @Override
    public void startRefreshBook() {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            errBooks.clear();
            refreshingDisps.clear();

            refreshingIterator = new RefreshingIterator(bookShelfBeans);

            for (int i = 0, size = Math.min(THREADS_NUM, bookShelfBeans.size()); i < size; i++) {
                newRefreshTask();
            }
        }
    }

    @Override
    public void stopRefreshBook() {
        refreshingDisps.dispose();
        refreshingDisps = null;
    }

    private Function<List<BookShelfBean>, List<BookShelfBean>> ensureNotLoading() {
        return bookShelfBeans -> {
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (bookShelfBean.isLoading()) {
                    bookShelfBean.setLoading(false);
                }
            }
            return bookShelfBeans;
        };
    }

    private void newRefreshTask() {
        BookShelfBean bookShelfBean = refreshingIterator.next();
        if (bookShelfBean != null) {
            if (bookShelfBean.getUpdateOff()) {
                refreshingIterator.moveToNext();
                if (refreshingIterator.hasNext()) {
                    newRefreshTask();
                } else {
                    dispatchFinishEvent();
                }
            } else {
                bookShelfBean.setLoading(true);
                dispatchRefreshEvent(bookShelfBean);
                refreshBookShelf(bookShelfBean);
            }
        }
    }

    private void refreshBookShelf(BookShelfBean bookShelfBean) {
        WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                .subscribeOn(Schedulers.io())
                .flatMap(this::saveBookToShelfO)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        loadingCount.incrementAndGet();
                        refreshingDisps.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean value) {
                        whenRefreshNext(bookShelfBean, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        whenRefreshNext(bookShelfBean, true);
                    }
                });
    }

    private void whenRefreshNext(BookShelfBean bookShelfBean, boolean error) {
        bookShelfBean.setLoading(false);
        dispatchRefreshEvent(bookShelfBean);
        if (error) {
            errBooks.add(bookShelfBean.getBookInfoBean().getName());
        }

        if (loadingCount.decrementAndGet() == 0) {
            if (errBooks.size() > 0) {
                dispatchErrorEvent(TextUtils.join("、", errBooks) + " 更新失败！");
                errBooks.clear();
            }
            dispatchFinishEvent();
        } else {
            newRefreshTask();
        }
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {//移出了书架
                BookshelfHelp.saveBookToShelf(bookShelfBean);
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private void dispatchResultEvent(List<BookShelfBean> bookShelfBeans) {
        if (refreshListener != null) {
            refreshListener.onResult(bookShelfBeans);
        }
    }

    private void dispatchRefreshEvent(BookShelfBean bookShelfBean) {
        if (refreshListener != null) {
            refreshListener.onRefresh(bookShelfBean);
        }
    }

    private void dispatchErrorEvent(String msg) {
        if (refreshListener != null) {
            refreshListener.onError(msg);
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

        void moveToNext() {
            if (cursor < limit) {
                cursor++;
            }
        }

    }

    public interface OnBookRefreshListener {
        void onResult(List<BookShelfBean> bookShelfBeans);

        void onError(String msg);

        void onRefresh(BookShelfBean bookShelfBean);

        void onRefreshFinish();
    }
}
