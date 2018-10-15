package com.monke.monkeybook.model.task;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.impl.IDownloadTask;

import java.util.ArrayList;
import java.util.List;

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
    private List<DownloadChapterBean> downloadChapters;

    private boolean isLocked = false;

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
                if (!book.isChapterListEmpty()) {
                    for (int i = downloadBook.getStart(); i <= downloadBook.getEnd(); i++) {
                        if (!book.getChapter(i).getHasCache(book.getBookInfoBean())) {
                            DownloadChapterBean item = new DownloadChapterBean();
                            item.setNoteUrl(book.getNoteUrl());
                            item.setDurChapterIndex(book.getChapter(i).getDurChapterIndex());
                            item.setDurChapterName(book.getChapter(i).getDurChapterName());
                            item.setDurChapterUrl(book.getChapter(i).getDurChapterUrl());
                            item.setTag(book.getTag());
                            item.setBookName(book.getBookInfoBean().getName());
                            item.setCoverUrl(book.getBookInfoBean().getCoverUrl());
                            downloadChapters.add(item);
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
                        } else {
                            onDownloadComplete(downloadBook);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
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

        if (!isLocked) {
            getDownloadingChapter()
                    .subscribe(new SimpleObserver<DownloadChapterBean>() {
                        @Override
                        public void onNext(DownloadChapterBean chapterBean) {
                            if (chapterBean != null) {
                                downloading(chapterBean, scheduler);
                            } else {
                                isLocked = true;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            onDownloadError(downloadBook);
                        }
                    });
        }
    }

    private Observable<DownloadChapterBean> getDownloadingChapter() {
        return Observable.create(emitter -> {
            DownloadChapterBean next = null;
            List<DownloadChapterBean> temp = new ArrayList<>(downloadChapters);
            for (DownloadChapterBean data : temp) {
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
            emitter.onNext(next);
        });
    }

    private void downloading(DownloadChapterBean data, Scheduler scheduler) {
        whenProgress(data);
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            e.onNext(!BookshelfHelp.isChapterCached(
                    BookshelfHelp.getCachePathName(data),
                    BookshelfHelp.getCacheFileName(data.getDurChapterIndex(), data.getDurChapterName())
            ));
            e.onComplete();
        }).subscribeOn(scheduler)
                .flatMap(result -> {
                            BookContentBean bookContentBean = new BookContentBean();
                            bookContentBean.setRight(false);
                            if (result) {
                                return WebBookModelImpl.getInstance()
                                        .getBookContent(data.getDurChapterUrl(), data.getDurChapterIndex(), data.getTag())
                                        .onErrorReturnItem(bookContentBean);
                            } else {
                                return Observable.create(e -> {
                                    e.onNext(bookContentBean);
                                    e.onComplete();
                                });
                            }
                        }
                )
                .flatMap(bookContentBean -> Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                    if (bookContentBean.getRight()) {
                        BookshelfHelp.saveChapterInfo(BookshelfHelp.getCachePathName(data),
                                BookshelfHelp.getCacheFileName(data.getDurChapterIndex(), data.getDurChapterName()),
                                bookContentBean.getDurChapterContent());
                        RxBus.get().post(RxBusTag.CHAPTER_CHANGE, bookContentBean.getDurChapterUrl());
                    }
                    e.onNext(removeFromDownloadList(data));
                }))
                .onErrorReturnItem(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Boolean bool) {
                        whenNext(scheduler);
                    }

                    @Override
                    public void onError(Throwable e) {
                        removeFromDownloadList(data);

                        whenError(scheduler);
                    }
                });
    }

    private synchronized boolean removeFromDownloadList(DownloadChapterBean chapterBean) {
        return downloadChapters.remove(chapterBean);
    }

    private synchronized boolean checkInDownloadList(DownloadChapterBean chapterBean) {
        return downloadChapters.contains(chapterBean);
    }

    private void whenNext(Scheduler scheduler) {
        if (!isDownloading) {
            return;
        }

        downloadBook.successCountAdd();

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
            onDownloadError(downloadBook);
        } else {
            toDownload(scheduler);
        }
    }

    private void whenProgress(DownloadChapterBean chapterBean) {
        if (!isDownloading) {
            return;
        }
        onDownloadProgress(chapterBean);
    }
}
