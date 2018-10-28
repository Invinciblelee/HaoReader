//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.presenter.contract.ImportBookContract;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ImportBookPresenterImpl extends BasePresenterImpl<ImportBookContract.View> implements ImportBookContract.Presenter {

    @Override
    public void importBooks(List<File> books){
        mView.showLoading("正在导入书籍");
        Observable.fromIterable(books)
                .subscribeOn(Schedulers.io())
                .flatMap(file -> ImportBookModelImpl.getInstance().importBook(file))
                .flatMap((Function<LocBookShelfBean, ObservableSource<LocBookShelfBean>>) locBookShelfBean -> {
                    if(locBookShelfBean.getNew()){
                        BookshelfHelp.saveBookToShelf(locBookShelfBean.getBookShelfBean());
                    }
                    return Observable.just(locBookShelfBean);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                    @Override
                    public void onNext(LocBookShelfBean value) {
                        if(value.getNew()){
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK,value.getBookShelfBean());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.addError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mView.addSuccess();
                    }
                });
    }


    @Override
    public void detachView() {

    }
}
