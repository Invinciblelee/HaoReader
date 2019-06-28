//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;

import java.util.Iterator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChoiceBookPresenterImpl extends BasePresenterImpl<ChoiceBookContract.View> implements ChoiceBookContract.Presenter {
    private String tag;
    private String url;

    private int page = 1;
    private long startThisSearchTime;

    public ChoiceBookPresenterImpl(final Bundle args) {
        if(args != null){
            url = args.getString("url");
            tag = args.getString("tag");
        }
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
                    if(page == 1) return searchBookBeans;
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
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {
    }

}