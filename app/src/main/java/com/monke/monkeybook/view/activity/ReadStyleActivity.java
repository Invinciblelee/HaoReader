package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.hwangjr.rxbus.RxBus;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadStyleActivity extends MBaseActivity implements ColorPickerDialogListener {

    private final int SELECT_TEXT_COLOR = 201;
    private final int SELECT_BG_COLOR = 301;

    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.tvSelectTextColor)
    TextView tvSelectTextColor;
    @BindView(R.id.tvSelectBgColor)
    TextView tvSelectBgColor;
    @BindView(R.id.tvSelectBgImage)
    TextView tvSelectBgImage;
    @BindView(R.id.tvDefault)
    TextView tvDefault;
    @BindView(R.id.sw_darkStatusIcon)
    Switch swDarkStatusIcon;

    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private int textDrawableIndex;
    private int textColor;
    private int bgColor;
    private Drawable bgDrawable;
    private int bgCustom;
    private boolean darkStatusIcon;
    private String bgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_read_style);
        ButterKnife.bind(this);
        setTextKind(readBookControl);
    }

    @Override
    public void initImmersionBar() {
        if (isImmersionBarEnabled()) {
            mImmersionBar.transparentStatusBar();
        } else {
            mImmersionBar.statusBarColor(R.color.colorStatusBar);
        }

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

        if (darkStatusIcon) {
            mImmersionBar.statusBarDarkFont(true);
        } else {
            mImmersionBar.statusBarDarkFont(false);
        }
        mImmersionBar.init();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        Intent intent = getIntent();
        textDrawableIndex = intent.getIntExtra("index", 1);
        bgCustom = readBookControl.getBgCustom(textDrawableIndex);
        textColor = readBookControl.getTextColor(textDrawableIndex);
        bgDrawable = readBookControl.getBgDrawable(textDrawableIndex, getContext());
        bgColor = readBookControl.getBgColor(textDrawableIndex);
        darkStatusIcon = readBookControl.getDarkStatusIcon(textDrawableIndex);
        upText();
        upBg();
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        swDarkStatusIcon.setChecked(darkStatusIcon);
        swDarkStatusIcon.setOnCheckedChangeListener((compoundButton, b) -> {
            darkStatusIcon = b;
            initImmersionBar();
        });
        //选择文字颜色
        tvSelectTextColor.setOnClickListener(view ->
                ColorPickerDialog.newBuilder()
                        .setColor(textColor)
                        .setShowAlphaSlider(false)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setDialogId(SELECT_TEXT_COLOR)
                        .show(ReadStyleActivity.this));
        //选择背景颜色
        tvSelectBgColor.setOnClickListener(view ->
                ColorPickerDialog.newBuilder()
                        .setColor(bgColor)
                        .setShowAlphaSlider(false)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setDialogId(SELECT_BG_COLOR)
                        .show(ReadStyleActivity.this));
        //选择背景图片
        tvSelectBgImage.setOnClickListener(view -> {
            new PermissionsCompat.Builder(ReadStyleActivity.this)
                    .addPermissions(Permissions.Group.STORAGE)
                    .rationale("存储")
                    .onGranted(requestCode -> imageSelectorResult())
                    .request();
        });
        //恢复默认
        tvDefault.setOnClickListener(view -> {
            bgCustom = 0;
            textColor = readBookControl.getDefaultTextColor(textDrawableIndex);
            bgColor = readBookControl.getDefaultBgColor(textDrawableIndex);
            bgDrawable = readBookControl.getDefaultBgDrawable(textDrawableIndex, this);
            upText();
            upBg();
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
            actionBar.setTitle(R.string.read_style);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read_style_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveStyle();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void imageSelectorResult() {
        FileSelectorDialog.newInstance("选择图片", true, false, true, new String[]{"png", "jpg", "jpeg"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                setCustomBg(path);
            }
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case SELECT_TEXT_COLOR:
                textColor = color;
                upText();
                break;
            case SELECT_BG_COLOR:
                bgCustom = 1;
                bgColor = color;
                bgDrawable = new ColorDrawable(bgColor);
                upBg();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    /**
     * 保存配置
     */
    private void saveStyle() {
        readBookControl.setTextColor(textDrawableIndex, textColor);
        readBookControl.setBgCustom(textDrawableIndex, bgCustom);
        readBookControl.setBgColor(textDrawableIndex, bgColor);
        readBookControl.setDarkStatusIcon(textDrawableIndex, darkStatusIcon);
        if (bgCustom == 2 && bgPath != null) {
            readBookControl.setBgPath(textDrawableIndex, bgPath);
        }
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        finish();
    }

    private void setTextKind(ReadBookControl readBookControl) {
        tvContent.setTextSize(readBookControl.getTextSize());
    }

    private void upText() {
        tvContent.setTextColor(textColor);
    }

    private void upBg() {
        llContent.setBackground(bgDrawable);
    }

    /**
     * 自定义背景
     */
    public void setCustomBg(String path) {
        try {
            bgPath = path;
            Bitmap bitmap = BitmapFactory.decodeFile(bgPath);
            bgCustom = 2;
            bgDrawable = new BitmapDrawable(getResources(), bitmap);
            upBg();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
