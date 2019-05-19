//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.widget.checkbox.SmoothCheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.monkeybook.utils.ScreenBrightnessUtil.getScreenBrightness;
import static com.monke.monkeybook.utils.ScreenBrightnessUtil.setScreenBrightness;

public class ReadAdjustPop extends PopupWindow {
    @BindView(R.id.hpb_light)
    AppCompatSeekBar hpbLight;
    @BindView(R.id.scb_follow_sys)
    SmoothCheckBox scbFollowSys;
    @BindView(R.id.ll_follow_sys)
    LinearLayout llFollowSys;
    @BindView(R.id.ll_click)
    LinearLayout llClick;
    @BindView(R.id.hpb_click)
    AppCompatSeekBar hpbClick;
    @BindView(R.id.ll_tts_SpeechRate)
    LinearLayout llTtsSpeechRate;
    @BindView(R.id.hpb_tts_SpeechRate)
    AppCompatSeekBar hpbTtsSpeechRate;
    @BindView(R.id.scb_tts_follow_sys)
    SmoothCheckBox scbTtsFollowSys;
    @BindView(R.id.tv_auto_page)
    TextView tvAutoPage;
    @BindView(R.id.hpb_anim_duration)
    AppCompatSeekBar hpbAnimDuration;
    @BindView(R.id.tv_anim_duration)
    TextView tvAnimDuration;

    private ReadBookActivity activity;
    private Boolean isFollowSys;
    private int light;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private OnAdjustListener adjustListener;

    public interface OnAdjustListener {
        void changeSpeechRate(int speechRate);

        void speechRateFollowSys();
    }

    public ReadAdjustPop(ReadBookActivity readBookActivity, OnAdjustListener adjustListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.activity = readBookActivity;
        this.adjustListener = adjustListener;

        View view = LayoutInflater.from(activity).inflate(R.layout.pop_read_adjust, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.Animation_Pop_Grow);
    }

    private void initData() {
        isFollowSys = readBookControl.getLightIsFollowSys();
        light = readBookControl.getScreenLight(getScreenBrightness(activity));
    }

    private void bindEvent() {
        //亮度调节
        llFollowSys.setOnClickListener(v -> {
            if (scbFollowSys.isChecked()) {
                scbFollowSys.setChecked(false, true);
            } else {
                scbFollowSys.setChecked(true, true);
            }
        });
        scbFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            isFollowSys = isChecked;
            if (isChecked) {
                //跟随系统
                hpbLight.setEnabled(false);
                setScreenBrightness(activity);
            } else {
                //不跟随系统
                hpbLight.setEnabled(true);
                hpbLight.setProgress(light);
            }
        });
        hpbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (!isFollowSys) {
                        setScreenBrightness(activity, progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //自动翻页间隔
        hpbClick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvAutoPage.setText(String.format("%sS", progress + 5));
                if (fromUser) {
                    readBookControl.setClickSensitivity(progress + 5);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        hpbClick.setMax(175);
        hpbClick.setProgress(readBookControl.getClickSensitivity() - 5);

        hpbAnimDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvAnimDuration.setText(String.format("%sMS", progress + 200));
                if (fromUser) {
                    readBookControl.setAnimSpeed(progress + 200);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        hpbAnimDuration.setMax(400);
        hpbAnimDuration.setProgress(readBookControl.getAnimSpeed() - 200);

        //朗读语速调节
        scbTtsFollowSys.setChecked(readBookControl.isSpeechRateFollowSys());
        hpbTtsSpeechRate.setEnabled(!scbTtsFollowSys.isChecked());
        llTtsSpeechRate.setOnClickListener(v -> {
            if (scbTtsFollowSys.isChecked()) {
                scbTtsFollowSys.setChecked(false, true);
            } else {
                scbTtsFollowSys.setChecked(true, true);
            }
        });
        scbTtsFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                //跟随系统
                hpbTtsSpeechRate.setEnabled(false);
                readBookControl.setSpeechRateFollowSys(true);
                if (adjustListener != null) {
                    adjustListener.speechRateFollowSys();
                }
            } else {
                //不跟随系统
                hpbTtsSpeechRate.setEnabled(true);
                readBookControl.setSpeechRateFollowSys(false);
                if (adjustListener != null) {
                    adjustListener.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }
        });

        hpbTtsSpeechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    readBookControl.setSpeechRate(progress + 5);
                    if (adjustListener != null) {
                        adjustListener.changeSpeechRate(readBookControl.getSpeechRate());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        hpbTtsSpeechRate.setProgress(readBookControl.getSpeechRate() - 5);
    }

    @Override
    public void dismiss() {
        readBookControl.saveLight(light, isFollowSys);
        super.dismiss();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        initData();
        hpbLight.setProgress(light);
        scbFollowSys.setChecked(isFollowSys);
    }


}
