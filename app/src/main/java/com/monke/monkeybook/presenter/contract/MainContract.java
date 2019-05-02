package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

import androidx.annotation.StringDef;
import androidx.annotation.StringRes;

public interface MainContract {

    interface View extends IView {

        void initImmersionBar();

        void updateBook(BookShelfBean bookShelfBean, boolean sort);

        void addBookShelf(BookShelfBean bookShelfBean);

        void removeBookShelf(BookShelfBean bookShelfBean);

        void showQueryBooks(List<BookShelfBean> bookShelfBeans);

        void clearBookshelf();

        /**
         * 取消弹出框
         */
        void dismissHUD();

        /**
         * 显示等待框
         */
        void showLoading(String msg);

        void restoreSuccess();

        /**
         * 添加成功
         */
        void addSuccess(BookShelfBean bookShelfBean);

        void showSnackBar(String msg);

        void showSnackBar(@StringRes int msgId);
    }

    interface Presenter extends IPresenter {
        void importBooks(List<String> books);

        void backupData();

        void restoreData();

        boolean checkLocalBookNotExists(BookShelfBean bookShelf);

        void addBookUrl(String bookUrl);

        void removeFromBookShelf(BookShelfBean bookShelfBean);

        void cleanCaches();

        void clearBookshelf();

        void queryBooks(String query);
    }

}
