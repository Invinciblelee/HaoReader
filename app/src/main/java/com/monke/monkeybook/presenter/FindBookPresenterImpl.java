//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.FindBookContract;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FindBookPresenterImpl extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {

    private final Collator collator = Collator.getInstance(java.util.Locale.CHINA);

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void initData() {
        Observable.create((ObservableOnSubscribe<List<FindKindGroupBean>>) e -> {
            List<BookSourceBean> bookSourceBeans;
            if (AppConfigHelper.get().getBoolean(mView.getContext().getString(R.string.pk_show_all_find), true)) {
                bookSourceBeans = BookSourceManager.getInstance().getAllBookSource();
            } else {
                bookSourceBeans = BookSourceManager.getInstance().getSelectedBookSource();
            }
            final List<FindKindGroupBean> group = new ArrayList<>();
            for (BookSourceBean sourceBean : bookSourceBeans) {
                try {
                    if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                        String[] kindA = sourceBean.getRuleFindUrl().split("(&&|\n)+");
                        List<FindKindBean> children = new ArrayList<>();
                        for (String kindB : kindA) {
                            if (kindB.trim().isEmpty()) continue;
                            String[] kind = kindB.split("::");
                            FindKindBean findKindBean = new FindKindBean();
                            findKindBean.setGroup(sourceBean.getBookSourceName());
                            findKindBean.setTag(sourceBean.getBookSourceUrl());
                            findKindBean.setKindName(kind[0]);
                            findKindBean.setKindUrl(kind[1]);
                            children.add(findKindBean);
                        }
                        FindKindGroupBean groupBean = new FindKindGroupBean();
                        groupBean.setGroupName(sourceBean.getBookSourceName());
                        groupBean.setTag(sourceBean.getBookSourceUrl());
                        groupBean.setChildrenCount(children.size());
                        groupBean.setChildren(children);
                        group.add(groupBean);
                    }
                } catch (Exception ignore) {
                    sourceBean.setBookSourceGroup("发现规则语法错误");
                    BookSourceManager.getInstance().saveBookSource(sourceBean);
                }
            }
            Collections.sort(group, (o1, o2) -> collator.compare(o1.getGroupName(), o2.getGroupName()));
            e.onNext(group);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<FindKindGroupBean>>() {
                    @Override
                    public void onNext(List<FindKindGroupBean> value) {
                        //执行刷新界面
                        mView.updateUI(value);
                        mView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.hideProgress();
                    }
                });
    }
}