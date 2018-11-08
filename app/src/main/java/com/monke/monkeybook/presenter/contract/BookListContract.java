package com.monke.monkeybook.presenter.contract;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface BookListContract {

    interface View extends IView {

        /**
         * /**
         * 刷新书架书籍小说信息 更新UI
         */
        void addAllBookShelf(List<BookShelfBean> bookShelfBeanList);

        void refreshBookShelf();

        void startLayoutAnimation();

        void updateBook(BookShelfBean bookShelfBean, boolean sort);

        void addBookShelf(BookShelfBean bookShelfBean);

        void removeBookShelf(BookShelfBean bookShelfBean);

        void clearBookShelf();

        List<BookShelfBean> getShowingBooks();

        void sortBookShelf();

        void updateBookPx(String bookPx);

        void updateLayoutType(boolean viewIsList);

        SharedPreferences getPreferences();

        void refreshError(String error);

        void toast(String msg);

        boolean isRecreate();
    }

    interface Presenter extends IPresenter {
        void initData(Fragment fragment);

        void queryBookShelf(boolean refresh);

        void saveData(List<BookShelfBean> bookShelfBeans);

        boolean viewIsList();

        String getBookPx();

        int getGroup();

        boolean getNeedAnim();
    }

}
