package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;

public interface ReadBookContract {
    interface View extends IView {

        void stopRefreshChapterList();

        void changeSourceFinish(boolean success, String msg);

        void prepareDisplay();

        void showLoading(String msg);

        void dismissHUD();

        void upMenu();

        /**
         * 开始加载
         */
        void startLoadingBook();

        void onMediaButton();

        void toast(String msg);

        void updateTitle(String title);

        void showBookmark(BookmarkBean bookmarkBean);

        void openChapter(ChapterBean chapterBean);

        void openBookmark(BookmarkBean bookmarkBean);

        void updateBookmark(BookShelfBean bookShelfBean);

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

        boolean inBookShelf();

        BookShelfBean getBookShelf();

        BookSourceBean getBookSource();

        void saveProgress();

        String getChapterTitle(int chapterIndex);

        void addToShelf(final ReadBookPresenterImpl.OnAddListener addListener);

        void handleIntent(android.content.Intent intent);

        void checkBookInfo();

        void addDownload(int start, int end);

        void changeBookSource(SearchBookBean searchBookBean);

        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void disableDurBookSource();

        void cleanCache();
    }

}
