package com.monke.monkeybook.model.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class AudioSniffer {

    private WeakReference<WebView> webViewRef;
    private String rawUrl;
    private BookSourceBean bookSourceBean;
    private AudioBookChapterUrl chapterUrl;

    private Map<String, String> caches;
    private OnSniffListener sniffListener;

    public AudioSniffer(@NonNull Context context, String tag) {
        bookSourceBean = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();

        if (bookSourceBean != null) {
            chapterUrl = new AudioBookChapterUrl(bookSourceBean);
        }

        if (chapterUrl != null && chapterUrl.isAJAX()) {
            initWebView(context);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(Context context) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(AnalyzeHeaders.getUserAgent(bookSourceBean == null ? null : bookSourceBean.getHttpUserAgent()));
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setBlockNetworkImage(true);
        webView.setWebViewClient(new SnifferWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webViewRef = new WeakReference<>(webView);
    }

    public boolean isDirectly(String url) {
        return chapterUrl != null && chapterUrl.checkChapterUrl(url);
    }

    public void start(String url) {
        if (chapterUrl == null) {
            return;
        }

        stop();

        if (webViewRef != null && webViewRef.get() != null) {
            this.rawUrl = url;

            if (caches != null && caches.containsKey(rawUrl)) {
                if (sniffListener != null) {
                    sniffListener.onResult(caches.get(rawUrl));
                }
            } else {
                webViewRef.get().loadUrl(url);
            }
        }

    }

    public void stop() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().stopLoading();
        }
    }

    public void destroy() {
        if (webViewRef != null && webViewRef.get() != null) {
            webViewRef.get().destroy();
        }
    }

    public void setOnSniffListener(OnSniffListener listener) {
        this.sniffListener = listener;
    }

    private class SnifferWebClient extends WebViewClient {

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (chapterUrl.checkChapterUrl(url)) {
                if (caches == null) {
                    caches = new HashMap<>();
                }

                if (!caches.containsKey(rawUrl)) {
                    caches.put(rawUrl, url);
                }

                if (sniffListener != null) {
                    sniffListener.onResult(url);
                    return null;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (sniffListener != null) {
                sniffListener.onError();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js = chapterUrl.getJavaScript();
            if(!TextUtils.isEmpty(js)){
                view.loadUrl("javascript:" + js);
            }
        }
    }

    public interface OnSniffListener {
        void onResult(String url);

        void onError();
    }
}
