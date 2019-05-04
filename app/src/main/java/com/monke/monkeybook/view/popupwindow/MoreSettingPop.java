//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MoreSettingPop extends PopupWindow {

    @BindView(R.id.ll_sb_click_all_next)
    View llsbClickAllNext;
    @BindView(R.id.sb_click_all_next)
    Switch sbClickAllNext;
    @BindView(R.id.ll_sb_click)
    View llsbClick;
    @BindView(R.id.sb_click)
    Switch sbClick;
    @BindView(R.id.ll_sb_showTimeBattery)
    View llsbShowTimeBattery;
    @BindView(R.id.sb_showTimeBattery)
    Switch sbShowTimeBattery;
    @BindView(R.id.ll_sb_hideStatusBar)
    View llsbHideStatusBar;
    @BindView(R.id.sb_hideStatusBar)
    Switch sbHideStatusBar;
    @BindView(R.id.ll_sbImmersionBar)
    View llsbImmersionBar;
    @BindView(R.id.sbImmersionBar)
    Switch sbImmersionBar;
    @BindView(R.id.llScreenTimeOut)
    View llScreenTimeOut;
    @BindView(R.id.tv_screen_time_out)
    TextView tvScreenTimeOut;
    @BindView(R.id.llJFConvert)
    View llJFConvert;
    @BindView(R.id.tvJFConvert)
    TextView tvJFConvert;
    @BindView(R.id.ll_sw_volume_next_page)
    View llswVolumeNextPage;
    @BindView(R.id.sw_volume_next_page)
    Switch swVolumeNextPage;
    @BindView(R.id.ll_sw_read_aloud_key)
    View llswReadAloudKey;
    @BindView(R.id.sw_read_aloud_key)
    Switch swReadAloudKey;
    @BindView(R.id.ll_sb_showBatteryNumber)
    View llswShowBatteryNumber;
    @BindView(R.id.sb_showBatteryNumber)
    Switch swShowBatteryNumber;
    @BindView(R.id.ll_sb_showDividerLine)
    View llswShowDividerLine;
    @BindView(R.id.sb_showDividerLine)
    Switch swShowDividerLine;

    private ReadBookActivity activity;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    public interface OnChangeProListener {
        void keepScreenOnChange(int keepScreenOn);

        void refresh();

        void refreshStatusBar();
    }

    private OnChangeProListener changeProListener;

    @SuppressLint("InflateParams")
    public MoreSettingPop(ReadBookActivity readBookActivity, @NonNull OnChangeProListener changeProListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.changeProListener = changeProListener;

        View view = LayoutInflater.from(activity).inflate(R.layout.pop_more_setting, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void bindEvent() {
        llsbHideStatusBar.setOnClickListener(v -> {
            boolean isChecked = sbHideStatusBar.isChecked();
            sbHideStatusBar.setChecked(!isChecked);
            readBookControl.setHideStatusBar(!isChecked);
            changeProListener.refresh();
            upView();
        });
        llswVolumeNextPage.setOnClickListener(v -> {
            boolean isChecked = swVolumeNextPage.isChecked();
            swVolumeNextPage.setChecked(!isChecked);
            readBookControl.setCanKeyTurn(!isChecked);
            upView();
        });
        llswReadAloudKey.setOnClickListener(v -> {
            boolean isChecked = swReadAloudKey.isChecked();
            swReadAloudKey.setChecked(!isChecked);
            readBookControl.setAloudCanKeyTurn(!isChecked);
        });
        llsbClick.setOnClickListener(v -> {
            boolean isChecked = sbClick.isChecked();
            sbClick.setChecked(!isChecked);
            readBookControl.setCanClickTurn(!isChecked);
        });
        llsbClickAllNext.setOnClickListener(v -> {
            boolean isChecked = sbClickAllNext.isChecked();
            sbClickAllNext.setChecked(!isChecked);
            readBookControl.setClickAllNext(!isChecked);
        });

        llsbShowTimeBattery.setOnClickListener(v -> {
            boolean isChecked = sbShowTimeBattery.isChecked();
            sbShowTimeBattery.setChecked(!isChecked);
            readBookControl.setShowTimeBattery(!isChecked);
            changeProListener.refreshStatusBar();
            upView();
        });

        llswShowBatteryNumber.setOnClickListener(v -> {
            boolean isChecked = swShowBatteryNumber.isChecked();
            swShowBatteryNumber.setChecked(!isChecked);
            readBookControl.setShowBatteryNumber(!isChecked);
            changeProListener.refreshStatusBar();
        });

        llswShowDividerLine.setOnClickListener(v -> {
            boolean isChecked = swShowDividerLine.isChecked();
            swShowDividerLine.setChecked(!isChecked);
            readBookControl.setShowBottomLine(!isChecked);
            changeProListener.refreshStatusBar();
        });

        llsbImmersionBar.setOnClickListener(v -> {
            boolean isChecked = sbImmersionBar.isChecked();
            sbImmersionBar.setChecked(!isChecked);
            readBookControl.setImmersionStatusBar(!isChecked);
            changeProListener.refresh();
            RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
        });

        llScreenTimeOut.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.keep_light))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.screen_time_out), readBookControl.getScreenTimeOut(), (dialogInterface, i) -> {
                        readBookControl.setScreenTimeOut(i);
                        upScreenTimeOut(i);
                        changeProListener.keepScreenOnChange(i);
                        dialogInterface.dismiss();
                    })
                    .create();
            dialog.show();
        });
        llJFConvert.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.jf_convert))
                    .setSingleChoiceItems(activity.getResources().getStringArray(R.array.convert_s), readBookControl.getTextConvert(), (dialogInterface, i) -> {
                        readBookControl.setTextConvert(i);
                        upFConvert(i);
                        dialogInterface.dismiss();
                        changeProListener.refresh();
                    })
                    .create();
            dialog.show();
        });
    }

    private void initData() {
        upScreenTimeOut(readBookControl.getScreenTimeOut());
        upFConvert(readBookControl.getTextConvert());
        swVolumeNextPage.setChecked(readBookControl.getCanKeyTurn());
        swReadAloudKey.setChecked(readBookControl.getAloudCanKeyTurn());
        sbHideStatusBar.setChecked(readBookControl.getHideStatusBar());
        sbClick.setChecked(readBookControl.getCanClickTurn());
        sbClickAllNext.setChecked(readBookControl.getClickAllNext());
        sbShowTimeBattery.setChecked(readBookControl.getShowTimeBattery());
        sbImmersionBar.setChecked(readBookControl.getImmersionStatusBar());
        swShowBatteryNumber.setChecked(readBookControl.getShowBatteryNumber());
        swShowDividerLine.setChecked(readBookControl.getShowBottomLine());
        upView();
    }

    private void upView() {
        if (readBookControl.getHideStatusBar()) {
            llsbShowTimeBattery.setVisibility(View.VISIBLE);
            llswShowBatteryNumber.setVisibility(readBookControl.getShowTimeBattery() ? View.VISIBLE : View.GONE);
        } else {
            llsbShowTimeBattery.setVisibility(View.GONE);
            llswShowBatteryNumber.setVisibility(View.GONE);
        }
        if (readBookControl.getCanKeyTurn()) {
            llswReadAloudKey.setVisibility(View.VISIBLE);
        } else {
            llswReadAloudKey.setVisibility(View.GONE);
        }

    }

    private void upScreenTimeOut(int screenTimeOut) {
        tvScreenTimeOut.setText(activity.getResources().getStringArray(R.array.screen_time_out)[screenTimeOut]);
    }

    private void upFConvert(int fConvert) {
        tvJFConvert.setText(activity.getResources().getStringArray(R.array.convert_s)[fConvert]);
    }
}
