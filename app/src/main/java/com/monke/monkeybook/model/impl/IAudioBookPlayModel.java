package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;

public interface IAudioBookPlayModel {

    void registerPlayCallback(PlayCallback callback);

    void ensureChapterList(Callback<BookShelfBean> callback);

    void updateBookShelf(BookShelfBean bookShelfBean);

    void addToShelf();

    boolean inBookShelf();

    void playChapter(ChapterBean chapter, boolean reset);

    boolean retryPlay();

    void saveProgress(int progress, int duration);

    void playNext();

    void playPrevious();

    boolean hasNext();

    boolean hasPrevious();

    boolean isPrepared();

    ChapterBean getDurChapter();


    interface Callback<T>{

        void onSuccess(T data);

        void onError(Throwable error);

    }

    interface PlayCallback{

        void onStart();

        void onPrepare(ChapterBean chapterBean);

        void onPlay(ChapterBean chapterBean);

        void onError(Throwable throwable);

    }
}
