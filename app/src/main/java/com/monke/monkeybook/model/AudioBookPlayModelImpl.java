package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.content.BookException;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AudioBookPlayModelImpl implements IAudioBookPlayModel {

    private static final int RETRY_COUNT = 3;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable mPlayDisposable;

    private PlayCallback mPlayCallback;

    private boolean isPrepared;
    private BookShelfBean bookShelfBean;

    private int mPlayIndex;
    private int mRetryCount;

    public AudioBookPlayModelImpl(BookShelfBean bookShelfBean) {
        this.bookShelfBean = bookShelfBean;
    }

    @Override
    public void registerPlayCallback(PlayCallback callback) {
        this.mPlayCallback = callback;
    }

    @Override
    public void ensureChapterList(Callback<BookShelfBean> callback) {
        if (mPlayCallback != null) {
            mPlayCallback.onStart();
        }

        if (!bookShelfBean.realChapterListEmpty()) {
            isPrepared = true;
            if (callback != null) {
                callback.onSuccess(bookShelfBean);
            }
            mPlayIndex = bookShelfBean.getDurChapter();
            ChapterBean chapterBean = bookShelfBean.getChapter(mPlayIndex);
            playChapter(chapterBean, false);
            saveBookShelf(bookShelfBean);
        } else {
            Observable.create((ObservableOnSubscribe<List<ChapterBean>>) emitter -> {
                List<ChapterBean> chapterBeans = BookshelfHelp.queryChapterList(bookShelfBean.getNoteUrl());
                if (chapterBeans != null) {
                    emitter.onNext(chapterBeans);
                } else {
                    emitter.onNext(new ArrayList<>());
                }
                emitter.onComplete();
            })
                    .subscribeOn(Schedulers.single())
                    .flatMap((Function<List<ChapterBean>, ObservableSource<BookShelfBean>>) chapterBeans -> {
                        if (chapterBeans.isEmpty()) {
                            return getChapterList(bookShelfBean);
                        }
                        bookShelfBean.setChapterList(chapterBeans);
                        return Observable.just(bookShelfBean);
                    })
                    .doAfterNext(this::saveBookShelf)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<BookShelfBean>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            if (bookShelfBean.realChapterListEmpty()) {
                                if (callback != null) {
                                    callback.onError(new BookException("目录获取失败"));
                                }
                            } else {
                                isPrepared = true;
                                if (callback != null) {
                                    callback.onSuccess(bookShelfBean);
                                }

                                mPlayIndex = bookShelfBean.getDurChapter();
                                ChapterBean chapterBean = bookShelfBean.getChapter(mPlayIndex);
                                playChapter(chapterBean, false);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (callback != null) {
                                callback.onError(e);
                            }
                        }
                    });
        }
    }

    @Override
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && !bookShelfBean.realChapterListEmpty()) {
            this.bookShelfBean = bookShelfBean;
        }
    }

    @Override
    public void playNext() {
        if (!isPrepared) return;

        mRetryCount = 0;

        if (hasNext()) {
            mPlayIndex += 1;
            ChapterBean chapterBean = bookShelfBean.getChapter(mPlayIndex);
            playChapter(chapterBean, true);
        }

    }

    @Override
    public void playPrevious() {
        if (!isPrepared) return;

        mRetryCount = 0;

        if (hasPrevious()) {
            mPlayIndex -= 1;
            ChapterBean chapterBean = bookShelfBean.getChapter(mPlayIndex);
            playChapter(chapterBean, true);
        }

    }

    @Override
    public boolean hasNext() {
        return mPlayIndex < bookShelfBean.getChapterList().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return mPlayIndex > 0;
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public ChapterBean getDurChapter() {
        if (bookShelfBean != null && !bookShelfBean.realChapterListEmpty()) {
            return bookShelfBean.getChapter(mPlayIndex);
        }
        return null;
    }

    @Override
    public void playChapter(ChapterBean chapter, boolean reset) {
        if (!isPrepared) return;

        if (mPlayCallback != null) {
            mPlayCallback.onStart();
        }

        if (mPlayDisposable != null) {
            disposables.remove(mPlayDisposable);
        }

        if (mPlayCallback != null) {
            mPlayCallback.onPrepare(chapter);
        }

        Observable.just(chapter)
                .subscribeOn(Schedulers.single())
                .map(chapterBean -> {
                    if (reset) {
                        chapterBean.setStart(0);
                    }
                    return chapterBean;
                })
                .doOnNext(chapterBean -> {
                    mPlayIndex = chapterBean.getDurChapterIndex();
                    bookShelfBean.setDurChapter(chapterBean.getDurChapterIndex());
                    bookShelfBean.setDurChapterName(chapterBean.getDurChapterName());
                    saveBookShelf(bookShelfBean);
                })
                .flatMap((Function<ChapterBean, ObservableSource<ChapterBean>>) chapterBean -> {
                    if (!TextUtils.isEmpty(chapter.getDurChapterPlayUrl())) {
                        return Observable.just(chapterBean);
                    }
                    return WebBookModelImpl.getInstance()
                            .processAudioChapter(bookShelfBean.getTag(), chapterBean);
                })
                .timeout(15, TimeUnit.SECONDS)
                .retry(RETRY_COUNT, throwable -> throwable instanceof TimeoutException)
                .flatMap((Function<ChapterBean, ObservableSource<ChapterBean>>) chapterBean -> {
                    if (!TextUtils.isEmpty(chapterBean.getDurChapterPlayUrl())) {
                        return Observable.create(emitter -> {
                            emitter.onNext(chapterBean);
                            emitter.onComplete();
                        });
                    }
                    return Observable.error(new NullPointerException("play url is null"));
                })
                .doAfterNext(chapterBean -> {
                    try {
                        bookShelfBean.getChapterList().set(chapterBean.getDurChapterIndex(), chapterBean);
                        if (BookshelfHelp.isInBookShelf(chapterBean.getNoteUrl())) {
                            DbHelper.getInstance().getDaoSession().getChapterBeanDao().insertOrReplace(chapterBean);
                        }
                    } catch (Exception ignore) {
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<ChapterBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(mPlayDisposable = d);
                    }

                    @Override
                    public void onNext(ChapterBean chapterBean) {
                        if (mPlayCallback != null) {
                            mPlayCallback.onPlay(chapterBean);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayCallback != null) {
                            mPlayCallback.onError(e);
                        }
                    }
                });

    }

    @Override
    public boolean retryPlay() {
        if (!isPrepared) return false;

        ChapterBean chapterBean = getDurChapter();
        if (chapterBean != null && mRetryCount < RETRY_COUNT) {
            mRetryCount += 1;
            chapterBean.setDurChapterPlayUrl(null);
            playChapter(chapterBean, true);
            return true;
        }
        return false;
    }

    @Override
    public void saveProgress(int progress, int duration) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                ChapterBean chapterBean = bookShelfBean.getChapter(mPlayIndex);
                chapterBean.setStart(progress);
                chapterBean.setEnd(duration);
                DbHelper.getInstance().getDaoSession().getChapterBeanDao().insertOrReplace(chapterBean);
            }
            emitter.onNext(true);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean bool) {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void saveBookShelf(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) emitter -> {
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookShelfBean.setHasUpdate(false);
            if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                BookshelfHelp.saveBookToShelf(bookShelfBean);
            }
            emitter.onNext(bookShelfBean);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        RxBus.get().post(RxBusTag.UPDATE_BOOK_SHELF, bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private Observable<BookShelfBean> getChapterList(BookShelfBean bookShelf) {
        return WebBookModelImpl.getInstance().getChapterList(bookShelf)
                .doOnNext(bookShelfBean -> {
                    // 存储章节到数据库
                    bookShelfBean.setHasUpdate(false);
                    bookShelfBean.setNewChapters(0);
                    bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                    saveBookShelf(bookShelfBean);
                });
    }

    public void destroy() {
        disposables.dispose();
    }

}
