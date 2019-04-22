package com.monke.monkeybook.view.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.view.fragment.FileSelector;
import com.monke.monkeybook.widget.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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

    private final int REQUEST_CAMERA = 303;
    private final String[] mCameraPer = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("选择图片").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (TextUtils.equals(item.getTitle(), "选择图片")) {
            if (!EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                EasyPermissions.requestPermissions(this, "图片选择需要储存权限", MApplication.RESULT_PERMS, MApplication.PerList);
            } else {
                requestImagePer();
            }
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
        if (!EasyPermissions.hasPermissions(this, mCameraPer)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需相机权限", REQUEST_CAMERA, mCameraPer);
        } else {
            requestCameraPer();
        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_CAMERA)
    public void requestCameraPer() {
        zxingview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @AfterPermissionGranted(MApplication.RESULT_PERMS)
    public void requestImagePer() {
        FileSelector.newInstance("选择图片", true, false, true, new String[]{"png", "jpg", "jpeg"}).show(this, new FileSelector.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
                zxingview.decodeQRCode(path);
            }
        });
    }

}
