package com.monke.basemvplib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;

public class AjaxWebView {

    private AjaxWebView() {
    }

    public static void ajax(AjaxParams params, Callback callback) {
        Poster.INSTANCE.post(() -> {
            WebView webView = createAjaxWebView(params.context, params.userAgent);
            webView.setWebViewClient(new HtmlWebViewClient(params, callback));
            applyAjaxParams(webView, params);
        });
    }

    public static void sniff(AjaxParams params, Callback callback) {
        Poster.INSTANCE.post(() -> {
            WebView webView = createAjaxWebView(params.context, params.userAgent);
            if (params.isSniff()) {
                webView.setWebViewClient(new SnifferWebClient(params, callback));
            } else {
                webView.setWebViewClient(new HtmlWebViewClient(params, callback));
            }
            applyAjaxParams(webView, params);
        });
    }

    private static void applyAjaxParams(WebView webView, AjaxParams params) {
        if (params.postData != null) {
            webView.postUrl(params.url, params.postData);
        } else if (params.headerMap != null) {
            webView.loadUrl(params.url, params.headerMap);
        } else {
            webView.loadUrl(params.url);
        }
    }

    private static void clearWebView(WebView webView) {
        if (webView != null) {
            webView.stopLoading();
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.destroy();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static WebView createAjaxWebView(Context context, String userAgent) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(userAgent);
        settings.setBlockNetworkImage(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        return webView;
    }

    public static class AjaxParams {
        private final Context context;
        private final String tag;
        private String url;
        private byte[] postData;
        private String userAgent;
        private Map<String, String> headerMap;
        private CookieStore cookieStore;
        private String audioSuffix;
        private String javaScript;

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
            this.audioSuffix = suffix;
            return this;
        }

        public AjaxParams javaScript(String javaScript) {
            this.javaScript = javaScript;
            return this;
        }

        private void setCookie(String cookie) {
            if (cookieStore != null) {
                cookieStore.setCookie(tag, cookie);
            }
        }

        private boolean isSniff() {
            return !TextUtils.isEmpty(audioSuffix);
        }

        private boolean hasJavaScript() {
            return !TextUtils.isEmpty(javaScript);
        }

        private void clearJavaScript() {
            javaScript = null;
        }
    }


    private static class HtmlWebViewClient extends WebViewClient {

        private final AjaxParams params;
        private Callback callback;

        private HtmlWebViewClient(AjaxParams params, Callback callback) {
            this.params = params;
            this.callback = callback;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String cookie = CookieManager.getInstance().getCookie(url);
            params.setCookie(cookie);
            super.onPageFinished(view, url);

            view.evaluateJavascript("document.documentElement.outerHTML", value -> {
                callback.onResult(StringEscapeUtils.unescapeJson(value));
                callback.onComplete();
                clearWebView(view);
            });
        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                callback.onError(new Exception(description));
                clearWebView(view);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                callback.onError(new Exception(error.getDescription().toString()));
                clearWebView(view);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

    private static class SnifferWebClient extends WebViewClient {

        private final AjaxParams params;
        private final Callback callback;

        private SnifferWebClient(AjaxParams params, Callback callback) {
            this.params = params;
            this.callback = callback;
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            String suffix = params.audioSuffix;
            if (!TextUtils.isEmpty(suffix) && url.contains(suffix)) {
                callback.onResult(url);
                callback.onComplete();
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                callback.onError(new Exception(description));
                clearWebView(view);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                callback.onError(new Exception(error.getDescription().toString()));
                clearWebView(view);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String cookie = CookieManager.getInstance().getCookie(url);
            params.setCookie(cookie);
            super.onPageFinished(view, url);
            if (params.hasJavaScript()) {
                view.evaluateJavascript(params.javaScript, null);
                params.clearJavaScript();
            } else {
                clearWebView(view);
            }
        }
    }

    public static abstract class Callback {

        public abstract void onResult(String result);

        public abstract void onError(Throwable error);

        public abstract void onComplete();

    }
}
