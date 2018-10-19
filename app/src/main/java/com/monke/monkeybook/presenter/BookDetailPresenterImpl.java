package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.BookDetailContract;
import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookDetailPresenterImpl extends BasePresenterImpl<BookDetailContract.View> implements BookDetailContract.Presenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openFrom;
    private SearchBookBean searchBook;
    private BookShelfBean bookShelf;
    private Boolean inBookShelf = false;

    private CompositeDisposable changeSourceDisp = new CompositeDisposable();

    @Override
    public void initData(Intent intent) {
        openFrom = intent.getIntExtra("openFrom", FROM_BOOKSHELF);
        if (openFrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            if (bookShelf == null) {
                mView.finish();
                return;
            }
            inBookShelf = true;
            searchBook = new SearchBookBean();
            searchBook.setNoteUrl(bookShelf.getNoteUrl());
            searchBook.setTag(bookShelf.getTag());
        } else {
            initBookFormSearch(intent.getParcelableExtra("data"));
        }
    }

    @Override
    public void initBookFormSearch(SearchBookBean searchBookBean) {
        if (searchBookBean == null) {
            mView.finish();
            return;
        }
        searchBook = searchBookBean;
        bookShelf = BookshelfHelp.getBookFromSearchBook(searchBookBean);
    }

    public Boolean getInBookShelf() {
        return bookShelf != null && inBookShelf;
    }

    public int getOpenFrom() {
        return openFrom;
    }

    public SearchBookBean getSearchBook() {
        return searchBook;
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void getBookShelfInfo() {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookShelfBean bookShelfBean;
            if (openFrom == FROM_BOOKSHELF) {
                bookShelfBean = BookshelfHelp.getBookByUrl(bookShelf.getNoteUrl());
            } else {//来自搜索页面
                bookShelfBean = BookshelfHelp.getBookByName(searchBook.getName(), searchBook.getAuthor());
            }
            if (bookShelfBean != null) {
                inBookShelf = true;
                bookShelf = bookShelfBean;
            }
            e.onNext(bookShelf);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(bookShelfBean -> WebBookModelImpl.getInstance().getBookInfo(bookShelfBean))
                .flatMap(bookShelfBean -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean))
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfResult) {
                        bookShelf = bookShelfResult;
                        mView.updateView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.getBookShelfError();
                    }
                });
    }

    @Override
    public void addToBookShelf(int group) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                bookShelf.setGroup(group);
                BookshelfHelp.saveBookToShelf(bookShelf);
                inBookShelf = true;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                                Toast.makeText(MApplication.getInstance(), group == 0 ? "已加入追更" : group == 1 ? "已加入养肥" : "已加入收藏", Toast.LENGTH_SHORT).show();
                                mView.updateView();
                            } else {
                                Toast.makeText(MApplication.getInstance(), group == 0 ? "加入追更失败" : group == 1 ? "加入养肥失败" : "加入收藏失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MApplication.getInstance(), group == 0 ? "加入追更失败" : group == 1 ? "加入养肥失败" : "加入收藏失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (bookShelf != null) {
            int group = bookShelf.getGroup();
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
                inBookShelf = false;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                Toast.makeText(MApplication.getInstance(), group == 0 ? "已移出追更" : group == 1 ? "已移出养肥" : "已取消收藏", Toast.LENGTH_SHORT).show();
                                mView.updateView();
                            } else {
                                Toast.makeText(MApplication.getInstance(), group == 0 ? "移出追更失败" : group == 1 ? "移出养肥失败" : "取消收藏失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MApplication.getInstance(), group == 0 ? "移出追更失败" : group == 1 ? "移出养肥失败" : "取消收藏失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void switchUpdate(boolean off) {
        if (bookShelf != null) {
            if (inBookShelf) {
                Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                    bookShelf.setUpdateOff(off);
                    BookshelfHelp.saveBookToShelf(bookShelf);
                    e.onNext(true);
                    e.onComplete();
                }).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                RxBus.get().post(RxBusTag.UPDATE_BOOK_INFO, bookShelf);
                                mView.changeUpdateSwitch(off);
                                Toast.makeText(MApplication.getInstance(), off ? "已禁用更新" : "已启用更新", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(MApplication.getInstance(), "操作失败", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                bookShelf.setUpdateOff(off);
                mView.changeUpdateSwitch(off);
            }
        }
    }

    /**
     * 换源
     */
    @Override
    public void changeBookSource(SearchBookBean searchBookBean) {
        changeSourceDisp.clear();
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBookBean);
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
                        saveChangedBook(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateView();
                        Toast.makeText(MApplication.getInstance(), "书源更换失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveChangedBook(BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookshelfHelp.removeFromBookShelf(bookShelf);
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
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
                        mView.updateView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateView();
                        Toast.makeText(MApplication.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO)})
    public void updateBookInfo(BookShelfBean bookShelfBean) {
        bookShelf = bookShelfBean;
        mView.updateView();
    }

}
