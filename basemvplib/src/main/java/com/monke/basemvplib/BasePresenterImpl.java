package com.monke.basemvplib;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.basemvplib.rxjava.RxExecutors;

public abstract class BasePresenterImpl<T extends IView> implements IPresenter {

    protected T mView;

    @SuppressWarnings(value = "unchecked")
    @Override
    public void attachView(@NonNull IView iView) {
        mView = (T) iView;
    }


    @CallSuper
    @Override
    public void detachView() {
        RxExecutors.setDefault(null);
    }

}
