package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.impl.IDownloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class DownloadTaskImpl implements IDownloadTask {

    private final long when;

    private boolean isDownloading = false;

    private DownloadBookBean downloadBook;
    private BookInfoBean bookInfo;
    private List<ChapterBean> downloadChapters;

    private CompositeDisposable disposables;

    protected DownloadTaskImpl(DownloadBookBean downloadBook) {
        this.when = System.currentTimeMillis();
        this.downloadBook = downloadBook;
        downloadChapters = new ArrayList<>();
        disposables = new CompositeDisposable();

        Observable.create((ObservableOnSubscribe<DownloadBookBean>) emitter -> {
            BookShelfBean book = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.NoteUrl.eq(downloadBook.getNoteUrl())).build().unique();
            if (book != null) {
                bookInfo = book.getBookInfoBean();
                if (!book.realChapterListEmpty()) {
                    for (int i = downloadBook.getStart(); i <= downloadBook.getEnd(); i++) {
                        ChapterBean chapter = book.getChapter(i);
                        if (!chapter.getHasCache(bookInfo)) {
                            downloadChapters.add(chapter);
                        }
                    }
                }
                downloadBook.setDownloadCount(downloadChapters.size());
            } else {
                downloadBook.setValid(false);
            }
            emitter.onNext(downloadBook);
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<DownloadBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(DownloadBookBean downloadBook) {
                        if (downloadBook.isValid()) {
                            onDownloadPrepared(downloadBook);
                            whenProgress(downloadBook.getName(), downloadChapters.get(0));
                        } else {
                            onDownloadComplete(downloadBook);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        downloadBook.setValid(false);
                        onDownloadError(downloadBook);
                    }
                });
    }

    @Override
    public int getId() {
        return (int) when;
    }

    @Override
    public long getWhen() {
        return when;
    }

    @Override
    public void startDownload(Scheduler scheduler) {
        if (isFinishing()) return;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        isDownloading = true;

        toDownload(scheduler);
    }

    @Override
    public void stopDownload(boolean callEvent) {
        isDownloading = false;
        downloadChapters.clear();

        if (!disposables.isDisposed()) {
            disposables.dispose();

            if (callEvent) {
                if (downloadBook.getSuccessCount() == 0) {
                    onDownloadCancel(downloadBook);
                } else {
                    onDownloadComplete(downloadBook);
                }
            }
        }
    }

    @Override
    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    public boolean isFinishing() {
        return downloadChapters.isEmpty();
    }

    @Override
    public DownloadBookBean getDownloadBook() {
        return downloadBook;
    }

    private void toDownload(Scheduler scheduler) {
        if (isFinishing()) {
            return;
        }

        getDownloadingChapter()
                .subscribe(new SimpleObserver<ChapterBean>() {
                    @Override
                    public void onNext(ChapterBean chapterBean) {
                        if (!TextUtils.isEmpty(chapterBean.getDurChapterUrl())) {
                            downloading(chapterBean, scheduler);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDownloadError(downloadBook);
                    }
                });
    }

    private Observable<ChapterBean> getDownloadingChapter() {
        return Observable.create(emitter -> {
            ChapterBean next = null;
            List<ChapterBean> temp = new ArrayList<>(downloadChapters);
            for (ChapterBean data : temp) {
                if (data.getHasCache(bookInfo)) {
                    removeFromDownloadList(data);
                } else {
                    next = data;
                    break;
                }
            }
            emitter.onNext(next == null ? new ChapterBean() : next);
            emitter.onComplete();
        });
    }

    private void downloading(ChapterBean chapter, Scheduler scheduler) {
        whenProgress(downloadBook.getName(), chapter);
        Observable.create((ObservableOnSubscribe<ChapterBean>) e -> {
            if (!ChapterContentHelp.isChapterCached(downloadBook, chapter)) {
                e.onNext(chapter);
            } else {
                e.onError(new Exception("chapter already cached"));
            }
            e.onComplete();
        })
                .subscribeOn(scheduler)
                .flatMap(result -> WebBookModel.getInstance().getBookContent(bookInfo, chapter))
                .timeout(25, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookContentBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {
                        RxBus.get().post(RxBusTag.CHAPTER_CHANGE, bookContentBean);
                        removeFromDownloadList(chapter);
                        whenNext(scheduler, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        removeFromDownloadList(chapter);
                        if (TextUtils.equals(e.getMessage(), "cached")) {
                            whenNext(scheduler, false);
                        } else {
                            whenError(scheduler);
                        }
                    }
                });
    }

    private void removeFromDownloadList(ChapterBean chapterBean) {
        downloadChapters.remove(chapterBean);
    }

    private void whenNext(Scheduler scheduler, boolean success) {
        if (!isDownloading) {
            return;
        }

        if (success) {
            downloadBook.successCountAdd();
        }
        if (isFinishing()) {
            stopDownload(false);
            onDownloadComplete(downloadBook);
        } else {
            onDownloadChange(downloadBook);
            toDownload(scheduler);
        }
    }

    private void whenError(Scheduler scheduler) {
        if (!isDownloading) {
            return;
        }

        if (isFinishing()) {
            stopDownload(false);
            if (downloadBook.getSuccessCount() == 0) {
                onDownloadError(downloadBook);
            } else {
                onDownloadComplete(downloadBook);
            }
        } else {
            toDownload(scheduler);
        }
    }

    private void whenProgress(String bookName, ChapterBean chapterBean) {
        if (!isDownloading) {
            return;
        }
        onDownloadProgress(bookName, chapterBean);
    }
}
