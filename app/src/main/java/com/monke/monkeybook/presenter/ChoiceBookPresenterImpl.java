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
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;

import java.util.Iterator;
import java.util.List;

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
        WebBookModel.getInstance().findBook(tag, url, page)
                .subscribeOn(Schedulers.single())
                .map(searchBookBeans -> {
                    Iterator<SearchBookBean> iterator = searchBookBeans.iterator();
                    while (iterator.hasNext()) {
                        SearchBookBean searchBook = iterator.next();
                        for (SearchBookBean temp : mView.getSearchBookAdapter().getSearchBooks()) {
                            if (TextUtils.equals(temp.getRealNoteUrl(), searchBook.getRealNoteUrl())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                    return searchBookBeans;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> value) {
                        if (searchTime == startThisSearchTime) {
                            if (page == 1) {
                                mView.refreshSearchBook(value);
                                mView.refreshFinish(value.isEmpty());
                            } else {
                                mView.loadMoreSearchBook(value);
                            }
                            page++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.searchBookError();
                    }
                });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getTitle() {
        return title;
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

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

}