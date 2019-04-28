package com.monke.monkeybook.presenter.contract;

import androidx.fragment.app.Fragment;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.FileSnapshot;
import com.monke.monkeybook.bean.RipeFile;

import java.util.Comparator;

public interface FileSelectorContract {

    interface Presenter extends IPresenter {
        void init(Fragment fragment);

        Comparator<RipeFile> sort(int orderIndex);

        void startLoad();

        void pop();

        void push(RipeFile folder, int offset);

        void refreshCurrent();

        boolean canGoBack();

        boolean isSingleChoice();

        boolean checkBookAdded();

        boolean isImage();

        String getTitle();
    }

    interface View extends IView {

        void showLoading();

        void hideLoading();

        int getScrollOffset();

        void onShow(FileSnapshot snapshot, boolean back);

        void showBigImage(android.view.View shareView, String url);

    }

}
