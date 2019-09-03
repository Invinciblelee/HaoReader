package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gyf.immersionbar.ImmersionBar;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * Created by GKF on 2018/1/29.
 */

public class QRCodeScanActivity extends AppCompatActivity implements QRCodeView.Delegate {

    @BindView(R.id.zxingview)
    ZXingView zxingview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBar)
    View appBar;

    private boolean isFlashLightOpen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        ImmersionBar.with(this).transparentBar().init();
        ButterKnife.bind(this);
        setupActionBar();

        zxingview.setDelegate(this);
    }

    //设置ToolBar
    private void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.white));

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.camera_scan);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getWindow().getDecorView().post(this::startCamera);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("选择图片").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (TextUtils.equals(item.getTitle(), "选择图片")) {
            new PermissionsCompat.Builder(this)
                    .addPermissions(Permissions.Group.STORAGE)
                    .rationale("存储")
                    .onGranted(requestCode -> requestImage())
                    .request();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect rect = new Rect();
        zxingview.getGlobalVisibleRect(rect);
        rect.top = rect.top + appBar.getHeight();
        if (rect.contains((int) ev.getX(), (int) ev.getY())) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && !isFlashLightOpen) {
                isFlashLightOpen = true;
                zxingview.openFlashlight();
            } else if (ev.getAction() == MotionEvent.ACTION_UP && isFlashLightOpen) {
                isFlashLightOpen = false;
                zxingview.closeFlashlight();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
        zxingview.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zxingview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra("result", result);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
    }

    public void startCamera() {
        zxingview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    public void requestImage() {
        FileSelectorDialog.newInstance("选择图片", true, false, true, new String[]{"png", "jpg", "jpeg"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
                zxingview.decodeQRCode(path);
            }
        });
    }

}
