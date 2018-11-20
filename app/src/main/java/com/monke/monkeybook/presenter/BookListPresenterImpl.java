package com.monke.monkeybook.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.BookListContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookListPresenterImpl extends BasePresenterImpl<BookListContract.View> implements BookListContract.Presenter {

    private static final int THREADS_NUM = 4;

    private List<BookShelfBean> bookShelfBeans;
    private List<String> errBooks = new ArrayList<>();
    private CompositeDisposable refreshingDisps = new CompositeDisposable();

    private int group;

    @Override
    public void initData(Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args != null) {
            group = args.getInt("group");
        }
    }

    @Override
    public boolean viewIsList() {
        return mView.getPreferences().getBoolean("bookshelfIsList", true);
    }

    @Override
    public int getBookshelfPx() {
        String bookPx = mView.getPreferences().getString(mView.getContext().getString(R.string.pk_bookshelf_px), "0");
        return Integer.parseInt(bookPx);
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public boolean getNeedAnim() {
        return mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_bookshelf_anim), false);
    }

    @Override
    public void queryBookShelf(boolean refresh) {
        boolean needRefresh = group != 3 && (refresh || haveRefresh());
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList = BookshelfHelp.getBooksByGroup(group);
            e.onNext(bookShelfList == null ? new ArrayList<>() : bookShelfList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        mView.addAllBookShelf(value);
                        if (needRefresh) {
                            if (!NetworkUtil.isNetworkAvailable()) {
                                mView.toast("无网络，请打开网络后再试");
                            } else {
                                bookShelfBeans = value;
                                startRefreshBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    @Override
    public void saveData(List<BookShelfBean> bookShelfBeans) {
        if (bookShelfBeans != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                List<BookShelfBean> temp = new ArrayList<>();
                for (int i = 0, size = bookShelfBeans.size(); i < size; i++) {
                    BookShelfBean shelfBean = bookShelfBeans.get(i);
                    if (shelfBean.getSerialNumber() != i) {
                        shelfBean.setSerialNumber(i);
                        temp.add(shelfBean);
                    }
                }
                if (!temp.isEmpty()) {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplaceInTx(temp);
                }
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void startRefreshBook() {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            errBooks.clear();
            refreshingDisps.clear();

            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                bookShelfBean.setQueuing(true);
            }

            for (int i = 0, size = Math.min(THREADS_NUM, bookShelfBeans.size()); i < size; i++) {
                newRefreshTask();
            }
        }
    }

    private synchronized void newRefreshTask() {
        BookShelfBean bookShelfBean = getNextRefreshBook();
        if (bookShelfBean != null) {
            bookShelfBean.setQueuing(false);
            bookShelfBean.setLoading(true);
            mView.updateBook(bookShelfBean, false);
            refreshBookShelf(bookShelfBean);
        }
    }

    private void refreshBookShelf(BookShelfBean bookShelfBean) {
        WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                .subscribeOn(Schedulers.io())
                .flatMap(this::saveBookToShelfO)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
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
        mView.updateBook(bookShelfBean, false);
        if (error) {
            errBooks.add(bookShelfBean.getBookInfoBean().getName());
        }

        if (checkRefreshComplete()) {
            if (errBooks.size() > 0) {
                Toast.makeText(mView.getContext(), TextUtils.join("、", errBooks) + " 更新失败！", Toast.LENGTH_SHORT).show();
                errBooks.clear();
            }
            mView.sortBookShelf();
        } else {
            newRefreshTask();
        }
    }

    private synchronized BookShelfBean getNextRefreshBook() {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (bookShelfBean.isQueuing() && !bookShelfBean.getUpdateOff()) {
                    return bookShelfBean;
                }
            }
        }
        return null;
    }

    private synchronized boolean checkRefreshComplete() {
        if (bookShelfBeans != null) {
            for (BookShelfBean bookShelfBean : bookShelfBeans) {
                if (bookShelfBean.isLoading()) {
                    return false;
                }
            }
            bookShelfBeans.clear();
        }
        return true;
    }

    private synchronized void setRefreshBookDequeue(BookShelfBean bookShelfBean) {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            for (BookShelfBean bookShelf : bookShelfBeans) {
                if (TextUtils.equals(bookShelf.getNoteUrl(), bookShelfBean.getNoteUrl())) {
                    if (bookShelf.isLoading()) {
                        newRefreshTask();
                    }
                    bookShelf.setQueuing(false);
                    break;
                }
            }
        }
    }

    private synchronized boolean checkNeedUpdate(BookShelfBean bookShelfBean) {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            for (BookShelfBean bookShelf : bookShelfBeans) {
                if (TextUtils.equals(bookShelf.getNoteUrl(), bookShelfBean.getNoteUrl())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                BookshelfHelp.saveBookToShelf(bookShelfBean);
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }


    private boolean haveRefresh() {
        return mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_auto_refresh), false) && !mView.isRecreate();
    }


    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addBookShelf(BookShelfBean bookShelfBean) {
        if (this.group == bookShelfBean.getGroup()) {
            if (bookShelfBean.isLoading()) {
                bookShelfBean.setLoading(false);
            }
            mView.addBookShelf(bookShelfBean);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        mView.removeBookShelf(bookShelfBean);
        setRefreshBookDequeue(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO), @Tag(RxBusTag.UPDATE_BOOK_SHELF)})
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        if (this.group == bookShelfBean.getGroup()
                || checkNeedUpdate(bookShelfBean)) {
            mView.updateBook(bookShelfBean, true);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.SAVE_BOOK_DATA)})
    public void saveBookData(Integer group) {
        if (this.group == group) {
            saveData(mView.getShowingBooks());
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_PX)})
    public void updateBookPx(Integer px) {
        mView.updateBookPx(px);
    }
}
