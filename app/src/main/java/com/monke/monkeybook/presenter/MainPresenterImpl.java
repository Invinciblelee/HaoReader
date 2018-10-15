//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.DataBackup;
import com.monke.monkeybook.help.DataRestore;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<MainContract.View> implements MainContract.Presenter {
    private int threadsNum = 4;
    private int refreshIndex;
    private List<BookShelfBean> bookShelfBeans;
    private int group;
    private List<String> errBooks = new ArrayList<>();

    private CompositeDisposable refreshingDisps = new CompositeDisposable();

    @Override
    public boolean checkLocalBookExists(BookShelfBean bookShelf) {
        if (bookShelf == null) {
            return false;
        }

        if (Objects.equals(bookShelf.getTag(), BookShelfBean.LOCAL_TAG)) {
            File bookFile = new File(bookShelf.getNoteUrl());
            return bookFile.exists();
        } else {
            return true;
        }
    }

    @Override
    public void queryBookShelf(boolean needRefresh, boolean needAnim, int group) {
        this.group = group;
        if (needRefresh) {
            errBooks.clear();
        }
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            List<BookShelfBean> bookShelfList = BookshelfHelp.getBooksByGroup(group);
            e.onNext(bookShelfList == null ? new ArrayList<>() : bookShelfList);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (null != value) {
                            bookShelfBeans = value;
                            mView.refreshBookShelf(group, bookShelfBeans);
                            if (needAnim) {
                                mView.startLayoutAnimation();
                            }
                            if (needRefresh) {
                                startRefreshBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    @Override
    public void backupData() {
        DataBackup.getInstance().run();
        mView.dismissHUD();
    }

    @Override
    public void restoreData() {
        mView.onRestore(mView.getContext().getString(R.string.on_restore));
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (DataRestore.getInstance().run()) {
                e.onNext(true);
            } else {
                e.onNext(false);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            mView.restoreSuccess();
                            queryBookShelf(true, true, group);
                            Toast.makeText(mView.getContext(), R.string.restore_success, Toast.LENGTH_LONG).show();
                        } else {
                            mView.dismissHUD();
                            Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.dismissHUD();
                        Toast.makeText(mView.getContext(), R.string.restore_fail, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void addBookUrl(String bookUrl) {
        if (TextUtils.isEmpty(bookUrl.trim())) return;
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            URL url = new URL(bookUrl);
            BookInfoBean temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookUrl)).limit(1).build().unique();
            if (temp != null) {
                e.onNext(null);
            } else {
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(String.format("%s://%s", url.getProtocol(), url.getHost()));
                bookShelfBean.setNoteUrl(url.toString());
                bookShelfBean.setDurChapter(0);
                bookShelfBean.setDurChapterPage(0);
                bookShelfBean.setFinalDate(System.currentTimeMillis());
                e.onNext(bookShelfBean);
            }
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        if (bookShelfBean != null) {
                            getBook(bookShelfBean);
                        } else {
                            Toast.makeText(mView.getContext(), "已在书架中", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "网址格式不对", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void clearBookshelf() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookshelfHelp.clearBookshelf();
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        queryBookShelf(false, false, group);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "书架清空失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void clearCaches() {
        mView.showLoading("正在清除缓存");
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookshelfHelp.clearCaches();
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        Toast.makeText(mView.getContext(), "缓存清除成功", Toast.LENGTH_SHORT).show();
                        mView.dismissHUD();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "缓存清除失败", Toast.LENGTH_SHORT).show();
                        mView.dismissHUD();
                    }
                });
    }

    @Override
    public void removeFromBookSelf(BookShelfBean bookShelf) {
        if (bookShelf != null) {
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookshelfHelp.removeFromBookShelf(bookShelf);
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
                            } else {
                                Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModelImpl.getInstance()
                .getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .flatMap(this::saveBookToShelfO)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        if (value.getBookInfoBean().getChapterUrl() == null) {
                            Toast.makeText(mView.getContext(), "添加书籍失败", Toast.LENGTH_SHORT).show();
                        } else {
                            //成功   //发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            Toast.makeText(mView.getContext(), "添加书籍成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "添加书籍失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startRefreshBook() {
        if (bookShelfBeans != null && !bookShelfBeans.isEmpty()) {
            if (Objects.equals(bookShelfBeans.get(0).getTag(), BookShelfBean.LOCAL_TAG)) {
                return;
            }

            refreshingDisps.clear();

            threadsNum = mView.getPreferences().getInt(mView.getContext().getString(R.string.pk_threads_num), 4);
            refreshIndex = -1;
            for (int i = 0; i < threadsNum; i++) {
                refreshBookshelf();
            }
        }
    }

    private synchronized void refreshBookshelf() {
        refreshIndex++;
        if (refreshIndex < bookShelfBeans.size()) {
            BookShelfBean bookShelfBean = bookShelfBeans.get(refreshIndex);
            bookShelfBean.setLoading(true);
            mView.updateBook(bookShelfBean, false);
            WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
                    .flatMap(this::saveBookToShelfO)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<BookShelfBean>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            refreshingDisps.add(d);
                        }

                        @Override
                        public void onNext(BookShelfBean value) {
                            if (value.getErrorMsg() != null) {
                                Toast.makeText(mView.getContext(), value.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                value.setErrorMsg(null);
                            }
                            bookShelfBean.setLoading(false);
                            mView.updateBook(bookShelfBean, false);
                            refreshBookshelf();
                        }

                        @Override
                        public void onError(Throwable e) {
                            errBooks.add(bookShelfBean.getBookInfoBean().getName());
                            bookShelfBean.setLoading(false);
                            mView.updateBook(bookShelfBean, false);
                            refreshBookshelf();
                        }
                    });
        } else if (refreshIndex >= bookShelfBeans.size() + threadsNum - 1) {
            if (errBooks.size() > 0) {
                Toast.makeText(mView.getContext(), TextUtils.join("、", errBooks) + " 更新失败！", Toast.LENGTH_SHORT).show();
                errBooks.clear();
            }
            mView.sortBookShelf();
        }
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            bookShelfBean.setLoading(false);
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
    }

    @Override
    public void detachView() {
        refreshingDisps.dispose();
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_ADD_BOOK)})
    public void addToBookShelf(BookShelfBean bookShelfBean) {
        mView.addToBookShelf(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeFromBookShelf(BookShelfBean bookShelfBean) {
        mView.removeFromBookShelf(bookShelfBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO), @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)})
    public void hadUpdateBook(BookShelfBean bookShelfBean) {
        mView.updateBook(bookShelfBean, true);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

}
