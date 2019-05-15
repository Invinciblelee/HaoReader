//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.DataBackup;
import com.monke.monkeybook.help.DataRestore;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<MainContract.View> implements MainContract.Presenter {

    @Override
    public boolean checkLocalBookNotExists(BookShelfBean bookShelf) {
        if (!bookShelf.isLocalBook()) {
            return false;
        }

        File bookFile = new File(bookShelf.getNoteUrl());
        return !bookFile.exists();
    }

    @Override
    public void queryBooks(String query) {
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfBeans = BookshelfHelp.queryBooks(query);
            e.onNext(bookShelfBeans == null ? new ArrayList<>() : bookShelfBeans);
            e.onComplete();
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> bookShelfBeans) {
                        mView.showQueryBooks(bookShelfBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void backupData() {
        mView.showLoading(mView.getContext().getString(R.string.on_backup));
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (DataBackup.getInstance().run()) {
                e.onNext(true);
            } else {
                e.onNext(false);
            }
            e.onComplete();
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            mView.showSnackBar(R.string.backup_success);
                        } else {
                            mView.showSnackBar(R.string.backup_fail);
                        }
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar(R.string.backup_fail);
                        mView.dismissHUD();
                    }
                });

    }

    @Override
    public void restoreData() {
        mView.showLoading(mView.getContext().getString(R.string.on_restore));
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (DataRestore.getInstance().run()) {
                e.onNext(true);
            } else {
                e.onNext(false);
            }
            e.onComplete();
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            mView.restoreSuccess();
                            mView.showSnackBar(R.string.restore_success);
                        } else {
                            mView.dismissHUD();
                            mView.showSnackBar(R.string.restore_fail);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.dismissHUD();
                        mView.showSnackBar(R.string.restore_fail);
                    }
                });
    }

    @Override
    public void addBookUrl(String bookUrl) {
        if (StringUtils.isBlank(bookUrl)) return;
        mView.showLoading("正在添加书籍");
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            BookInfoBean temp = DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookUrl)).build().unique();
            if (temp != null) {
                //onNext不能为null
                e.onNext(new BookShelfBean());
            } else {
                URL url = new URL(bookUrl);
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setGroup(0);
                bookShelfBean.setTag(String.format("%s://%s", url.getProtocol(), url.getHost()));
                bookShelfBean.setNoteUrl(url.toString());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                e.onNext(bookShelfBean);
            }
            e.onComplete();
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean.getTag() != null) {
                            getBook(bookShelfBean);
                        } else {
                            mView.dismissHUD();
                            mView.showSnackBar("该书已在书架中");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.dismissHUD();
                        mView.showSnackBar("书籍添加失败");
                    }
                });
    }

    @Override
    public void removeFromBookShelf(BookShelfBean bookShelf) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
                e.onNext(true);
                e.onComplete();
            }).compose(RxUtils::toSimpleSingle)
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            } else {
                                mView.showSnackBar("移出书架失败");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.showSnackBar("移出书架失败");
                        }
                    });
        }
    }

    @Override
    public void clearBookshelf() {
        mView.showLoading("正在清空书架");
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookshelfHelp.clearBookshelf();
            e.onNext(true);
            e.onComplete();
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.clearBookshelf();
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar("书架清空失败");
                        mView.dismissHUD();
                    }
                });
    }

    @Override
    public void cleanCaches() {
        mView.showLoading("正在清除缓存");
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookshelfHelp.cleanCaches();
            e.onNext(true);
            e.onComplete();
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.showSnackBar("缓存清除成功");
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar("缓存清除失败");
                        mView.dismissHUD();
                    }
                });
    }

    @Override
    public void importBooks(List<String> books) {
        mView.showLoading("正在导入书籍");
        Observable.fromIterable(books)
                .subscribeOn(Schedulers.single())
                .map(File::new)
                .flatMap(file -> ImportBookModelImpl.getInstance().importBook(file))
                .flatMap((Function<LocBookShelfBean, ObservableSource<LocBookShelfBean>>) locBookShelfBean -> {
                    if (locBookShelfBean.getNew()) {
                        BookshelfHelp.saveBookToShelf(locBookShelfBean.getBookShelfBean());
                    }
                    return Observable.just(locBookShelfBean);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                    @Override
                    public void onNext(LocBookShelfBean value) {
                        if (value.getNew()) {
                            mView.addSuccess(value.getBookShelfBean());
                        }
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar(e.getMessage());
                        mView.dismissHUD();
                    }
                });
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModel.getInstance()
                .getBookInfo(bookShelfBean)
                .subscribeOn(Schedulers.single())
                .flatMap(bookShelfBean1 -> WebBookModel.getInstance().getChapterList(bookShelfBean1))
                .flatMap(this::saveBookToShelfO)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        mView.dismissHUD();
                        if (value.getBookInfoBean().getChapterListUrl() == null) {
                            mView.showSnackBar("添加书籍失败");
                        } else {
                            //成功   //发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                            mView.showSnackBar("添加书籍成功");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.dismissHUD();
                        mView.showSnackBar("添加书籍失败");
                    }
                });
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
        AudioBookPresenterImpl.setHasUpdated(false);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addBookShelf(BookShelfBean bookShelfBean) {
        mView.addBookShelf(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeFromBook(BookShelfBean bookShelfBean) {
        mView.removeBookShelf(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO), @Tag(RxBusTag.UPDATE_BOOK_SHELF)})
    public void hadUpdateBook(BookShelfBean bookShelfBean) {
        mView.updateBook(bookShelfBean, true);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

}
