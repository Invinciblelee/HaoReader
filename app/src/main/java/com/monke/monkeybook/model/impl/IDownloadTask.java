package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;

import io.reactivex.Scheduler;

public interface IDownloadTask {

    int getId();

    long getWhen();

    void startDownload(Scheduler scheduler);

    void stopDownload(boolean callEvent);

    boolean isDownloading();

    boolean isFinishing();

    DownloadBookBean getDownloadBook();

    void onDownloadPrepared(DownloadBookBean downloadBook);

    void onDownloadProgress(String bookName, ChapterBean chapterBean);

    void onDownloadChange(DownloadBookBean downloadBook);

    void onDownloadError(DownloadBookBean downloadBook);

    void onDownloadComplete(DownloadBookBean downloadBook);

    void onDownloadCancel(DownloadBookBean downloadBook);
}
