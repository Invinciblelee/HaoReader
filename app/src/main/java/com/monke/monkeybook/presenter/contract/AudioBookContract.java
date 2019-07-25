package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface AudioBookContract {

    interface Presenter extends IPresenter {

        void loadAudioBooks(boolean refresh);

        boolean getNeedAnim();
    }


    interface View extends IView {

        void showAudioBooks(List<BookShelfBean> bookShelfBeans);

        void addBookShelf(BookShelfBean bookShelfBean);

        void removeBookShelf(BookShelfBean bookShelfBean);

        void updateBook(BookShelfBean bookShelfBean, boolean b);

        void sortBookShelf();

        void clearBookShelf();

        void toast(String msg);

        void onPlayEvent(AudioPlayInfo info);
    }

}
