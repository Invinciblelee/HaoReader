package com.monke.basemvplib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class BaseModelImpl {


    protected <T> T createService(String url, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, tClass);
    }

    protected <T> T createService(String url, String encode, Class<T> tClass) {
        return OkHttpHelper.getInstance().createService(url, encode, tClass);
    }


    protected Observable<String> getAjaxHtml(AjaxParams params) {
        return Observable.create(e -> {
            Poster.INSTANCE.post(() -> {
                WebView webView = createAjaxWebView(params.context, params.userAgent);
                webView.setWebViewClient(new HtmlWebViewClient(params.tag, params.cookieStore, e));
                if (params.postData != null) {
                    webView.postUrl(params.url, params.postData);
                } else if (params.headerMap != null) {
                    webView.loadUrl(params.url, params.headerMap);
                } else {
                    webView.loadUrl(params.url);
                }
            });
        });
    }

    protected Observable<String> sniffAudio(AjaxParams params) {
        return Observable.create(e -> {
            if (params.suffix == null) {
                e.onError(new IllegalAccessException("sniff audio can not with a null suffix."));
                e.onComplete();
                return;
            }

            Poster.INSTANCE.post(() -> {
                WebView webView = createAjaxWebView(params.context, params.userAgent);
                webView.setWebViewClient(new SnifferWebClient(params.tag, params.suffix, params.cookieStore, e));
                if (params.postData != null) {
                    webView.postUrl(params.url, params.postData);
                } else if (params.headerMap != null) {
                    webView.loadUrl(params.url, params.headerMap);
                } else {
                    webView.loadUrl(params.url);
                }
            });
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createAjaxWebView(Context context, String userAgent) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(userAgent);
        settings.setBlockNetworkImage(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        return webView;
    }

    protected static class AjaxParams {

        private final Context context;
        private final String tag;
        private String url;
        private byte[] postData;
        private String userAgent;
        private Map<String, String> headerMap;
        private CookieStore cookieStore;
        private String suffix;

        public AjaxParams(Context context, String tag) {
            this.context = context;
            this.tag = tag;
        }

        public AjaxParams url(String url) {
            this.url = url;
            return this;
        }

        public AjaxParams postData(byte[] postData) {
            this.postData = postData;
            return this;
        }

        public AjaxParams userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AjaxParams headerMap(Map<String, String> headerMap) {
            this.headerMap = headerMap;
            return this;
        }

        public AjaxParams cookieStore(CookieStore cookieStore) {
            this.cookieStore = cookieStore;
            return this;
        }

        public AjaxParams suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
    }

    private static class HtmlWebViewClient extends WebViewClient {

        private final String tag;
        private final CookieStore cookieStore;
        private final ObservableEmitter<String> emitter;

        private HtmlWebViewClient(String tag, CookieStore cookieStore, ObservableEmitter<String> emitter) {
            this.tag = tag;
            this.cookieStore = cookieStore;
            this.emitter = emitter;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String cookie = CookieManager.getInstance().getCookie(url);
            cookieStore.setCookie(tag, cookie);
            super.onPageFinished(view, url);


            view.evaluateJavascript("document.documentElement.outerHTML", value -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(StringEscapeUtils.unescapeJson(value));
                    emitter.onComplete();
                    view.destroy();
                }
            });
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(description));
                    emitter.onComplete();
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(error.getDescription().toString()));
                    emitter.onComplete();
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

    private static class SnifferWebClient extends WebViewClient {

        private final String tag;
        private final String suffix;
        private final CookieStore cookieStore;
        private final ObservableEmitter<String> emitter;

        private SnifferWebClient(String tag, String suffix, CookieStore cookieStore, ObservableEmitter<String> emitter) {
            this.tag = tag;
            this.suffix = suffix;
            this.cookieStore = cookieStore;
            this.emitter = emitter;
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (suffix != null && url.endsWith(suffix)) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(url);
                    emitter.onComplete();
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(description));
                    emitter.onComplete();
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception(error.getDescription().toString()));
                    emitter.onComplete();
                }
                view.destroy();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String cookie = CookieManager.getInstance().getCookie(url);
            cookieStore.setCookie(tag, cookie);
            super.onPageFinished(view, url);
            view.destroy();
        }
    }
}