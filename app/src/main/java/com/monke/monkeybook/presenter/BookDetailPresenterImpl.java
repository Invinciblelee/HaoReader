package com.monke.monkeybook.presenter;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.content.exception.BookSourceException;
import com.monke.monkeybook.presenter.contract.BookDetailContract;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class BookDetailPresenterImpl extends BasePresenterImpl<BookDetailContract.View> implements BookDetailContract.Presenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openFrom;
    private SearchBookBean searchBook;
    private BookShelfBean bookShelf;
    private Boolean inBookShelf = false;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void initData(Intent intent) {
        openFrom = intent.getIntExtra("openFrom", FROM_BOOKSHELF);
        if (openFrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            bookShelf = BitIntentDataManager.getInstance().getData(key, null);
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

    @Override
    public Boolean inBookShelf() {
        return bookShelf != null && inBookShelf;
    }

    @Override
    public int getOpenFrom() {
        return openFrom;
    }

    @Override
    public SearchBookBean getSearchBook() {
        return searchBook;
    }

    @Override
    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void loadBookShelfInfo(boolean refresh) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookShelfBean bookShelfBean;
            if (openFrom == FROM_BOOKSHELF) {
                bookShelfBean = BookshelfHelp.queryBookByUrl(bookShelf.getNoteUrl());
            } else {//来自搜索页面
                bookShelfBean = BookshelfHelp.queryBookByName(searchBook.getName(), searchBook.getAuthor(), searchBook.getBookType());
            }

            if (bookShelfBean != null) {
                inBookShelf = true;
                bookShelf = bookShelfBean;
            }
            e.onNext(bookShelf);
            e.onComplete();
        })
                .subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(bookShelfBean -> {
                    bookShelf = bookShelfBean;
                    mView.updateView(false);
                })
                .observeOn(RxExecutors.getDefault())
                .flatMap(bookShelfBean -> WebBookModel.getInstance().getBookInfo(bookShelfBean))
                .flatMap(bookShelfBean -> {
                    if (refresh && inBookShelf) {
                        return WebBookModel.getInstance().getChapterList(bookShelfBean);
                    }
                    return Observable.just(bookShelfBean);
                })
                .doAfterNext(bookShelfBean -> {
                    if (inBookShelf) {
                        BookshelfHelp.saveBookToShelf(bookShelfBean);
                        RxBus.get().post(RxBusTag.UPDATE_BOOK_SHELF, bookShelfBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfResult) {
                        bookShelf = bookShelfResult;
                        mView.updateView(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getBookShelfError(refresh);
                    }
                });
    }

    @Override
    public void addToBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.saveBookToShelf(bookShelf);
                inBookShelf = true;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(RxExecutors.getDefault())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                                mView.updateView(true);
                            } else {
                                mView.toast("放入书架失败");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.toast("放入书架失败");
                        }
                    });
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
                inBookShelf = false;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(RxExecutors.getDefault())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                mView.updateView(true);
                            } else {
                                mView.toast("移出书架失败");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.toast("移出书架失败");
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
                }).subscribeOn(RxExecutors.getDefault())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                RxBus.get().post(RxBusTag.UPDATE_BOOK_INFO, bookShelf);
                                mView.changeUpdateSwitch(off);
                                mView.toast(off ? "已禁用更新" : "已启用更新");
                            }

                            @Override
                            public void onError(Throwable e) {
                                mView.toast(off ? "禁用更新失败" : "启用更新失败");
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
        disposables.clear();
        BookShelfBean target = BookshelfHelp.getBookFromSearchBook(searchBookBean);
        target.setSerialNumber(bookShelf.getSerialNumber());
        target.setDurChapterName(bookShelf.getDurChapterName());
        target.setDurChapter(bookShelf.getDurChapter());
        target.setDurChapterPage(bookShelf.getDurChapterPage());
        target.setFinalDate(bookShelf.getFinalDate());
        WebBookModel.getInstance().getBookInfo(target)
                .subscribeOn(RxExecutors.getDefault())
                .flatMap(bookShelfBean -> {
                    if (inBookShelf) {
                        return WebBookModel.getInstance().getChapterList(bookShelfBean);
                    }
                    return Observable.just(bookShelfBean);
                })
                .timeout(30, TimeUnit.SECONDS)
                .map(bookShelfBean -> {
                    bookShelfBean.setGroup(bookShelf.getGroup());
                    bookShelfBean.setUpdateOff(bookShelf.getUpdateOff());
                    bookShelfBean.setNewChapters(0);
                    return bookShelfBean;
                })
                .flatMap((Function<BookShelfBean, ObservableSource<BookShelfBean>>) bookShelfBean -> Observable.create(emitter -> {
                    if (inBookShelf) {
                        BookshelfHelp.removeFromBookShelf(bookShelf);
                        BookshelfHelp.saveBookToShelf(bookShelfBean);
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf.withFlag(true));
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                        RxBus.get().post(RxBusTag.CHANGE_SOURCE, bookShelfBean);
                    }
                    emitter.onNext(bookShelfBean);
                    emitter.onComplete();
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookShelf = bookShelfBean;
                        mView.updateView(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateView(true);
                        if (e instanceof BookSourceException) {
                            mView.toast(e.getMessage());
                        } else {
                            mView.toast("书源更换失败");
                        }
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
        super.detachView();
        disposables.dispose();
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO)})
    public void updateBookInfo(BookShelfBean bookShelfBean) {
        bookShelf = bookShelfBean;
        mView.updateView(true);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addBookShelf(BookShelfBean bookShelfBean) {
        inBookShelf = true;
        bookShelf = bookShelfBean;
        mView.updateView(true);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        inBookShelf = false;
        bookShelf = bookShelfBean;
        mView.updateView(true);
    }

}
