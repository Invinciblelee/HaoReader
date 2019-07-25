package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.model.content.Debug;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.theme.AppCompat;
import com.monke.monkeybook.widget.refreshview.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class SourceDebugActivity extends MBaseActivity {
    private final int REQUEST_QR = 202;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout loading;
    @BindView(R.id.action_bar)
    AppBarLayout actionBar;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;

    private CompositeDisposable compositeDisposable;
    private String sourceTag;

    public static void startThis(Context context, String sourceUrl) {
        if (TextUtils.isEmpty(sourceUrl)) return;
        Intent intent = new Intent(context, SourceDebugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("sourceUrl", sourceUrl);
        context.startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onDestroy() {
        Debug.SOURCE_DEBUG_TAG = null;
        RxBus.get().unregister(this);
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_debug);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        sourceTag = getIntent().getStringExtra("sourceUrl");
    }

    @Override
    protected void bindView() {
        super.bindView();
        ButterKnife.bind(this);
        initSearchView();
        loading.setEnabled(false);
    }

    private void initSearchView() {
        AppCompat.useCustomIconForSearchView(searchView, getString(R.string.debug_hint));
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query))
                    return false;
                startDebug(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void startDebug(String key) {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
        loading.startRefreshing();
        tvContent.setText("");
        Debug.newDebug(sourceTag, key, compositeDisposable, new Debug.CallBack() {
            @Override
            public void printLog(String msg) {
                printDebugLog(msg);
            }

            @Override
            public void printError(String msg) {
                loading.stopRefreshing();
                printDebugLog(msg);
            }

            @Override
            public void finish() {
                loading.stopRefreshing();
            }
        });
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debug_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_scan:
                scan();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scan() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.Group.CAMERA)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("相机/存储")
                .onGranted(requestCode -> {
                    Intent intent = new Intent(SourceDebugActivity.this, QRCodeScanActivity.class);
                    startActivityForResult(intent, REQUEST_QR);
                })
                .request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_QR) {
                String result = data.getStringExtra("result");
                if (!StringUtils.isBlank(result)) {
                    searchView.setQuery(result, true);
                }
            }
        }
    }

    private void printDebugLog(String msg) {
        int titleIndex = msg.indexOf("◆[");
        if (titleIndex >= 0) {
            SpannableString spannableString = new SpannableString(msg);
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), titleIndex, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!TextUtils.isEmpty(tvContent.getText())) {
                tvContent.append("\n");
            }
            tvContent.append(spannableString);
        } else {
            if (!TextUtils.isEmpty(tvContent.getText())) {
                tvContent.append("\n");
            }
            tvContent.append(msg);
        }

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

    }

}