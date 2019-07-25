package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface CacheManagerContract {

    interface View extends IView{
        void showBookList(List<BookShelfBean> bookShelfBeans);

        void removeItem(BookShelfBean bookShelfBean);

        void toast(String msg);

        void showExtractTip(BookShelfBean bookShelfBean);

        void updateProgress(int max, int progress);

        void showProgress();

        void hideProgress();
    }


    interface Presenter extends IPresenter{
        void queryBooks();

        void extractBookCache(BookShelfBean bookShelfBean, boolean force);

        void cancel();
    }
}
