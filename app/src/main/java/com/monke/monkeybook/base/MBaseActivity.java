//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.google.android.material.snackbar.Snackbar;
import com.gyf.immersionbar.ImmersionBar;
import com.monke.basemvplib.AppActivityManager;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.lang.ref.WeakReference;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {

    protected ImmersionBar mImmersionBar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppActivityManager.getInstance().add(this);
        preferences = AppConfigHelper.get().getPreferences();
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mImmersionBar = ImmersionBar.with(this);
        initImmersionBar();
    }


    @Override
    protected void onDestroy() {
        AppActivityManager.getInstance().remove(this);
        super.onDestroy();
    }

    /**
     * 设置MENU图标颜色
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu instanceof MenuBuilder) {
                ((MenuBuilder) menu).setOptionalIconsVisible(true);
            }
            for (int i = 0; i < menu.size(); i++) {
                MenuItemImpl item = (MenuItemImpl) menu.getItem(i);
                if (item.requiresOverflow()) {
                    AppCompat.setTint(item, getResources().getColor(R.color.colorMenuText));
                } else {
                    AppCompat.setTint(item, getResources().getColor(R.color.colorBarText));
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 沉浸状态栏
     */
    public void initImmersionBar() {
        if (isImmersionBarEnabled()) {
            mImmersionBar.transparentStatusBar();
        } else {
            mImmersionBar.statusBarColor(R.color.colorStatusBar);
        }

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        boolean isNightTheme = isNightTheme();

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(!isNightTheme);
        }

        mImmersionBar.statusBarDarkFont(!isNightTheme);

        mImmersionBar.init();
    }

    protected boolean canNavigationBarLightFont() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * @return 是否沉浸
     */
    public boolean isImmersionBarEnabled() {
        return preferences.getBoolean("immersionStatusBar", false);
    }

    /**
     * @return 是否夜间模式
     */
    public boolean isNightTheme() {
        return preferences.getBoolean("nightTheme", false);
    }

    public int getNightMode() {
        return isNightTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void setNightTheme(boolean isNightTheme) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("nightTheme", isNightTheme);
        if (editor.commit()) {
            applyDefaultNightMode();
        }
    }

    private void applyDefaultNightMode() {
        final int nightMode = getNightMode();

        AppCompatDelegate.setDefaultNightMode(nightMode);

        for (WeakReference<Activity> actRef : AppActivityManager.getInstance().getActivities()) {
            Activity activity = actRef.get();
            if (activity instanceof AppCompatActivity && !activity.getClass().equals(this.getClass())) {
                AppCompatDelegate delegate = ((AppCompatActivity) activity).getDelegate();
                delegate.applyDayNight();
            }
        }

    }

    public void startActivityByAnim(Intent intent, int animIn, int animExit) {
        super.startActivity(intent);
        overridePendingTransition(animIn, animExit);
    }

    public void startActivityByAnim(Intent intent, @NonNull View view, @NonNull String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, view, transitionName);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    public void finishByAnim(int animIn, int animOut) {
        super.finish();
        overridePendingTransition(animIn, animOut);
    }

    protected View getSnackBarView() {
        return null;
    }

    public Snackbar getSnackBar(String msg) {
        if (getSnackBarView() == null) {
            return null;
        }
        Snackbar snackbar = Snackbar.make(getSnackBarView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundResource(R.color.colorCardBackground);
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.colorTextDefault));
        return snackbar;
    }

    public void toast(String msg) {
        ToastUtils.toast(this, msg);
    }

    public void toast(@StringRes int resId) {
        ToastUtils.toast(this, resId);
    }

}
