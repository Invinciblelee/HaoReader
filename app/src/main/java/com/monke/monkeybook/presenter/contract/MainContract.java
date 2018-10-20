package com.monke.monkeybook.presenter.contract;

import android.content.SharedPreferences;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface MainContract {

    interface View extends IView {

        void initImmersionBar();

        /**
         * 刷新书架书籍小说信息 更新UI
         *
         */
        void refreshBookShelf(int group, List<BookShelfBean> bookShelfBeanList);

        void startLayoutAnimation();

        void updateBook(BookShelfBean bookShelfBean, boolean sort);

        void addToBookShelf(BookShelfBean bookShelfBean);

        void removeFromBookShelf(BookShelfBean bookShelfBean);

        void sortBookShelf();

        /**
         * 取消弹出框
         */
        void dismissHUD();

        /**
         * 刷新错误
         *
         * @param error 错误
         */
        void refreshError(String error);

        /**
         * 显示等待框
         */
        void showLoading(String msg);

        /**
         * 恢复数据
         */
        void onRestore(String msg);

        void restoreSuccess();

        SharedPreferences getPreferences();

        void recreate();


    }

    interface Presenter extends IPresenter {
        void queryBookShelf(boolean needRefresh, boolean needAnim, int group);

        void saveData(List<BookShelfBean> bookShelfBeans);

        void backupData();

        void restoreData();

        boolean checkLocalBookExists(BookShelfBean bookShelf);

        void addBookUrl(String bookUrl);

        void removeFromBookSelf(BookShelfBean bookShelf);

        void clearCaches();

        void clearBookshelf();
    }

}
