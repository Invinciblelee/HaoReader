package com.monke.basemvplib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class BaseModelImpl {


    protected <T> T createService(String url, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, tClass);
    }

    protected <T> T createService(String url, String encode, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, encode, tClass);
    }


    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    protected Observable<String> getAjaxHtml(Context context, String url, String userAgent) {
        return Observable.create(e -> {
            WebView webView = new WebView(context);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUserAgentString(userAgent);
            webView.getSettings().setBlockNetworkImage(true);
            webView.addJavascriptInterface(new MyJavaScriptInterface(webView, e), "HTMLOUT");
            webView.setWebViewClient(new MyWebViewClient(e));
            webView.loadUrl(url);
        });
    }

    private static class MyJavaScriptInterface {
        private ObservableEmitter<String> emitter;
        private WeakReference<WebView> webViewRef;

        private MyJavaScriptInterface(WebView webView, ObservableEmitter<String> emitter) {
            this.webViewRef = new WeakReference<>(webView);
            this.emitter = emitter;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            if (!emitter.isDisposed()) {
                emitter.onNext(html);
                emitter.onComplete();
            }

            WebView webView = webViewRef.get();
            if (webView != null) {
                new Handler(Looper.getMainLooper()).post(webView::destroy);
            }
        }
    }

    private static class MyWebViewClient extends WebViewClient {

        private ObservableEmitter<String> emitter;

        private MyWebViewClient(ObservableEmitter<String> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(description));
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(error.getDescription().toString()));
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }
}