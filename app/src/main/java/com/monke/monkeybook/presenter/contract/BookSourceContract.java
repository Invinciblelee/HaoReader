package com.monke.monkeybook.presenter.contract;

import com.google.android.material.snackbar.Snackbar;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookSourceBean;

import java.io.File;
import java.util.List;

public interface BookSourceContract {

    interface Presenter extends IPresenter {

        void saveData(BookSourceBean bookSourceBean);

        void saveData(List<BookSourceBean> bookSourceBeans);

        void delData(BookSourceBean bookSourceBean);

        void delData(List<BookSourceBean> bookSourceBeans);

        void initData();

        void importBookSource(File file);

        void importBookSource(String url);

        void checkBookSource();

        void refreshGroup();

        void refresh();
    }

    interface View extends IView {

        Snackbar getSnackBar(String msg);

        String getQuery();

        void upGroupMenu(List<String> groupList);

        void resetData(List<BookSourceBean> bookSourceBeans);

        void toast(String msg);

        void showLoading(String msg);

        void dismissHUD();
    }

}
