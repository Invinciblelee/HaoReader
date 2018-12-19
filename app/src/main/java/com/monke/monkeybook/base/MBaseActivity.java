//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.base;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.widget.AppCompat;

import java.lang.reflect.Method;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    protected ImmersionBar mImmersionBar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = AppConfigHelper.get(this).getPreferences();
        initNightTheme();
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        mImmersionBar = ImmersionBar.with(this);
        initImmersionBar();
        initToolbarColors();
    }


    private void initToolbarColors() {
        Toolbar toolbar = findToolbar();
        if (toolbar != null) {
            int color = getResources().getColor(R.color.menu_color_default);
            toolbar.setTitleTextColor(color);
            AppCompat.setToolbarNavIconTint(toolbar, color);
        }
    }

    private Toolbar findToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            return toolbar;
        } else {
            ViewGroup viewGroup = getWindow().getDecorView().findViewById(android.R.id.content);
            return findToolbar(viewGroup);
        }
    }

    private Toolbar findToolbar(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof Toolbar) {
                return (Toolbar) child;
            } else if (child instanceof ViewGroup) {
                return findToolbar((ViewGroup) child);
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) {
            mImmersionBar.destroy();  //在BaseActivity里销毁}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    @SuppressLint("PrivateApi")
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 设置MENU图标颜色
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                AppCompat.setTint(item, getResources().getColor(R.color.menu_color_default));
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 沉浸状态栏
     */
    protected void initImmersionBar() {
        try {
            if (isImmersionBarEnabled()) {
                mImmersionBar.transparentStatusBar();
            } else {
                mImmersionBar.statusBarColor(R.color.status_bar_bag);
            }

            mImmersionBar.navigationBarColor(R.color.navigation_bar_bag);

            if (canNavigationBarLightFont()) {
                mImmersionBar.navigationBarDarkIcon(false);
            }

            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }

            mImmersionBar.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void setNightTheme(boolean isNightTheme) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("nightTheme", isNightTheme);
        editor.apply();
        initNightTheme();
    }

    public void setOrientation(int screenDirection) {
        switch (screenDirection) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 3:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
        }
    }

    public void initNightTheme() {
        if (isNightTheme()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
        view.setBackgroundResource(R.color.card_background);
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.tv_text_default));
        return snackbar;
    }

    public void showSnackBar(String msg) {
        Snackbar snackbar = getSnackBar(msg);
        if (snackbar != null) {
            snackbar.show();
        }
    }

    public void showSnackBar(@StringRes int msgId){
        showSnackBar(getString(msgId));
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void toast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
