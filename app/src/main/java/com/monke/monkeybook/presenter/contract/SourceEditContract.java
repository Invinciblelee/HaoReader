package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookSourceBean;

import java.io.File;

public interface SourceEditContract {
    interface Presenter extends IPresenter {

        void saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld, boolean debug);

        void copySource(BookSourceBean bookSourceBean);

        void pasteSource();

        void setText(String bookSourceStr);

        void handleSourceShare();

    }

    interface View extends IView {

        void setText(BookSourceBean bookSourceBean);

        void toDebug(BookSourceBean bookSourceBean);

        String getBookSourceStr();

        String getBookSourceName();

        void saveSuccess();

        void toast(String msg);

        void shareSource(File file, String mediaType);
    }
}
