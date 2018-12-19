package com.monke.monkeybook.help;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频嗅探
 */
public class AudioSniffer {

    private WeakReference<WebView> mWebViewRef;
    private String mRawUrl;
    private String mAudioType;
    private String mJavaScript;

    private Map<String, String> mCaches;
    private OnSniffListener mSniffListener;

    public AudioSniffer(@NonNull Context context, String audioType, String userAgent, String javaScript) {
        this.mAudioType = audioType;
        this.mJavaScript = javaScript;
        initWebView(context, userAgent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(Context context, String userAgent) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(AnalyzeHeaders.getUserAgent(userAgent));
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setBlockNetworkImage(true);
        webView.setWebViewClient(new SnifferWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        mWebViewRef = new WeakReference<>(webView);
    }

    public void start(String url) {
        stop();

        if (mWebViewRef != null && mWebViewRef.get() != null) {
            this.mRawUrl = url;

            if (mCaches != null && mCaches.containsKey(mRawUrl)) {
                if (mSniffListener != null) {
                    mSniffListener.onResult(mCaches.get(mRawUrl));
                }
            } else {
                mWebViewRef.get().loadUrl(url);
            }
        }

    }

    public void stop() {
        if (mWebViewRef != null && mWebViewRef.get() != null) {
            mWebViewRef.get().stopLoading();
        }
    }

    public void destroy() {
        if (mWebViewRef != null && mWebViewRef.get() != null) {
            mWebViewRef.get().destroy();
        }
    }

    public void setOnSniffListener(OnSniffListener listener) {
        this.mSniffListener = listener;
    }

    private class SnifferWebClient extends WebViewClient {

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.endsWith(mAudioType)) {
                if (mCaches == null) {
                    mCaches = new HashMap<>();
                }

                if (!mCaches.containsKey(mRawUrl)) {
                    mCaches.put(mRawUrl, url);
                }

                if (mSniffListener != null) {
                    mSniffListener.onResult(url);
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
            if (mSniffListener != null) {
                mSniffListener.onError();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!TextUtils.isEmpty(mJavaScript)) {
                view.evaluateJavascript(mJavaScript, null);
            }
        }
    }

    public interface OnSniffListener {
        void onResult(String url);

        void onError();
    }
}
