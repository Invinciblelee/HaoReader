package com.monke.monkeybook.presenter.contract;

import android.content.Intent;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;

public interface BookDetailContract {
    interface Presenter extends IPresenter {
        void initData(Intent intent);

        int getOpenFrom();

        SearchBookBean getSearchBook();

        BookShelfBean getBookShelf();

        Boolean inBookShelf();

        void initBookFormSearch(SearchBookBean searchBookBean);

        void getBookShelfInfo();

        void addToBookShelf();

        void removeFromBookShelf();

        void changeBookSource(SearchBookBean searchBookBean);

        void switchUpdate(boolean off);
    }

    interface View extends IView {
        /**
         * 更新书籍详情UI
         */
        void updateView();

        void changeUpdateSwitch(boolean off);

        /**
         * 数据获取失败
         */
        void getBookShelfError();

        void toast(String msg);

        void finish();
    }
}
