//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.presenter.WelcomePresenterImpl;
import com.monke.monkeybook.presenter.contract.WelcomeContract;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class WelcomeActivity extends MBaseActivity<WelcomeContract.Presenter> implements WelcomeContract.View {

    @Override
    protected WelcomeContract.Presenter initInjector() {
        return new WelcomePresenterImpl();
    }

    @Override
    protected void onCreateActivity() {

    }

    @Override
    protected void initData() {
        mPresenter.initData(this);
    }

    @Override
    public void onNormalCreate() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_welcome);

        getWindow().getDecorView().postDelayed(() -> {
            if (preferences.getBoolean(getString(R.string.pk_default_read), false)) {
                startActivity(new Intent(this, ReadBookActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }, 1000L);
    }

    @Override
    public void onFromOtherCreate() {
        setContentView(R.layout.activity_welcome);

        openBookFromUri();
    }

    @Override
    public void openBookFromUri() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            mPresenter.openBookFromUri(this);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.open_from_other),
                    MApplication.RESULT__PERMS, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(MApplication.RESULT__PERMS)
    private void onResultOpenOtherPerms() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            mPresenter.openBookFromUri(this);
        } else {
            Toast.makeText(this, "未获取到权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void startReadBook(BookShelfBean shelfBean, boolean inShelf, long delay) {
        if (delay <= 0) {
            startReadBookActivity(shelfBean, inShelf);
        } else {
            new Handler().postDelayed(() -> startReadBookActivity(shelfBean, inShelf), delay);
        }
    }

    private void startReadBookActivity(BookShelfBean shelfBean, boolean inShelf) {
        Intent intent = new Intent(WelcomeActivity.this, ReadBookActivity.class);
        intent.putExtra("openFromUri", true);
        intent.putExtra("inBookShelf", inShelf);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, shelfBean);
        startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
