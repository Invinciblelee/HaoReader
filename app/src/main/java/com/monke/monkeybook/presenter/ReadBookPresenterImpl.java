//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.BookShelfDataHolder;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.source.My716;
import com.monke.monkeybook.presenter.contract.ReadBookContract;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.widget.page.PageLoader;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReadBookPresenterImpl extends BasePresenterImpl<ReadBookContract.View> implements ReadBookContract.Presenter {
    private final int ADD = 1;
    private final int REMOVE = 2;
    private final int CHECK = 3;

    private boolean isRecreate;

    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private boolean inBookShelf;
    private boolean openFromUri;
    private BookShelfBean bookShelf;

    private ExecutorService executor;
    private Scheduler scheduler;

    private List<String> downloadingChapterList = new ArrayList<>();

    private CompositeDisposable changeSourceDisp = new CompositeDisposable();

    public ReadBookPresenterImpl() {
        executor = Executors.newFixedThreadPool(6);
        scheduler = Schedulers.from(executor);
    }

    @Override
    public void prepare(Activity activity) {
        Intent intent = activity.getIntent();
        openFromUri = intent.getBooleanExtra("openFromUri", false);
        isRecreate = intent.getBooleanExtra("isRecreate", false);
        intent.putExtra("isRecreate", true);
        if (isRecreate && !openFromUri) {
            BookShelfDataHolder holder = BookShelfDataHolder.getInstance();
            bookShelf = holder.getBookShelf();
            if (bookShelf != null) {
                inBookShelf = holder.isInBookShelf();
                mView.prepareDisplay(false);
            } else {
                mView.finish();
            }
        } else {
            inBookShelf = intent.getBooleanExtra("inBookShelf", true);
            if (bookShelf == null) {
                String key = intent.getStringExtra("data_key");
                bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
                BitIntentDataManager.getInstance().cleanData(key);
            }
            if (bookShelf == null) {
                bookShelf = BookshelfHelp.getBookByUrl(mView.getNoteUrl());
            }
            if (bookShelf != null) {
                readBookControl.setLastNoteUrl(bookShelf.getNoteUrl());
                mView.updateTitle(bookShelf.getBookInfoBean().getName());
                mView.prepareDisplay(true);
            } else {
                mView.finish();
            }
        }
        mView.showHideView();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public synchronized void loadContent(final int chapterIndex) {
        if (NetworkUtil.isNetworkAvailable() && null != bookShelf && bookShelf.getChapterListSize() > 0) {
            Observable.create((ObservableOnSubscribe<Integer>) e -> {
                if (!BookshelfHelp.isChapterCached(BookshelfHelp.getCachePathName(bookShelf.getBookInfoBean()),
                        BookshelfHelp.getCacheFileName(chapterIndex, bookShelf.getChapter(chapterIndex).getDurChapterName()))
                        && !DownloadingList(CHECK, bookShelf.getChapter(chapterIndex).getDurChapterUrl())) {
                    DownloadingList(ADD, bookShelf.getChapter(chapterIndex).getDurChapterUrl());
                    e.onNext(chapterIndex);
                }
                e.onComplete();
            })
                    .flatMap(index -> WebBookModelImpl.getInstance().getBookContent(scheduler, bookShelf.getChapter(chapterIndex)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .timeout(20, TimeUnit.SECONDS)
                    .subscribe(new SimpleObserver<BookContentBean>() {

                        @Override
                        public void onNext(BookContentBean bookContentBean) {
                            DownloadingList(REMOVE, bookContentBean.getDurChapterUrl());
                            if (chapterIndex == mView.getCurChapterPos()) {//防止跳章
                                mView.finishContent();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            DownloadingList(REMOVE, bookShelf.getChapter(chapterIndex).getDurChapterUrl());
                            mView.chapterError(chapterIndex, NetworkUtil.isNetworkAvailable() ? PageLoader.STATUS_UNKNOWN_ERROR :
                                    PageLoader.STATUS_NETWORK_ERROR);
                        }
                    });
        }
    }

    /**
     * 禁用当前书源
     */
    @Override
    public void disableDurBookSource() {
        try {
            switch (bookShelf.getTag()) {
                case BookShelfBean.LOCAL_TAG:
                    break;
                case My716.TAG:
                    ACache.get(mView.getContext()).put("useMy716", "False");
                    mView.toast("已禁用My716书源");
                    break;
                default:
                    BookSourceBean bookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                            .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookShelf.getTag())).unique();
                    bookSource.setEnable(false);
                    if (TextUtils.isEmpty(bookSource.getBookSourceGroup()))
                        bookSource.setBookSourceGroup("禁用");
                    mView.toast("已禁用" + bookSource.getBookSourceName());
                    BookshelfHelp.saveBookSource(bookSource);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanCache() {
        if(bookShelf != null){
            mView.showLoading("正在清除缓存");
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                BookshelfHelp.cleanBookCache(bookShelf);
                emitter.onNext(true);
                emitter.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            mView.dismissHUD();
                            mView.toast("缓存清除成功");
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.dismissHUD();
                            mView.toast("缓存清除失败");
                        }
                    });
        }
    }

    /**
     * 编辑下载列表
     */
    private synchronized boolean DownloadingList(int editType, String value) {
        if (editType == ADD) {
            downloadingChapterList.add(value);
            return true;
        } else if (editType == REMOVE) {
            downloadingChapterList.remove(value);
            return true;
        } else {
            return downloadingChapterList.indexOf(value) != -1;
        }
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null && inBookShelf) {
            Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
                bookShelf.setFinalDate(System.currentTimeMillis());
                if(!bookShelf.realChapterListEmpty()) {
                    bookShelf.upDurChapterName();
                    bookShelf.upLastChapterName();
                }
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                e.onNext(bookShelf);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean value) {
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_PROGRESS, bookShelf);
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
            return bookShelf.getChapter(chapterIndex).getDurChapterName();
    }

    /**
     * 下载
     */
    @Override
    public void addDownload(int start, int end) {
        addToShelf(() -> {
            DownloadBookBean downloadBook = new DownloadBookBean();
            downloadBook.setName(bookShelf.getBookInfoBean().getName());
            downloadBook.setNoteUrl(bookShelf.getNoteUrl());
            downloadBook.setCoverUrl(bookShelf.getBookInfoBean().getCoverUrl());
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
        changeSourceDisp.clear();
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBook);
        bookShelfBean.setSerialNumber(bookShelf.getSerialNumber());
        bookShelfBean.setLastChapterName(bookShelf.getLastChapterName());
        bookShelfBean.setDurChapterName(bookShelf.getDurChapterName());
        bookShelfBean.setDurChapter(bookShelf.getDurChapter());
        bookShelfBean.setDurChapterPage(bookShelf.getDurChapterPage());
        bookShelfBean.setGroup(bookShelf.getGroup());
        WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        changeSourceDisp.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookShelfBean.setHasUpdate(false);

                        if (inBookShelf) {
                            saveChangedBook(bookShelfBean);
                        } else {
                            bookShelf = bookShelfBean;
                            mView.changeSourceFinish(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.changeSourceFinish(false);
                    }
                });
    }

    @Override
    public void saveBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookmarkBean>) e -> {
            BookshelfHelp.saveBookmark(bookmarkBean);
            bookShelf.setBookmarkList(BookshelfHelp.getBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookmarkBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delBookmark(BookmarkBean bookmarkBean) {
        Observable.create((ObservableOnSubscribe<BookmarkBean>) e -> {
            BookshelfHelp.delBookmark(bookmarkBean);
            bookShelf.setBookmarkList(BookshelfHelp.getBookmarkList(bookmarkBean.getBookName()));
            e.onNext(bookmarkBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 保存换源后book
     */
    private void saveChangedBook(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.removeFromBookShelf(bookShelf);
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        changeSourceDisp.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean value) {
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                        bookShelf = value;
                        mView.changeSourceFinish(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.changeSourceFinish(false);
                    }
                });
    }

    public boolean isOpenFromUri() {
        return openFromUri;
    }

    @Override
    public boolean inBookShelf() {
        return inBookShelf;
    }

    @Override
    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public boolean isRecreate() {
        return isRecreate;
    }

    @Override
    public void checkBookInfo() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (bookShelf.realChapterListEmpty()) {
                bookShelf.setChapterList(BookshelfHelp.getChapterList(bookShelf.getNoteUrl()));
                if(!bookShelf.realChapterListEmpty()){
                    bookShelf.upChapterListSize();
                }
            }
            if (bookShelf.realBookmarkListEmpty()) {
                bookShelf.setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
            }
            bookShelf.setHasUpdate(false);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.startLoadingBook();

                        if (value) {
                            saveProgress();
                        }
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
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                            inBookShelf = true;
                            if (addListener != null)
                                addListener.addSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
    }

    @Override
    public void removeFromShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            inBookShelf = aBoolean;
                            mView.finish();
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
        executor.shutdown();
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
}
