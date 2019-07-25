package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.WebLoadConfig;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.widget.ScrimInsetsRelativeLayout;
import com.monke.monkeybook.widget.refreshview.SwipeRefreshLayout;
import com.monke.monkeybook.widget.theme.AppCompat;

import org.apache.commons.lang3.StringEscapeUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.schedulers.Schedulers;

public class WebViewActivity extends MBaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.rl_content)
    ScrimInsetsRelativeLayout rlContent;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
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

    private String mOuterHtml;

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
    public void initImmersionBar() {
        mImmersionBar.transparentStatusBar();

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

        mImmersionBar.statusBarDarkFont(false);

        mImmersionBar.init();
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mConfig = intent.getParcelableExtra("config");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void bindView() {
        rlContent.setOnInsetsCallback(insets -> appBar.setPadding(0, insets.top, 0, 0));

        refreshLayout.setOnRefreshListener(this);

        WebSettings settings = webView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setUserAgentString(AnalyzeHeaders.getUserAgent(mConfig.getUserAgent()));
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    if (progressBar.getVisibility() != View.VISIBLE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                    refreshLayout.stopRefreshing();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (TextUtils.isEmpty(mConfig.getTitle())) {
                    setTitle(title);
                }
            }
        });
        webView.setWebViewClient(new MWebClient(this));

        getWindow().getDecorView().post(() -> {
            if (webView != null) {
                mConfig.intoWebView(webView);
            }
        });

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
                if (mOuterHtml == null) {
                    toast("网页正在加载，请稍候...");
                } else if (!codeView.isShown()) {
                    showText(mOuterHtml);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.setVisibility(View.GONE);
            webView.destroy();
            webView = null;
        }
    }

    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mConfig.getTitle());
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void onRefresh() {
        webView.reload();
    }

    private void openInBrowser() {
        try {
            String url = webView.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open);
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

    private void setHtmlText(String html) {
        mOuterHtml = html;
    }

    private static class MWebClient extends WebViewClient {

        private final WeakReference<WebViewActivity> actRef;

        private MWebClient(WebViewActivity activity) {
            this.actRef = new WeakReference<>(activity);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (actRef.get() != null) {
                actRef.get().setHtmlText(null);
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
                if (config != null) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    CookieHelper.getInstance().setCookie(config.getTag(), cookieManager.getCookie(url));
                }
            }
            super.onPageFinished(view, url);

            view.evaluateJavascript("document.documentElement.outerHTML", value -> {
                if (actRef.get() != null) {
                    actRef.get().setHtmlText(StringEscapeUtils.unescapeJson(value));
                }
            });
        }
    }

}
