package com.monke.basemvplib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.lang3.StringEscapeUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AjaxWebView {

    private final AjaxHandler mHandler;

    public AjaxWebView(Callback callback) {
        mHandler = new AjaxHandler(callback);
    }

    private static class AjaxHandler extends Handler {

        private static final int MSG_AJAX_START = 0;
        private static final int MSG_SNIFF_START = 1;
        private static final int MSG_SUCCESS = 2;
        private static final int MSG_ERROR = 3;

        private final AjaxWebView.Callback mCallback;

        private WebView mWebView;

        private AjaxHandler(AjaxWebView.Callback callback) {
            super(Looper.getMainLooper());
            this.mCallback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AJAX_START:
                    AjaxParams params = (AjaxParams) msg.obj;
                    mWebView = createAjaxWebView(params, this);
                    break;
                case MSG_SNIFF_START:
                    params = (AjaxParams) msg.obj;
                    mWebView = createAjaxWebView(params, this);
                    break;
                case MSG_SUCCESS:
                    System.out.println(msg.obj);
                    mCallback.onResult((String) msg.obj);
                    mCallback.onComplete();
                    destroyWebView();
                    break;
                case MSG_ERROR:
                    mCallback.onError((Throwable) msg.obj);
                    destroyWebView();
                    break;
            }
        }

        private void destroyWebView() {
            if (mWebView != null) {
                mWebView.destroy();
                mWebView = null;
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void ajax(AjaxParams params) {
        mHandler.obtainMessage(AjaxHandler.MSG_AJAX_START, params)
                .sendToTarget();
    }

    public void sniff(AjaxParams params) {
        mHandler.obtainMessage(AjaxHandler.MSG_SNIFF_START, params)
                .sendToTarget();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private static WebView createAjaxWebView(AjaxParams params, Handler handler) {
        WebView webView = new WebView(params.context.getApplicationContext());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(true);
        settings.setUserAgentString(params.getUserAgent());
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (params.isSniff()) {
            webView.setWebViewClient(new SnifferWebClient(params, handler));
        } else {
            webView.setWebViewClient(new HtmlWebViewClient(params, handler));
        }
        switch (params.getRequestMethod()) {
            case POST:
                webView.postUrl(params.url, params.postData);
                break;
            case GET:
            case DEFAULT:
                webView.loadUrl(params.url, params.headerMap);
        }
        return webView;
    }

    public static class AjaxParams {
        private final Context context;
        private final String tag;
        private RequestMethod requestMethod;
        private String url;
        private byte[] postData;
        private Map<String, String> headerMap;
        private CookieStore cookieStore;
        private String audioSuffix;
        private String javaScript;
        private List<String> audioSuffixList;

        public AjaxParams(Context context, String tag) {
            this.context = context;
            this.tag = tag;
        }

        public AjaxParams requestMethod(RequestMethod method) {
            this.requestMethod = method;
            return this;
        }

        public AjaxParams url(String url) {
            this.url = url;
            return this;
        }

        public AjaxParams postData(byte[] postData) {
            this.postData = postData;
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

        private void setCookie(String url) {
            if (cookieStore != null) {
                String cookie = CookieManager.getInstance().getCookie(url);
                cookieStore.setCookie(tag, cookie);
            }
        }

        private RequestMethod getRequestMethod() {
            return requestMethod == null ? RequestMethod.DEFAULT : requestMethod;
        }

        private String getUserAgent() {
            if (this.headerMap != null) {
                return this.headerMap.get("User-Agent");
            }
            return null;
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

        private List<String> getAudioSuffixList() {
            if (audioSuffixList == null) {
                if (isSniff()) {
                    final String[] suffixArray = audioSuffix.split("\\|\\|");
                    audioSuffixList = Arrays.asList(suffixArray);
                } else {
                    audioSuffixList = Collections.emptyList();
                }
            }
            return audioSuffixList;
        }
    }

    private static class HtmlWebViewClient extends WebViewClient {

        private static final String OUTER_HTML = "document.documentElement.outerHTML";

        private final AjaxParams params;
        private final Handler handler;

        private HtmlWebViewClient(AjaxParams params, Handler handler) {
            this.params = params;
            this.handler = handler;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            params.setCookie(url);

            evaluateJavascript(view, OUTER_HTML);
        }


        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(description))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(error.getDescription().toString()))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        private void evaluateJavascript(final WebView webView, final String javaScript) {
            final ScriptRunnable runnable = new ScriptRunnable(webView, javaScript, value -> {
                if (!TextUtils.isEmpty(value)) {
                    handler.obtainMessage(AjaxHandler.MSG_SUCCESS, StringEscapeUtils.unescapeJson(value))
                            .sendToTarget();
                } else {
                    evaluateJavascript(webView, javaScript);
                }
            });
            handler.postDelayed(runnable, 1000L);
        }
    }

    private static class SnifferWebClient extends WebViewClient {

        private final AjaxParams params;
        private final Handler handler;

        private SnifferWebClient(AjaxParams params, Handler handler) {
            this.params = params;
            this.handler = handler;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            List<String> suffixList = params.getAudioSuffixList();
            for (String suffix : suffixList) {
                if (!TextUtils.isEmpty(suffix) && url.contains(suffix)) {
                    handler.obtainMessage(AjaxHandler.MSG_SUCCESS, url)
                            .sendToTarget();
                    break;
                }
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(description))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(error.getDescription().toString()))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            params.setCookie(url);

            if (params.hasJavaScript()) {
                evaluateJavascript(view, params.javaScript);
                params.clearJavaScript();
            }
        }

        private void evaluateJavascript(final WebView webView, final String javaScript) {
            final ScriptRunnable runnable = new ScriptRunnable(webView, javaScript, value -> {
                if (TextUtils.isEmpty(value)) {
                    evaluateJavascript(webView, javaScript);
                }
            });
            handler.postDelayed(runnable, 1000L);
        }
    }

    private static class ScriptRunnable implements Runnable {

        private final String mJavaScript;
        private final ValueCallback<String> mCallback;

        private WeakReference<WebView> mWebView;

        private ScriptRunnable(WebView webView, String javaScript, ValueCallback<String> callback) {
            mWebView = new WeakReference<>(webView);
            mJavaScript = javaScript;
            mCallback = callback;
        }

        @Override
        public void run() {
            WebView webView = mWebView.get();
            if (webView != null) {
                webView.evaluateJavascript(mJavaScript, mCallback);
            }
        }
    }

    public static abstract class Callback {

        public abstract void onResult(String result);

        public abstract void onError(Throwable error);

        public abstract void onComplete();

    }
}
