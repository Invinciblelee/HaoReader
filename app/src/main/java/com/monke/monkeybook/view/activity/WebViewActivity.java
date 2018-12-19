package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.WebLoadConfig;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.widget.ScrimInsetsRelativeLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.schedulers.Schedulers;

public class WebViewActivity extends MBaseActivity {

    @BindView(R.id.rl_content)
    ScrimInsetsRelativeLayout rlContent;
    @BindView(R.id.appBar)
    View appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.tv_html)
    TextView tvHtml;
    @BindView(R.id.web_progress)
    ProgressBar progressBar;
    @BindView(R.id.text_progress)
    ProgressBar textProgressBar;
    @BindView(R.id.web_html_code)
    ScrollView codeView;

    private String mHtmlCode;

    private WebLoadConfig mConfig;

    public static void startThis(Context context, WebLoadConfig config) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("config", config);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mConfig = intent.getParcelableExtra("config");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void bindView() {
        setupActionBar();

        rlContent.setOnInsetsCallback(insets -> appBar.setPadding(0, insets.top, 0, 0));

        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setUserAgentString(AnalyzeHeaders.getUserAgent(mConfig.getUserAgent()));
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new MJavaScriptInterface(this), "HTMLOUT");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    if (progressBar.getVisibility() != View.VISIBLE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        webView.setWebViewClient(new MWebClient(this, mConfig.getUrl()));

        webView.loadUrl(mConfig.getUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.action_browser:
                openInBrowser();
                break;
            case R.id.action_code:
                if (mHtmlCode == null) {
                    toast("网页正在加载，请稍候...");
                } else if (!codeView.isShown()) {
                    showText(mHtmlCode);
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (codeView.isShown()) {
            codeView.setVisibility(View.GONE);
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mConfig.getTitle());
        }
    }

    private void openInBrowser() {
        try {
            String url = webView.getUrl();
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            toast(getString(R.string.can_not_open));
        }
    }

    private void showText(String html) {
        codeView.setVisibility(View.VISIBLE);
        textProgressBar.setVisibility(View.VISIBLE);
        tvHtml.setVisibility(View.INVISIBLE);
        Schedulers.single().createWorker().schedule(() ->
                runOnUiThread(() -> {
                    tvHtml.setText(html);
                    tvHtml.setVisibility(View.VISIBLE);
                    textProgressBar.setVisibility(View.INVISIBLE);
                    codeView.scrollTo(0, 0);
                }), 400L, TimeUnit.MILLISECONDS);
    }

    private void setHtmlCode(String html) {
        mHtmlCode = html;
    }

    private static class MWebClient extends WebViewClient {

        private WeakReference<WebViewActivity> actRef;

        private MWebClient(WebViewActivity activity, String url) {
            this.actRef = new WeakReference<>(activity);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (actRef.get() != null) {
                actRef.get().setHtmlCode(null);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (actRef.get() != null) {
                WebLoadConfig config = actRef.get().mConfig;
                if(config.getLoginTag() != null) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookie = cookieManager.getCookie(url);
                    if(!TextUtils.isEmpty(cookie)) {
                        Log.e("TAG", config.getLoginTag() + "\n" + cookie);
                        CookieHelper.get(actRef.get()).setCookie(config.getLoginTag(), cookie);
                    }
                }
            }
            super.onPageFinished(view, url);

            view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        }
    }

    private static class MJavaScriptInterface {

        private WeakReference<WebViewActivity> actRef;

        private MJavaScriptInterface(WebViewActivity activity) {
            actRef = new WeakReference<>(activity);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            if (actRef.get() != null) {
                actRef.get().setHtmlCode(html);
            }
        }
    }
}
