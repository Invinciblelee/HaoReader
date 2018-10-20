package com.monke.monkeybook.presenter.contract;

import android.app.Activity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

public interface WelcomeContract {

    interface View extends IView {
        void openBookFromUri();

        void startReadBook(BookShelfBean shelfBean, boolean inShelf, long delay);

        void onNormalCreate();

        void onFromOtherCreate();

        void finish();
    }

    interface Presenter extends IPresenter {

        void initData(Activity activity);

        void openBookFromUri(Activity activity);

    }
}
