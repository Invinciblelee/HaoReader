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

    private CompositeDisposable disposables = new CompositeDisposable();

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
                .flatMap(bookShelfBean -> {
                    if (inBookShelf && bookShelfBean.getHasUpdate()) {
                        BookshelfHelp.saveBookToShelf(bookShelfBean);
                        RxBus.get().post(RxBusTag.UPDATE_BOOK_SHELF, bookShelfBean);
                    }
                    return Observable.just(bookShelfBean);
                })
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

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
    public void addToBookShelf() {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.saveBookToShelf(bookShelf);
                inBookShelf = true;
                e.onNext(true);
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                                mView.updateView();
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
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                                mView.updateView();
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
                }).subscribeOn(Schedulers.io())
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
        disposables.clear();
        BookShelfBean bookShelfBean = BookshelfHelp.getBookFromSearchBook(searchBookBean);
        bookShelfBean.setSerialNumber(bookShelf.getSerialNumber());
        bookShelfBean.setLastChapterName(bookShelf.getLastChapterName());
        bookShelfBean.setDurChapterName(bookShelf.getDurChapterName());
        bookShelfBean.setDurChapter(bookShelf.getDurChapter());
        bookShelfBean.setDurChapterPage(bookShelf.getDurChapterPage());
        bookShelfBean.setGroup(bookShelf.getGroup());
        bookShelfBean.setFinalDate(bookShelf.getFinalDate());
        WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (inBookShelf) {
                            saveChangedBook(bookShelfBean);
                        } else {
                            bookShelf = bookShelfBean;
                            mView.updateView();
                        }
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                        bookShelf = bookShelfBean;
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
        disposables.dispose();
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO)})
    public void updateBookInfo(BookShelfBean bookShelfBean) {
        bookShelf = bookShelfBean;
        mView.updateView();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addBookShelf(BookShelfBean bookShelfBean) {
        inBookShelf = true;
        bookShelf = bookShelfBean;
        mView.updateView();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        inBookShelf = false;
        bookShelf = bookShelfBean;
        mView.updateView();
    }

}
