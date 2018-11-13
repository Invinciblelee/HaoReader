//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gyf.barlibrary.BarHide;
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
    public void onStartNormal(long startDelay) {
        if (startDelay <= 0) {
            startMainActivity();
        } else {
            new Handler().postDelayed(this::startMainActivity, startDelay);
        }
    }

    @Override
    public void onStartFromUri() {
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
    public void startReadBookAct(BookShelfBean shelfBean, boolean inShelf, boolean fromUri, long startDelay) {
        if (startDelay <= 0) {
            startReadBookActivity(shelfBean, inShelf, fromUri);
        } else {
            new Handler().postDelayed(() -> startReadBookActivity(shelfBean, inShelf, fromUri), startDelay);
        }
    }

    private void startReadBookActivity(BookShelfBean shelfBean, boolean inShelf, boolean fromUri) {
        if (fromUri) {
            ReadBookActivity.startThisFromUri(this, shelfBean, inShelf);
        } else {
            ReadBookActivity.startThis(this, shelfBean, inShelf);
        }
        finish();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivityByAnim(intent, 0, 0);
    }

    @Override
    public void finish() {
        super.finishByAnim(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
