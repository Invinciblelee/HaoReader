package com.monke.monkeybook.base.observer;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public abstract class SimpleSingleObserver<T> implements SingleObserver<T> {
    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSuccess(T t) {

    }

    @Override
    public void onError(Throwable e) {

    }
}
