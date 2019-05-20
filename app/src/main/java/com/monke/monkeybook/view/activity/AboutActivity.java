package com.monke.monkeybook.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.utils.ReadAssets;
import com.monke.monkeybook.view.fragment.dialog.LargeTextDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/15.
 * 关于
 */

public class AboutActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.vw_version)
    CardView vwVersion;
    @BindView(R.id.tv_scoring)
    TextView tvScoring;
    @BindView(R.id.vw_scoring)
    CardView vwScoring;
    @BindView(R.id.tv_git)
    TextView tvGit;
    @BindView(R.id.vw_git)
    CardView vwGit;
    @BindView(R.id.tv_disclaimer)
    TextView tvDisclaimer;
    @BindView(R.id.vw_disclaimer)
    CardView vwDisclaimer;
    @BindView(R.id.tv_mail)
    TextView tvMail;
    @BindView(R.id.vw_mail)
    CardView vwMail;
    @BindView(R.id.tv_source_rule)
    TextView tvSourceRule;
    @BindView(R.id.vw_source_rule)
    CardView vwSourceRule;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.vw_update)
    CardView vwUpdate;
    @BindView(R.id.tv_qq)
    TextView tvQq;
    @BindView(R.id.vw_qq)
    CardView vwQq;
    @BindView(R.id.tv_app_summary)
    TextView tvAppSummary;
    @BindView(R.id.tv_update_log)
    TextView tvUpdateLog;
    @BindView(R.id.vw_update_log)
    CardView vwUpdateLog;
    @BindView(R.id.tv_home_page)
    TextView tvHomePage;
    @BindView(R.id.vw_home_page)
    CardView vwHomePage;

    private String qq = "701903217 788025059";

    public static void startThis(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
        tvQq.setText(getString(R.string.qq_group, qq));

        setTextViewIconColor(tvDisclaimer);
        setTextViewIconColor(tvGit);
        setTextViewIconColor(tvHomePage);
        setTextViewIconColor(tvMail);
        setTextViewIconColor(tvQq);
        setTextViewIconColor(tvScoring);
        setTextViewIconColor(tvSourceRule);
        setTextViewIconColor(tvUpdate);
        setTextViewIconColor(tvUpdateLog);
        setTextViewIconColor(tvVersion);

    }

    private void setTextViewIconColor(TextView textView) {
        AppCompat.setTint(textView, getResources().getColor(R.color.colorTextDefault));
    }

    @Override
    protected void bindEvent() {
        vwScoring.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, "market://details?id=" + getPackageName()));
        vwMail.setOnClickListener(view -> openIntent(Intent.ACTION_SENDTO, "1760316362@qq.com"));
        vwGit.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.this_github_url)));
        vwSourceRule.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.source_rule_url)));
        vwDisclaimer.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.disclaimer_url)));
        vwUpdate.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.latest_release_url)));
        vwHomePage.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.home_page_url)));
        vwQq.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, qq);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clipData);
                toast(R.string.copy_complete);
            }
        });

        vwUpdateLog.setOnClickListener(view -> {
            String content = ReadAssets.getText(AboutActivity.this, "updateLog.md");
            LargeTextDialog.show(getSupportFragmentManager(), content, true);
        });
    }

    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open);
        }
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.about);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
