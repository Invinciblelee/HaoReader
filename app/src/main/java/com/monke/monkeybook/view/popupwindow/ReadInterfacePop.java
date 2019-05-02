//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.activity.ReadStyleActivity;
import com.monke.monkeybook.widget.font.FontSelector;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReadInterfacePop extends PopupWindow {

    @BindView(R.id.btn_font_smaller)
    AppCompatButton smallerTextSize;
    @BindView(R.id.btn_font_larger)
    AppCompatButton largerTextSize;
    @BindView(R.id.tv_font_size)
    TextView tvFontSize;
    @BindView(R.id.btn_text_bold)
    AppCompatButton btnBoldText;
    @BindView(R.id.btn_text_font)
    AppCompatButton btnTextFont;
    @BindView(R.id.view_page_anim_mode)
    ViewGroup pageAnimModeView;
    @BindView(R.id.view_page_space)
    ViewGroup pageSpaceModeView;
    @BindView(R.id.civ_bg_white)
    CircleImageView civBgWhite;
    @BindView(R.id.civ_bg_yellow)
    CircleImageView civBgYellow;
    @BindView(R.id.civ_bg_green)
    CircleImageView civBgGreen;
    @BindView(R.id.civ_bg_black)
    CircleImageView civBgBlack;
    @BindView(R.id.civ_bg_blue)
    CircleImageView civBgBlue;
    @BindView(R.id.tv0)
    TextView tv0;
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.tv4)
    TextView tv4;

    private PopupWindow mSpacePop;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    private WeakReference<View> mParentView;

    public interface OnChangeProListener {
        void upPageMode();

        void upTextSize();

        void upMargin();

        void bgChange();

        void refresh();
    }

    private OnChangeProListener changeProListener;

    public ReadInterfacePop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(readBookActivity).inflate(R.layout.pop_read_interface, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(readBookActivity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void initData() {
        setBg();
        updateBg(readBookControl.getTextDrawableIndex());
        updateBoldText(readBookControl.getTextBold());
        updatePageMode(readBookControl.getPageMode());
        updateSpaceMode(readBookControl.getPageSpaceMode());
        tvFontSize.setText(String.valueOf(readBookControl.getTextSize()));
    }

    private void bindEvent() {
        smallerTextSize.setOnClickListener(v -> {
            tvFontSize.setText(String.valueOf(readBookControl.smallerTextSize()));
            changeProListener.upTextSize();
        });

        largerTextSize.setOnClickListener(v -> {
            tvFontSize.setText(String.valueOf(readBookControl.largerTextSize()));
            changeProListener.upTextSize();
        });


        //加粗切换
        btnBoldText.setOnClickListener(view -> {
            readBookControl.setTextBold(!readBookControl.getTextBold());
            updateBoldText(readBookControl.getTextBold());
            changeProListener.refresh();
        });

        //选择字体
        btnTextFont.setOnClickListener(view -> new PermissionsCompat.Builder(activity)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(requestCode -> showFontSelector())
                .request());

        //长按清除字体
        btnTextFont.setOnLongClickListener(view -> {
            clearFontPath();
            ToastUtils.toast(activity, R.string.clear_font);
            return true;
        });


        //翻页模式
        for (int i = 0; i < pageAnimModeView.getChildCount(); i++) {
            View child = pageAnimModeView.getChildAt(i);
            if (child instanceof AppCompatButton) {
                child.setOnClickListener(v -> {
                    int pos = Integer.parseInt((String) v.getTag());
                    readBookControl.setPageMode(pos);
                    updatePageMode(pos);
                    changeProListener.upPageMode();
                });
            }
        }

        //排版模式
        for (int i = 0; i < pageSpaceModeView.getChildCount(); i++) {
            View child = pageSpaceModeView.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String) {
                child.setOnClickListener(v -> {
                    int pos = Integer.parseInt((String) v.getTag());
                    if (pos == 4) {
                        showCustomSpacePop();
                    } else {
                        readBookControl.setPageSpaceMode(pos);
                        updateSpaceMode(pos);
                        changeProListener.upMargin();
                    }
                });
            }
        }

        //背景选择
        civBgWhite.setOnClickListener(v -> {
            updateBg(0);
            changeProListener.bgChange();
        });
        civBgYellow.setOnClickListener(v -> {
            updateBg(1);
            changeProListener.bgChange();
        });
        civBgGreen.setOnClickListener(v -> {
            updateBg(2);
            changeProListener.bgChange();
        });
        civBgBlue.setOnClickListener(v -> {
            updateBg(3);
            changeProListener.bgChange();
        });
        civBgBlack.setOnClickListener(v -> {
            updateBg(4);
            changeProListener.bgChange();
        });
        //自定义阅读样式
        civBgWhite.setOnLongClickListener(view -> customReadStyle(0));
        civBgYellow.setOnLongClickListener(view -> customReadStyle(1));
        civBgGreen.setOnLongClickListener(view -> customReadStyle(2));
        civBgBlue.setOnLongClickListener(view -> customReadStyle(3));
        civBgBlack.setOnLongClickListener(view -> customReadStyle(4));

    }

    //自定义阅读样式
    private boolean customReadStyle(int index) {
        Intent intent = new Intent(activity, ReadStyleActivity.class);
        intent.putExtra("index", index);
        activity.startActivity(intent);
        return false;
    }

    private void showFontSelector() {
        new FontSelector(activity, readBookControl.getFontPath())
                .setListener(new FontSelector.OnThisListener() {
                    @Override
                    public void setDefault() {
                        clearFontPath();
                    }

                    @Override
                    public void setFontPath(String fontPath) {
                        setReadFonts(fontPath);
                    }
                })
                .create()
                .show();
    }

    //设置字体
    public void setReadFonts(String path) {
        readBookControl.setReadBookFont(path);
        changeProListener.refresh();
    }

    //清除字体
    private void clearFontPath() {
        readBookControl.setReadBookFont(null);
        changeProListener.refresh();
    }

    private void updatePageMode(int pageMode) {
        for (int i = 0; i < pageAnimModeView.getChildCount(); i++) {
            View child = pageAnimModeView.getChildAt(i);
            if (child instanceof AppCompatButton) {
                int pos = Integer.parseInt((String) child.getTag());
                child.setSelected(pageMode == pos);
            }
        }
    }

    private void updateSpaceMode(int spaceMode) {
        for (int i = 0; i < pageSpaceModeView.getChildCount(); i++) {
            View child = pageSpaceModeView.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String) {
                int pos = Integer.parseInt((String) child.getTag());
                child.setSelected(spaceMode == pos);
            }
        }
    }

    private void updateBoldText(Boolean isBold) {
        btnBoldText.setSelected(isBold);
    }

    public void setBg() {
        tv0.setTextColor(readBookControl.getTextColor(0));
        tv1.setTextColor(readBookControl.getTextColor(1));
        tv2.setTextColor(readBookControl.getTextColor(2));
        tv3.setTextColor(readBookControl.getTextColor(3));
        tv4.setTextColor(readBookControl.getTextColor(4));
        civBgWhite.setImageDrawable(readBookControl.getBgDrawable(0, activity));
        civBgYellow.setImageDrawable(readBookControl.getBgDrawable(1, activity));
        civBgGreen.setImageDrawable(readBookControl.getBgDrawable(2, activity));
        civBgBlue.setImageDrawable(readBookControl.getBgDrawable(3, activity));
        civBgBlack.setImageDrawable(readBookControl.getBgDrawable(4, activity));
    }

    private void updateBg(int index) {
        civBgWhite.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgYellow.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgGreen.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgBlack.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        civBgBlue.setBorderColor(activity.getResources().getColor(R.color.tv_text_default));
        int selectColor = activity.getResources().getColor(R.color.colorAccent);
        switch (index) {
            case 0:
                civBgWhite.setBorderColor(selectColor);
                break;
            case 1:
                civBgYellow.setBorderColor(selectColor);
                break;
            case 2:
                civBgGreen.setBorderColor(selectColor);
                break;
            case 3:
                civBgBlue.setBorderColor(selectColor);
                break;
            case 4:
                civBgBlack.setBorderColor(selectColor);
                break;
        }
        readBookControl.setTextDrawableIndex(index);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        mParentView = new WeakReference<>(parent);
    }

    private void showCustomSpacePop() {
        if (mSpacePop == null) {
            View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_custom_space, null);
            mSpacePop = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mSpacePop.setContentView(contentView);

            mSpacePop.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
            mSpacePop.setFocusable(true);
            mSpacePop.setTouchable(true);
            mSpacePop.setAnimationStyle(R.style.anim_pop_windowslide);
        }

        initCustomSpaceView(mSpacePop.getContentView());

        if (mParentView != null) {
            mSpacePop.showAtLocation(mParentView.get(), Gravity.BOTTOM, 0, 0);
        }
    }


    private void initCustomSpaceView(View contentView) {
        ImageButton btnClose = contentView.findViewById(R.id.btn_close_custom_space);
        btnClose.setOnClickListener(v -> mSpacePop.dismiss());

        ViewGroup viewGroup = contentView.findViewById(R.id.view_line_space);
        initSpaceChildView(viewGroup);
        viewGroup = contentView.findViewById(R.id.view_paragraph_space);
        initSpaceChildView(viewGroup);
        viewGroup = contentView.findViewById(R.id.view_padding_top);
        initSpaceChildView(viewGroup);
        viewGroup = contentView.findViewById(R.id.view_padding_left);
        initSpaceChildView(viewGroup);
        viewGroup = contentView.findViewById(R.id.view_padding_right);
        initSpaceChildView(viewGroup);
        viewGroup = contentView.findViewById(R.id.view_padding_bottom);
        initSpaceChildView(viewGroup);
    }

    private void initSpaceChildView(ViewGroup childGroup) {
        String key = (String) childGroup.getTag();
        int progress = readBookControl.getSpacingByKey(key);
        AppCompatSeekBar progressBar = (AppCompatSeekBar) childGroup.getChildAt(1);
        TextView tvProgress = (TextView) childGroup.getChildAt(2);
        progressBar.setProgress(progress);
        tvProgress.setText(String.valueOf(progress));

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgress.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSpacing(key, seekBar.getProgress());
            }
        });
    }

    private void updateSpacing(String key, int value) {
        switch (key) {
            case "lineSpacing":
                readBookControl.setLineSpacing(value);
                break;
            case "paragraphSpacing":
                readBookControl.setParagraphSpacing(value);
                break;
            case "paddingTop":
                readBookControl.setPaddingTop(value);
                break;
            case "paddingLeft":
                readBookControl.setPaddingLeft(value);
                break;
            case "paddingRight":
                readBookControl.setPaddingRight(value);
                break;
            case "paddingBottom":
                readBookControl.setPaddingBottom(value);
                break;
        }
        changeProListener.upMargin();

        updateSpaceMode(4);
    }
}