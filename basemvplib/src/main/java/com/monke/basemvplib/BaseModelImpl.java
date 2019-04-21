package com.monke.basemvplib;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class BaseModelImpl {


    protected <T> T createService(String url, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, tClass);
    }

    protected <T> T createService(String url, String encode, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, encode, tClass);
    }


    protected Observable<String> ajax(AjaxWebView.AjaxParams params) {
        return Observable.create(e -> AjaxWebView.ajax(params, new AjaxCallback(e)));
    }

    protected Observable<String> sniff(AjaxWebView.AjaxParams params) {
        return Observable.create(e -> AjaxWebView.sniff(params, new AjaxCallback(e)));
    }


    public static class AjaxCallback extends AjaxWebView.Callback{

        private final ObservableEmitter<String> emitter;

        private AjaxCallback(ObservableEmitter<String> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onResult(String result) {
            emitter.onNext(result);
        }

        @Override
        public void onError(Throwable error) {
            emitter.onError(error);
        }

        @Override
        public void onComplete() {
            emitter.onComplete();
        }
    }


}