package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.NetworkUtil;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BookShelfHolder;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;
import com.monke.monkeybook.service.AudioBookPlayService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AudioBookPlayModelImpl implements IAudioBookPlayModel {

    private static final int RETRY_COUNT = 2;

    private CompositeDisposable disposables;

    private Disposable mPlayDisposable;
    private Disposable mChapterDisposable;
    private Disposable mUpdateChaptersDisp;

    private PlayCallback mPlayCallback;

    private boolean isPrepared;
    private BookShelfBean bookShelf;

    private int mPlayIndex;

    public AudioBookPlayModelImpl(BookShelfBean bookShelf) {
        this.bookShelf = bookShelf;
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

        if (disposables != null && mChapterDisposable != null) {
            disposables.remove(mChapterDisposable);
        }

        if (!bookShelf.realChapterListEmpty()) {
            onSuccess(callback);
        } else {
            Observable.create((ObservableOnSubscribe<List<ChapterBean>>) emitter -> {
                List<ChapterBean> chapterBeans = BookshelfHelp.queryChapterList(bookShelf.getNoteUrl());
                if (chapterBeans != null) {
                    emitter.onNext(chapterBeans);
                } else {
                    emitter.onNext(new ArrayList<>());
                }
                emitter.onComplete();
            })
                    .subscribeOn(RxExecutors.getDefault())
                    .flatMap(chapterBeans -> {
                        if (chapterBeans.isEmpty()) {
                            return getChapterList(bookShelf);
                        }
                        bookShelf.setChapterList(chapterBeans);
                        return Observable.just(bookShelf);
                    })
                    .doOnNext(this::saveBookShelf)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<BookShelfBean>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            ensureCompositeDisposable();
                            disposables.add(mChapterDisposable = d);
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            bookShelf = bookShelfBean;
                            onSuccess(callback);
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

    private void onSuccess(Callback<BookShelfBean> callback) {
        if (bookShelf.realChapterListEmpty()) {
            if (callback != null) {
                callback.onError(new Exception("目录获取失败"));
            }
        } else {
            isPrepared = true;
            if (callback != null) {
                callback.onSuccess(bookShelf);
            }

            mPlayIndex = bookShelf.getDurChapter();
            ChapterBean chapterBean = bookShelf.getChapter(mPlayIndex);
            playChapter(chapterBean, false);
        }
    }

    @Override
    public void changeSource(SearchBookBean searchBookBean, Callback<BookShelfBean> callback) {
        if (bookShelf == null) return;

        if (mPlayCallback != null) {
            mPlayCallback.onStart();
        }

        if (disposables != null && mChapterDisposable != null) {
            disposables.remove(mChapterDisposable);
        }

        BookShelfBean target = BookshelfHelp.getBookFromSearchBook(searchBookBean);
        target.setSerialNumber(bookShelf.getSerialNumber());
        target.setDurChapterName(bookShelf.getDurChapterName());
        target.setDurChapter(bookShelf.getDurChapter());
        target.setDurChapterPage(bookShelf.getDurChapterPage());
        target.setFinalDate(bookShelf.getFinalDate());
        WebBookModel.getInstance().getBookInfo(target)
                .subscribeOn(RxExecutors.getDefault())
                .flatMap(bookShelfBean -> WebBookModel.getInstance().getChapterList(bookShelfBean))
                .timeout(30, TimeUnit.SECONDS)
                .map(bookShelfBean -> {
                    bookShelfBean.setGroup(bookShelf.getGroup());
                    bookShelfBean.setUpdateOff(bookShelf.getUpdateOff());
                    bookShelfBean.setNewChapters(0);
                    return bookShelfBean;
                })
                .flatMap((Function<BookShelfBean, ObservableSource<BookShelfBean>>) bookShelfBean -> {
                    if (!bookShelfBean.realChapterListEmpty()) {
                        return Observable.create(emitter -> {
                            if (inBookShelf()) {
                                BookshelfHelp.removeFromBookShelf(bookShelf);
                                BookshelfHelp.saveBookToShelf(bookShelfBean);
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf.withFlag(true));
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            }
                            emitter.onNext(bookShelfBean);
                            emitter.onComplete();
                        });
                    }
                    return Observable.error(new Exception("目录获取失败"));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookShelf = bookShelfBean;
                        onSuccess(callback);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                });
    }

    @Override
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && !bookShelfBean.realChapterListEmpty()) {
            this.bookShelf = bookShelfBean;
        }
    }

    @Override
    public void updateChapters() {
        if (bookShelf == null) {
            return;
        }

        if (mUpdateChaptersDisp != null && !mUpdateChaptersDisp.isDisposed()) {
            onMessage("章节正在更新中，请稍侯");
            return;
        }

        onMessage("正在更新章节，请稍侯");
        WebBookModel.getInstance().getChapterList(bookShelf.copy())
                .subscribeOn(RxExecutors.getDefault())
                .timeout(60, TimeUnit.SECONDS)
                .doOnNext(bookShelfBean -> {
                    if (bookShelfBean.realChapterListEmpty()) return;

                    List<ChapterBean> oldList = bookShelf.getChapterList();
                    List<ChapterBean> newList = bookShelfBean.getChapterList();
                    for (int i = 0, size = oldList.size(); i < size; i++) {
                        ChapterBean oldBean = oldList.get(i);
                        ChapterBean newBean = newList.get(i);
                        if (oldBean.equals(newBean)) {
                            newBean.setStart(oldBean.getStart());
                            newBean.setEnd(oldBean.getEnd());
                            newBean.setDurChapterPlayUrl(oldBean.getDurChapterPlayUrl());
                        }
                    }

                    // 存储章节到数据库
                    bookShelfBean.setHasUpdate(false);
                    bookShelfBean.setNewChapters(0);
                    bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                    bookShelfBean.setDurChapter(bookShelf.getDurChapter());
                    bookShelfBean.setDurChapterName(bookShelf.getDurChapterName());
                    if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                        BookshelfHelp.saveBookToShelf(bookShelfBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        mUpdateChaptersDisp = d;
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean.realChapterListEmpty()) {
                            onMessage("章节更新失败");
                        } else {
                            int newChapters = bookShelfBean.getChapterListSize() - bookShelf.getChapterListSize();
                            bookShelf = bookShelfBean;
                            onMessage(newChapters == 0 ? "章节更新成功，没有新增章节"
                                    : String.format(Locale.CANADA, "章节更新成功，新增%d章", newChapters));
                            BookShelfHolder.get().post(bookShelfBean);
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_SHELF, bookShelf);
                            AudioPlayInfo info = AudioPlayInfo.update(bookShelf.getChapterList());
                            info.setAction(AudioBookPlayService.ACTION_UPDATE_CHAPTER);
                            RxBus.get().post(RxBusTag.AUDIO_PLAY, info);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        onMessage("章节更新失败");
                    }
                });
    }

    private void onMessage(String msg) {
        if (mPlayCallback != null) {
            mPlayCallback.onMessage(msg);
        }
    }

    @Override
    public void addToShelf() {
        if (bookShelf != null) {
            saveBookShelf(bookShelf, true);
        }
    }

    @Override
    public boolean inBookShelf() {
        if (bookShelf == null) {
            return false;
        }
        return BookshelfHelp.isInBookShelf(bookShelf.getNoteUrl());
    }

    @Override
    public void playNext() {
        if (!isPrepared) return;

        if (hasNext()) {
            mPlayIndex += 1;
            ChapterBean chapterBean = bookShelf.getChapter(mPlayIndex);
            playChapter(chapterBean, true);
        }

    }

    @Override
    public void playPrevious() {
        if (!isPrepared) return;

        if (hasPrevious()) {
            mPlayIndex -= 1;
            ChapterBean chapterBean = bookShelf.getChapter(mPlayIndex);
            playChapter(chapterBean, true);
        }

    }

    @Override
    public boolean hasNext() {
        return mPlayIndex < bookShelf.getChapterList().size() - 1;
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
        if (bookShelf != null && !bookShelf.realChapterListEmpty()) {
            return bookShelf.getChapter(mPlayIndex);
        }
        return null;
    }

    @Override
    public void playChapter(ChapterBean chapter, boolean reset) {
        if (!isPrepared) return;

        if (mPlayCallback != null) {
            mPlayCallback.onStart();
        }

        if (disposables != null && mPlayDisposable != null) {
            disposables.remove(mPlayDisposable);
        }

        Observable.just(chapter)
                .subscribeOn(RxExecutors.getDefault())
                .map(chapterBean -> {
                    if (reset) {
                        chapterBean.setStart(0);
                    }
                    return chapterBean;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(chapterBean -> {
                    mPlayIndex = chapterBean.getDurChapterIndex();
                    bookShelf.setDurChapter(chapterBean.getDurChapterIndex());
                    bookShelf.setDurChapterName(chapterBean.getDurChapterName());
                    saveBookShelf(bookShelf);
                    if (mPlayCallback != null) {
                        mPlayCallback.onPrepare(chapter);
                    }
                })
                .observeOn(RxExecutors.getDefault())
                .flatMap((Function<ChapterBean, ObservableSource<ChapterBean>>) chapterBean -> {
                    if (!NetworkUtil.isNetworkAvailable() || !TextUtils.isEmpty(chapter.getDurChapterPlayUrl())) {
                        return Observable.just(chapterBean);
                    }
                    return WebBookModel.getInstance()
                            .getAudioBookContent(bookShelf.getBookInfoBean(), chapterBean);
                })
                .timeout(20L, TimeUnit.SECONDS)
                .retry(RETRY_COUNT)
                .flatMap((Function<ChapterBean, ObservableSource<ChapterBean>>) chapterBean -> {
                    if (TextUtils.isEmpty(chapterBean.getDurChapterPlayUrl())) {
                        return Observable.error(new NullPointerException("audio play url is null"));
                    }
                    return Observable.just(chapterBean);
                })
                .doAfterNext(chapterBean -> {
                    try {
                        bookShelf.getChapterList().set(chapterBean.getDurChapterIndex(), chapterBean);
                        if (inBookShelf()) {
                            BookshelfHelp.saveChapter(chapterBean);
                        }
                    } catch (Exception ignore) {
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<ChapterBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        ensureCompositeDisposable();
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
    public void retryPlay(int progress) {
        if (!isPrepared) return;

        ChapterBean chapterBean = getDurChapter();
        if (chapterBean != null) {
            chapterBean.setStart(progress);
            playChapter(chapterBean, false);
        } else if (mPlayCallback != null) {
            mPlayCallback.onError(new NullPointerException("current chapter is null"));
        }
    }

    @Override
    public void resetChapter() {
        if (!isPrepared) return;

        ChapterBean chapter = getDurChapter();
        if (chapter != null) {
            chapter.setDurChapterPlayUrl(null);
            playChapter(chapter, true);
        } else if (mPlayCallback != null) {
            mPlayCallback.onError(new NullPointerException("current chapter is null"));
        }
    }

    @Override
    public void saveProgress(int progress, int duration) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            if (inBookShelf()) {
                ChapterBean chapterBean = bookShelf.getChapter(mPlayIndex);
                chapterBean.setStart(progress);
                chapterBean.setEnd(duration);
                BookshelfHelp.saveChapter(chapterBean);
            }
            emitter.onNext(true);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
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
        saveBookShelf(bookShelfBean, false);
    }

    private void saveBookShelf(BookShelfBean bookShelfBean, boolean forceSave) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookShelfBean.setHasUpdate(false);
            bookShelfBean.setNewChapters(0);
            boolean inShelf = inBookShelf();
            if (forceSave || inShelf) {
                BookshelfHelp.saveBookToShelf(bookShelfBean);
            }
            emitter.onNext(forceSave && !inShelf);
            emitter.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        RxBus.get().post(value ? RxBusTag.HAD_ADD_BOOK : RxBusTag.UPDATE_BOOK_SHELF, bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private Observable<BookShelfBean> getChapterList(BookShelfBean bookShelf) {
        return WebBookModel.getInstance().getChapterList(bookShelf)
                .map(bookShelfBean -> {
                    bookShelfBean.setHasUpdate(false);
                    bookShelfBean.setNewChapters(0);
                    bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                    return bookShelfBean;
                });
    }


    private void ensureCompositeDisposable() {
        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
    }

    public void destroy() {
        disposables.dispose();
        disposables = null;
    }

}
