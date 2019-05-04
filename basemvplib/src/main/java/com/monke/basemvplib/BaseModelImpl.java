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
        return Observable.create(emitter -> {
            final AjaxWebView webView = new AjaxWebView();
            final AjaxWebView.Callback callback = new AjaxCallback(emitter);
            webView.ajax(params, callback);
        });
    }

    protected Observable<String> sniff(AjaxWebView.AjaxParams params) {
        return Observable.create(emitter -> {
            final AjaxWebView webView = new AjaxWebView();
            final AjaxWebView.Callback callback = new AjaxCallback(emitter);
            webView.sniff(params, callback);
        });
    }


    public static class AjaxCallback extends AjaxWebView.Callback {

        private final ObservableEmitter<String> emitter;

        private AjaxCallback(ObservableEmitter<String> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onResult(String result) {
            if (!emitter.isDisposed()) {
                emitter.onNext(result);
            }
        }

        @Override
        public void onError(Throwable error) {
            if (!emitter.isDisposed()) {
                emitter.onError(error);
            }
        }

        @Override
        public void onComplete() {
            if (!emitter.isDisposed()) {
                emitter.onComplete();
            }
        }
    }


}