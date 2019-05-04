package com.monke.monkeybook.presenter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookRefreshModelImpl;
import com.monke.monkeybook.presenter.contract.BookListContract;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookListPresenterImpl extends BasePresenterImpl<BookListContract.View> implements BookListContract.Presenter, BookRefreshModelImpl.OnBookRefreshListener {

    private BookRefreshModelImpl impl = BookRefreshModelImpl.newInstance();

    private int group;

    @Override
    public void initData(Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args != null) {
            group = args.getInt("group");
        }

        impl.setOnBookRefreshListener(this);
    }

    @Override
    public boolean viewIsList() {
        return AppConfigHelper.get().getBoolean("bookshelfIsList", true);
    }

    @Override
    public int getBookshelfPx() {
        String bookPx = AppConfigHelper.get().getString(MApplication.getInstance().getString(R.string.pk_bookshelf_px), "0");
        return bookPx == null ? 0 : Integer.parseInt(bookPx);
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public boolean getNeedAnim() {
        return AppConfigHelper.get().getBoolean(MApplication.getInstance().getString(R.string.pk_bookshelf_anim), false);
    }

    @Override
    public void queryBookShelf(boolean refresh) {
        boolean needUpdate = group != 3 && (refresh || haveRefresh());

        impl.queryBooks(group, needUpdate, autoClean());
    }

    @Override
    public void onResult(List<BookShelfBean> bookShelfBeans) {
        mView.addAllBookShelf(bookShelfBeans);
    }

    @Override
    public void onError(String msg) {
        mView.refreshError(msg);
    }

    @Override
    public void onRefresh(BookShelfBean bookShelfBean) {
        mView.updateBook(bookShelfBean, false);
    }

    @Override
    public void onRefreshFinish() {
        mView.sortBookShelf();
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
                    DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().insertOrReplaceInTx(temp);
                }
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }


    private boolean haveRefresh() {
        return mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_auto_refresh), false) && !mView.isRecreate();
    }

    private boolean autoClean() {
        return AppConfigHelper.get().getBoolean(mView.getContext().getString(R.string.pk_auto_clean_book), false);
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
        impl.stopRefreshBook();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addBookShelf(BookShelfBean bookShelfBean) {
        if (this.group == bookShelfBean.getGroup()) {
            mView.addBookShelf(bookShelfBean);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        mView.removeBookShelf(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO), @Tag(RxBusTag.UPDATE_BOOK_SHELF)})
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        if (this.group == bookShelfBean.getGroup()) {
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
