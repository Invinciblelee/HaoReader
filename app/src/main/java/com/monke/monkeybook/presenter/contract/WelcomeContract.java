package com.monke.monkeybook.presenter.contract;

import android.app.Activity;
import android.content.SharedPreferences;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

public interface WelcomeContract {

    interface View extends IView {
        void openBookFromUri();

        void startReadBookAct(BookShelfBean shelfBean, boolean inShelf, boolean fromUri, long startDelay);

        void onStartNormal(long startDelay);

        void onStartFromUri();

        SharedPreferences getPreferences();

        void finish();
    }

    interface Presenter extends IPresenter {

        void initData(Activity activity);

        void openBookFromRecent();

        void openBookFromUri(Activity activity);

    }
}
