package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.presenter.CacheManagerPresenter;
import com.monke.monkeybook.presenter.contract.CacheManagerContract;
import com.monke.monkeybook.utils.NumberUtil;
import com.monke.monkeybook.view.adapter.CacheListAdapter;
import com.monke.monkeybook.view.fragment.dialog.AlertDialog;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CacheManagerActivity extends MBaseActivity<CacheManagerContract.Presenter> implements CacheManagerContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView rvList;
    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    private CacheListAdapter adapter;

    private AlertDialog progressDialog;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, CacheManagerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected CacheManagerContract.Presenter initInjector() {
        return new CacheManagerPresenter();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_cache_manager);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(new LinearLayoutManager(this));

        rvList.setAdapter(adapter = new CacheListAdapter(this));
    }

    @Override
    protected void bindEvent() {
        adapter.setOnExtractCacheListener(bookShelfBean -> extractBookCache(bookShelfBean, false));
    }

    @Override
    protected void firstRequest() {
        mPresenter.queryBooks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected View getSnackBarView() {
        return toolbar;
    }

    @Override
    public void showBookList(List<BookShelfBean> bookShelfBeans) {
        adapter.setDataList(bookShelfBeans);
    }

    @Override
    public void removeItem(BookShelfBean bookShelfBean) {
        adapter.remove(bookShelfBean);
    }

    @Override
    public void showExtractTip(BookShelfBean bookShelfBean) {
        new AlertDialog.Builder(getSupportFragmentManager())
                .setTitle(R.string.dialog_title)
                .setMessage("当前书籍存在已提取的文本，是否重新提取")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> extractBookCache(bookShelfBean, true))
                .show();
    }

    @Override
    public void updateProgress(int max, int progress) {
        if (progressDialog == null) {
            showProgressDialog(max, progress);
        } else {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }

            if (progressDialog.isViewCreated()) {
                setProgress(max, progress);
            }

            if (progress == max) {
                new Handler().postDelayed(() -> {
                    progressDialog.dismissAllowingStateLoss();
                    toast("缓存提取成功");
                }, 100L);
            }
        }
    }

    @Override
    public void showProgress() {
        progressBar.show();
    }

    @Override
    public void hideProgress() {
        progressBar.hide();
    }

    private void showProgressDialog(int max, int progress) {
        progressDialog = new AlertDialog.Builder(getSupportFragmentManager())
                .setTitle("正在提取")
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .setOnViewCreatedCallback((dialog, dialogView) -> setProgress(max, progress))
                .setNegativeButton(R.string.cancel, (dialog, which) -> mPresenter.cancel())
                .show();
    }

    private void setProgress(int max, int progress) {
        ProgressBar progressBar = progressDialog.findViewById(R.id.progress_bar);
        TextView tvPercent = progressDialog.findViewById(R.id.tv_progress_percent);
        TextView tvProgress = progressDialog.findViewById(R.id.tv_progress);
        if (progressBar != null) {
            progressBar.setMax(max);
            progressBar.setProgress(progress);
        }

        if (tvPercent != null) {
            if (max != 0) {
                tvPercent.setText(String.format(Locale.getDefault(), "%d%%", NumberUtil.getPercent(progress, max)));
            } else {
                tvPercent.setText("0%");
            }
        }

        if (tvProgress != null) {
            tvProgress.setText(String.format(Locale.getDefault(), "%d/%d", progress, max));
        }
    }

    private void extractBookCache(BookShelfBean bookShelfBean, boolean force) {
        new PermissionsCompat.Builder(CacheManagerActivity.this)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(requestCode -> mPresenter.extractBookCache(bookShelfBean, force))
                .request();
    }
}
