package com.monke.monkeybook.presenter.contract;

import android.app.Activity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;

public interface ReadBookContract {
    interface View extends IView {

        /**
         * @return Book标志
         */
        String getNoteUrl();

        void finishContent(int chapter);

        void changeSourceFinish(boolean success);

        void chapterError(int chapter, int status);

        void showHideView();

        void prepareDisplay(boolean check);

        /**
         * 开始加载
         */
        void startLoadingBook();

        void chapterChange(int chapterIndex);

        void chapterListChanged();

        void chapterListUpdateFinish();

        void onMediaButton();

        void toast(String msg);

        void updateTitle(String title);

        /**
         * 更新朗读状态
         */
        void upAloudState(int state);

        void upAloudTimer(String timer);

        void speakIndex(int index);

        void refresh(boolean recreate);

        void finish();
    }

    interface Presenter extends IPresenter {

        boolean isRecreate();

        boolean isOpenFromUri();

        boolean inBookShelf();

        BookShelfBean getBookShelf();

        void loadContent(final int chapterIndex);

        void saveProgress();

        String getChapterTitle(int chapterIndex);

        void addToShelf(final ReadBookPresenterImpl.OnAddListener addListner);

        void removeFromShelf();

        void prepare(Activity activity);

        void checkBookInfo();

        void addDownload(int start, int end);

        void changeBookSource(SearchBookBean searchBookBean);

        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void disableDurBookSource();

        void updateChapterList();
    }
}
