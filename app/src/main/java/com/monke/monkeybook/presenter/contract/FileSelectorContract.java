package com.monke.monkeybook.presenter.contract;

import android.support.v4.app.Fragment;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.FileSnapshot;
import com.monke.monkeybook.bean.RipeFile;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public interface FileSelectorContract {

    interface Presenter extends IPresenter {
        void init(Fragment fragment);

        Comparator<RipeFile> sort(int orderIndex);

        void startLoad();

        boolean pop();

        boolean push(RipeFile folder, int offset);

        boolean canGoBack();

        boolean isSingleChoice();

        boolean checkBookAdded();

        boolean isImage();
    }

    interface View extends IView {

        void showLoading();

        void hideLoading();

        void showSubtitle(String subtitle);

        void onShow(FileSnapshot snapshot, boolean back);

        void showBigImage(android.view.View shareView, String url);

    }

}
