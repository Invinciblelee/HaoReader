//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChoiceBookPresenterImpl extends BasePresenterImpl<ChoiceBookContract.View> implements ChoiceBookContract.Presenter {
    private String tag;
    private String url;
    private String title;

    private int page = 1;
    private long startThisSearchTime;

    public ChoiceBookPresenterImpl(final Intent intent) {
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        tag = intent.getStringExtra("tag");
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void initPage() {
        this.page = 1;
        this.startThisSearchTime = System.currentTimeMillis();
    }

    @Override
    public void toSearchBooks(String key) {
        final long tempTime = startThisSearchTime;
        searchBook(tempTime);
    }

    private void searchBook(final long searchTime) {
        WebBookModelImpl.getInstance().findBook(url, page, tag)
                .subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> value) {
                        if (searchTime == startThisSearchTime) {
                            if (page == 1) {
                                mView.refreshSearchBook(value);
                                mView.refreshFinish(value.size() <= 0);
                            } else {
                                mView.loadMoreSearchBook(value);
                            }
                            page++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.searchBookError();
                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookToShelf(final SearchBookBean searchBookBean) {
        final BookShelfBean bookShelfResult = new BookShelfBean();
        bookShelfResult.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfResult.setFinalDate(System.currentTimeMillis());
        bookShelfResult.setDurChapter(0);
        bookShelfResult.setDurChapterPage(0);
        bookShelfResult.setTag(searchBookBean.getTag());
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult)
                .flatMap(bookShelfBean1 -> WebBookModelImpl.getInstance().getChapterList(bookShelfBean1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfResult) {
                        saveBookToShelf(bookShelfResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(e.getMessage());
                    }
                });
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void saveBookToShelf(final BookShelfBean bookShelfBean) {
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
            DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
            //网络数据获取成功  存入BookShelf表数据库
            DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            e.onNext(bookShelfBean);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        //成功   //发送RxBus
                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.addBookShelfFailed(e.getMessage());
                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

}