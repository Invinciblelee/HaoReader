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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AjaxWebView {

    public AjaxWebView() {
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
                    mCallback.onResult((String) msg.obj);
                    mCallback.onComplete();
                    clearWebView(mWebView);
                    break;
                case MSG_ERROR:
                    mCallback.onError((Throwable) msg.obj);
                    clearWebView(mWebView);
                    break;
            }
        }
    }

    public void ajax(AjaxParams params, Callback callback) {
        final AjaxHandler handler = new AjaxHandler(callback);
        handler.obtainMessage(AjaxHandler.MSG_AJAX_START, params)
                .sendToTarget();
    }

    public void sniff(AjaxParams params, Callback callback) {
        final AjaxHandler handler = new AjaxHandler(callback);
        handler.obtainMessage(AjaxHandler.MSG_SNIFF_START, params)
                .sendToTarget();
    }

    private static void clearWebView(WebView webView) {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static WebView createAjaxWebView(AjaxParams params, Handler handler) {
        WebView webView = new WebView(params.context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(params.getUserAgent());
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
                webView.loadUrl(params.url, params.headerMap);
                break;
            case DEFAULT:
                webView.loadUrl(params.url);

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

        private void setCookie(String cookie) {
            if (cookieStore != null) {
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

        private final AjaxParams params;
        private final Handler handler;

        private HtmlWebViewClient(AjaxParams params, Handler handler) {
            this.params = params;
            this.handler = handler;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String cookie = CookieManager.getInstance().getCookie(url);
            params.setCookie(cookie);
            super.onPageFinished(view, url);

            final String script = "document.documentElement.outerHTML";
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    String result = StringEscapeUtils.unescapeJson(value);
                    if (isLoadFinish(result)) {
                        System.out.println(value);
                        handler.obtainMessage(AjaxHandler.MSG_SUCCESS, result)
                                .sendToTarget();
                    } else {
                        view.evaluateJavascript(script, this);
                    }
                }
            });

        }

        @Override
        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(description))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(error.getDescription().toString()))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        private boolean isLoadFinish(String value) {    // 验证正文内容是否符合要求
            value = value.replaceAll("&nbsp;|<br.*?>|\\s|\\n", "");
            return Pattern.matches(".*[^\\x00-\\xFF]{50,}.*", value);
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
            super.onLoadResource(view, url);
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
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handler.obtainMessage(AjaxHandler.MSG_ERROR, new Exception(description))
                        .sendToTarget();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
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
            String cookie = CookieManager.getInstance().getCookie(url);
            params.setCookie(cookie);
            super.onPageFinished(view, url);
            if (params.hasJavaScript()) {
                view.evaluateJavascript(params.javaScript, null);
                params.clearJavaScript();
            }
        }
    }

    public static abstract class Callback {

        public abstract void onResult(String result);

        public abstract void onError(Throwable error);

        public abstract void onComplete();

    }
}
