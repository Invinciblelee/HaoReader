//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.content.exception.BookSourceException;
import com.monke.monkeybook.presenter.contract.ReadBookContract;
import com.monke.monkeybook.service.DownloadService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class ReadBookPresenterImpl extends BasePresenterImpl<ReadBookContract.View> implements ReadBookContract.Presenter {

    private BookShelfBean bookShelf;
    private BookSourceBean bookSource;

    private CompositeDisposable changeSourceDisp = new CompositeDisposable();

    public ReadBookPresenterImpl() {
    }

    @Override
    public void handleIntent(Intent intent) {
        boolean isRecreate = intent.getBooleanExtra("isRecreate", false);
        if (isRecreate && !intent.getBooleanExtra("fromUri", false)) {
            BitIntentDataManager dataManager = BitIntentDataManager.getInstance();
            bookShelf = dataManager.getData("bookShelf", null);
            if (bookShelf != null) {
                mView.prepareDisplay();
                prepareSync();
            } else {
                mView.finish();
            }
        } else {
            intent.putExtra("isRecreate", true).putExtra("fromUri", false);
            String key = intent.getStringExtra("data_key");
            bookShelf = BitIntentDataManager.getInstance().getData(key, null);
            BitIntentDataManager.getInstance().cleanData(key);
            if (bookShelf == null) {
                mView.finish();
            } else {
                mView.prepareDisplay();
                prepareSync();
            }
        }
    }

    @Override
    public BookSourceBean getBookSource() {
        return bookSource;
    }

    /**
     * 禁用当前书源
     */
    @Override
    public void disableDurBookSource() {
        try {
            if (!BookShelfBean.LOCAL_TAG.equals(bookShelf.getTag())) {
                BookSourceBean bookSource = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                        .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookShelf.getTag())).unique();
                bookSource.setEnable(false);
                if (TextUtils.isEmpty(bookSource.getBookSourceGroup()))
                    bookSource.setBookSourceGroup("禁用");
                mView.toast("已禁用" + bookSource.getBookSourceName());
                BookSourceManager.save(bookSource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanCache() {
        if (bookShelf == null) {
            return;
        }

        mView.showLoading("正在清除缓存");
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            BookshelfHelp.cleanBookCache(bookShelf);
            emitter.onSuccess(true);
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RxBus.get().post(RxBusTag.CLEAN_BOOK_CACHE, true);
                        mView.toast("缓存清除成功");
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("缓存清除失败");
                        mView.dismissHUD();
                    }
                });
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
                bookShelf.setFinalDate(System.currentTimeMillis());
                if (!bookShelf.realChapterListEmpty()) {
                    bookShelf.upDurChapterName();
                    bookShelf.upLastChapterName();
                }
                if (inBookShelf()) {
                    DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                }
                e.onNext(bookShelf);
                e.onComplete();
            }).subscribeOn(RxExecutors.getDefault())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean value) {
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_SHELF, bookShelf);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public String getChapterTitle(int chapterIndex) {
        if (bookShelf.getChapterListSize() == 0) {
            return mView.getContext().getString(R.string.no_chapter);
        } else
            return bookShelf.getChapter(chapterIndex).getDisplayDurChapterName();
    }

    /**
     * 下载
     */
    @Override
    public void addDownload(int start, int end) {
        addToShelf(() -> {
            DownloadBookBean downloadBook = new DownloadBookBean();
            downloadBook.setTag(bookShelf.getTag());
            downloadBook.setName(bookShelf.getBookInfoBean().getName());
            downloadBook.setNoteUrl(bookShelf.getNoteUrl());
            downloadBook.setCoverUrl(bookShelf.getBookInfoBean().getRealCoverUrl());
            downloadBook.setStart(start);
            downloadBook.setEnd(end);
            downloadBook.setFinalDate(System.currentTimeMillis());
            DownloadService.addDownload(mView.getContext(), downloadBook);
        });
    }

    /**
     * 换源
     */
    @Override
    public void changeBookSource(SearchBookBean searchBook) {
        mView.stopRefreshChapterList();
        changeSourceDisp.clear();
        BookShelfBean target = BookshelfHelp.getBookFromSearchBook(searchBook);
        target.setSerialNumber(bookShelf.getSerialNumber());
        target.setLastChapterName(bookShelf.getLastChapterName());
        target.setDurChapterName(bookShelf.getDurChapterName());
        target.setDurChapter(bookShelf.getDurChapter());
        target.setDurChapterPage(bookShelf.getDurChapterPage());
        target.setGroup(bookShelf.getGroup());
        WebBookModel.getInstance().getBookInfo(target)
                .flatMap(bookShelfBean -> WebBookModel.getInstance().getChapterList(bookShelfBean))
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bookShelfBean -> {
                    bookShelfBean.setHasUpdate(false);
                    bookShelfBean.setNewChapters(0);
                    bookShelfBean.setUpdateOff(bookShelf.getUpdateOff());
                    return bookShelfBean;
                })
                .flatMap((Function<BookShelfBean, ObservableSource<BookShelfBean>>) bookShelfBean -> {
                    if (!bookShelfBean.realChapterListEmpty()) {
                        return Observable.create(emitter -> {
                            if (inBookShelf()) {
                                BookshelfHelp.removeFromBookShelf(bookShelf);
                                BookshelfHelp.saveBookToShelf(bookShelfBean);
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            }
                            emitter.onNext(bookShelfBean);
                            emitter.onComplete();
                        });
                    }
                    return Observable.error(new Exception("目录获取失败"));
                })
                .doOnNext(bookShelfBean -> bookSource = BookSourceManager.getByUrl(bookShelfBean.getTag()))
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        changeSourceDisp.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookShelf = bookShelfBean;
                        mView.changeSourceFinish(true, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.changeSourceFinish(false, (e instanceof BookSourceException) ? e.getMessage() : null);
                    }
                });
    }

    @Override
    public void saveBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.saveBookmark(bookmarkBean);
            bookShelf.setBookmarkList(BookshelfHelp.queryBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookShelf);
            e.onComplete();
        })
                .subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        mView.updateBookmark(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void delBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.delBookmark(bookmarkBean);
            bookShelf.setBookmarkList(BookshelfHelp.queryBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookShelf);
            e.onComplete();
        })
                .subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        mView.updateBookmark(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void prepareSync() {
        if (bookShelf.isLocalBook()) {
            mView.upMenu();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                bookSource = BookSourceManager.getByUrl(bookShelf.getTag());
                e.onNext(bookSource != null);
                e.onComplete();
            }).subscribeOn(RxExecutors.getDefault())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            mView.upMenu();
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.upMenu();
                        }
                    });
        }
        RxExecutors.getDefault().createWorker().schedule(() -> {
            if (inBookShelf()) {
                ReadBookControl.getInstance().setLastNoteUrl(getBookShelf().getNoteUrl());
            }
        });
    }

    @Override
    public boolean inBookShelf() {
        if (bookShelf == null) {
            return false;
        }
        return BookshelfHelp.isInBookShelf(bookShelf.getNoteUrl());
    }

    @Override
    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void checkBookInfo() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (bookShelf.realChapterListEmpty()) {
                bookShelf.setChapterList(BookshelfHelp.queryChapterList(bookShelf.getNoteUrl()));
            }
            if (bookShelf.realBookmarkListEmpty()) {
                bookShelf.setBookmarkList(BookshelfHelp.queryBookmarkList(bookShelf.getBookInfoBean().getName()));
            }
            bookShelf.setHasUpdate(false);
            bookShelf.setNewChapters(0);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.startLoadingBook();

                        saveProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void addToShelf(final OnAddListener addListener) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.saveBookToShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(RxExecutors.getDefault())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                            if (addListener != null)
                                addListener.addSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
    }

    public interface OnAddListener {
        void addSuccess();
    }

    /////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        super.detachView();
        changeSourceDisp.dispose();
        RxBus.get().unregister(this);
    }

    /////////////////////RxBus////////////////////////

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.MEDIA_BUTTON)})
    public void onMediaButton(String command) {
        if (bookShelf != null) {
            mView.onMediaButton();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_READ)})
    public void updateRead(Boolean recreate) {
        mView.refresh(recreate);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_STATE)})
    public void upAloudState(Integer state) {
        mView.upAloudState(state);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_MSG)})
    public void showMsg(String msg) {
        mView.toast(msg);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_INDEX)})
    public void speakIndex(Integer index) {
        mView.speakIndex(index);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.ALOUD_TIMER)})
    public void upAloudTimer(String timer) {
        mView.upAloudTimer(timer);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO)})
    public void updateTitle(BookShelfBean bookShelfBean) {
        mView.updateTitle(bookShelfBean.getBookInfoBean().getName());
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SHOW_BOOKMARK)})
    public void showBookmark(BookmarkBean bookmarkBean) {
        if (bookmarkBean != null) {
            mView.showBookmark(bookmarkBean);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.OPEN_BOOKMARK)})
    public void openBookmark(BookmarkBean bookmarkBean) {
        if (bookmarkBean != null) {
            mView.openBookmark(bookmarkBean);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.OPEN_CHAPTER)})
    public void openChapter(ChapterBean chapterBean) {
        if (chapterBean != null) {
            mView.openChapter(chapterBean);
        }
    }
}
