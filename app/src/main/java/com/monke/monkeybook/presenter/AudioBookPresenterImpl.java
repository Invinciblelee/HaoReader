package com.monke.monkeybook.presenter;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookRefreshModelImpl;
import com.monke.monkeybook.presenter.contract.AudioBookContract;

import java.util.List;

public class AudioBookPresenterImpl extends BasePresenterImpl<AudioBookContract.View> implements AudioBookContract.Presenter, BookRefreshModelImpl.OnBookRefreshListener {

    private static boolean hasUpdated = false;

    public static void setHasUpdated(boolean hasUpdated) {
        AudioBookPresenterImpl.hasUpdated = hasUpdated;
    }

    private BookRefreshModelImpl impl = BookRefreshModelImpl.newInstance();

    public AudioBookPresenterImpl() {
        impl.setOnBookRefreshListener(this);
    }

    @Override
    public void loadAudioBooks(boolean refresh) {
        boolean needUpdate = refresh || haveRefresh();
        impl.queryBooks(Constant.BookType.AUDIO, needUpdate);
    }

    @Override
    public void onResult(List<BookShelfBean> bookShelfBeans) {
        mView.showAudioBooks(bookShelfBeans);
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            hasUpdated = true;
        }
    }

    @Override
    public void onError(String msg) {
        mView.toast(msg);
    }

    @Override
    public void onRefresh(BookShelfBean bookShelfBean) {
        mView.updateBook(bookShelfBean, false);
    }

    @Override
    public void onRefreshFinish() {
        mView.sortBookShelf();
        mView.refreshFinish();
    }

    @Override
    public boolean getNeedAnim() {
        return AppConfigHelper.get(mView.getContext()).getBoolean(mView.getContext().getString(R.string.pk_bookshelf_anim), false);
    }

    @Override
    public int getBookshelfPx() {
        String bookPx = AppConfigHelper.get(mView.getContext()).getString(mView.getContext().getString(R.string.pk_bookshelf_px), "0");
        return bookPx == null ? 0 : Integer.parseInt(bookPx);
    }


    private boolean haveRefresh() {
        return !hasUpdated && AppConfigHelper.get(mView.getContext()).getBoolean(mView.getContext().getString(R.string.pk_auto_refresh), false);
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
        if (bookShelfBean.getGroup() == 4) {
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
        if (bookShelfBean.getGroup() == 4) {
            mView.updateBook(bookShelfBean, true);
        }
    }

}
