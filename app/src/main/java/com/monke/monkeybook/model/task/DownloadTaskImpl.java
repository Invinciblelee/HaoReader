package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
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
import io.reactivex.schedulers.Schedulers;

public abstract class DownloadTaskImpl implements IDownloadTask {

    private int id;

    private boolean isDownloading = false;

    private DownloadBookBean downloadBook;
    private List<ChapterListBean> downloadChapters;

    private CompositeDisposable disposables;

    public DownloadTaskImpl(int id, DownloadBookBean downloadBook) {
        this.id = id;
        this.downloadBook = downloadBook;
        downloadChapters = new ArrayList<>();
        disposables = new CompositeDisposable();

        Observable.create((ObservableOnSubscribe<DownloadBookBean>) emitter -> {
            BookShelfBean book = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.NoteUrl.eq(downloadBook.getNoteUrl())).build().unique();
            if (book != null) {
                if (!book.realChapterListEmpty()) {
                    for (int i = downloadBook.getStart(); i <= downloadBook.getEnd(); i++) {
                        ChapterListBean chapter = book.getChapter(i);
                        if (!chapter.getHasCache(book.getBookInfoBean())) {
                            downloadChapters.add(chapter);
                        }
                    }
                }
                downloadBook.setDownloadCount(downloadChapters.size());
            } else {
                downloadBook.setValid(false);
            }
            emitter.onNext(downloadBook);
        }).subscribeOn(Schedulers.io())
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
                            whenProgress(downloadChapters.get(0));
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
        return id;
    }

    @Override
    public void startDownload(Scheduler scheduler, int threadsNum) {
        if (isFinishing()) return;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        isDownloading = true;

        toDownload(scheduler);
    }

    @Override
    public void stopDownload() {
        if (isDownloading) {
            isDownloading = false;
            onDownloadComplete(downloadBook);
        }

        if (!isFinishing()) {
            downloadChapters.clear();
        }

        if (!disposables.isDisposed()) {
            disposables.dispose();
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
                .subscribe(new SimpleObserver<ChapterListBean>() {
                    @Override
                    public void onNext(ChapterListBean chapterBean) {
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

    private Observable<ChapterListBean> getDownloadingChapter() {
        return Observable.create(emitter -> {
            ChapterListBean next = null;
            List<ChapterListBean> temp = new ArrayList<>(downloadChapters);
            for (ChapterListBean data : temp) {
                boolean cached = BookshelfHelp.isChapterCached(
                        BookshelfHelp.getCachePathName(data),
                        BookshelfHelp.getCacheFileName(data.getDurChapterIndex(), data.getDurChapterName()));
                if (cached) {
                    removeFromDownloadList(data);
                } else {
                    next = data;
                    break;
                }
            }
            emitter.onNext(next == null ? new ChapterListBean() : next);
        });
    }

    private void downloading(ChapterListBean chapter, Scheduler scheduler) {
        whenProgress(chapter);
        Observable.create((ObservableOnSubscribe<ChapterListBean>) e -> {
            if (!BookshelfHelp.isChapterCached(
                    BookshelfHelp.getCachePathName(chapter),
                    BookshelfHelp.getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName())
            )) {
                e.onNext(chapter);
            } else {
                e.onError(new Exception("cached"));
            }
            e.onComplete();
        })
                .flatMap(result -> WebBookModelImpl.getInstance().getBookContent(scheduler, chapter))
                .timeout(20, TimeUnit.SECONDS)
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

    private void removeFromDownloadList(ChapterListBean chapterBean) {
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
            stopDownload();
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
            stopDownload();
            if (downloadBook.getSuccessCount() == 0) {
                onDownloadError(downloadBook);
            } else {
                onDownloadComplete(downloadBook);
            }
        } else {
            toDownload(scheduler);
        }
    }

    private void whenProgress(ChapterListBean chapterBean) {
        if (!isDownloading) {
            return;
        }
        onDownloadProgress(chapterBean);
    }
}
