//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;

import com.gyf.immersionbar.BarHide;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.presenter.WelcomePresenterImpl;
import com.monke.monkeybook.presenter.contract.WelcomeContract;

public class WelcomeActivity extends MBaseActivity<WelcomeContract.Presenter> implements WelcomeContract.View {

    @Override
    protected WelcomeContract.Presenter initInjector() {
        return new WelcomePresenterImpl();
    }

    @Override
    protected void onCreateActivity() {

    }

    @Override
    protected void initImmersionBar() {
        mImmersionBar.fullScreen(true);
        mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
        mImmersionBar.init();
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
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(requestCode -> mPresenter.openBookFromUri(WelcomeActivity.this))
                .request();
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
        super.finishByAnim(R.anim.anim_alpha_in, R.anim.anim_alpha_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
