package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 阅读状态条
 */
public class ReadBottomStatusBar extends FrameLayout {

    @BindView(R.id.ll_battery_time)
    LinearLayout batteryTimeView;
    @BindView(R.id.batteryProgress)
    BatteryView batteryProgress;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_title_left)
    TextView tvTitleLeft;
    @BindView(R.id.tv_chapter_index)
    TextView tvChapterIndex;
    @BindView(R.id.line)
    View lineView;

    private boolean showTimeBattery;

    private int batteryLevel;

    private String durChapterName;
    private int durPage;
    private int pageSize;
    private int durChapter;
    private int chapterSize;

    private final Runnable updatePaddingRunnable = this::updateTitlePadding;

    public ReadBottomStatusBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable superData = super.onSaveInstanceState();
        bundle.putParcelable("super_data", superData);
        bundle.putBoolean("showTimeBattery", showTimeBattery);
        bundle.putInt("batteryLevel", batteryLevel);
        bundle.putString("durChapterName", durChapterName);
        bundle.putInt("durPage", durPage);
        bundle.putInt("pageSize", pageSize);
        bundle.putInt("durChapter", durChapter);
        bundle.putInt("chapterSize", chapterSize);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        showTimeBattery = bundle.getBoolean("showTimeBattery");
        batteryLevel = bundle.getInt("batteryLevel", batteryLevel);
        durChapterName = bundle.getString("durChapterName");
        durPage = bundle.getInt("durPage");
        pageSize = bundle.getInt("pageSize");
        durChapter = bundle.getInt("durChapter");
        chapterSize = bundle.getInt("chapterSize");
        super.onRestoreInstanceState(bundle.getParcelable("super_data"));

        updateTime();
        setShowTimeBattery(showTimeBattery);
        updatePageIndex(durChapterName, durPage, pageSize);
        updateChapterIndex(durChapter, chapterSize);
    }

    public void setShowTimeBattery(boolean showTimeBattery) {
        this.showTimeBattery = showTimeBattery;
        if (showTimeBattery) {
            tvTime.setVisibility(VISIBLE);
            batteryProgress.setVisibility(VISIBLE);
            tvTitleLeft.setVisibility(GONE);
            tvTitle.setVisibility(VISIBLE);

            updateTime();
            updateBattery(batteryLevel);
            updatePageIndex(durChapterName, durPage, pageSize);
        } else {
            tvTime.setVisibility(GONE);
            batteryProgress.setVisibility(GONE);
            tvTitleLeft.setVisibility(VISIBLE);
            tvTitle.setVisibility(GONE);
        }
    }

    public void updateTime() {
        String time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME);
        tvTime.setText(time);
    }

    public void updateBattery(int batteryLevel) {
        if (batteryLevel != 0) {
            this.batteryLevel = batteryLevel;
            batteryProgress.setProgress(batteryLevel);
        }
    }


    public void updatePageIndex(String durChapterName, int durPage, int durPageSize) {
        this.durChapterName = durChapterName;
        this.durPage = durPage;
        this.pageSize = durPageSize;

        tvTitle.setText(formatTitle(durChapterName, durPage, durPageSize));
        tvTitleLeft.setText(tvTitle.getText());

        removeCallbacks(updatePaddingRunnable);
        post(updatePaddingRunnable);

        if (!TextUtils.isEmpty(tvTitle.getText()) && getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
    }

    public void updateChapterIndex(int durChapter, int durChapterSize) {
        this.durChapter = durChapter;
        this.chapterSize = durChapterSize;

        tvChapterIndex.setText(String.format(Locale.getDefault(), "%d/%d章", durChapter, durChapterSize));

        removeCallbacks(updatePaddingRunnable);
        post(updatePaddingRunnable);
    }

    public void updateTextColor(int color) {
        tvTime.setTextColor(color);
        tvTitle.setTextColor(color);
        tvTitleLeft.setTextColor(color);
        tvChapterIndex.setTextColor(color);
        lineView.setBackgroundColor(color);
    }

    public void updateBatteryColor(int color) {
        batteryProgress.setColor(color);
    }

    public void updateTextTypeface(String fontPath, boolean bold) {
        Typeface typeface;
        try {
            if (fontPath != null) {
                typeface = Typeface.createFromFile(fontPath);
            } else {
                typeface = Typeface.SANS_SERIF;
            }
        } catch (Exception e) {
            typeface = Typeface.SANS_SERIF;
        }

        tvTime.setTypeface(typeface);
        tvTitle.setTypeface(typeface);
        tvTitleLeft.setTypeface(typeface);
        tvChapterIndex.setTypeface(typeface);
        tvTime.getPaint().setFakeBoldText(bold);
        tvTitle.getPaint().setFakeBoldText(bold);
        tvTitleLeft.getPaint().setFakeBoldText(bold);
        tvChapterIndex.getPaint().setFakeBoldText(bold);
    }

    public void refreshUI() {
        ReadBookControl readConfig = ReadBookControl.getInstance();
        boolean hideStatusBar = readConfig.getHideStatusBar();
        if (hideStatusBar) {
            setShowTimeBattery(readConfig.getShowTimeBattery());
        } else {
            setShowTimeBattery(false);
        }
        updateTextColor(readConfig.getTextColor());
        updateTextTypeface(readConfig.getFontPath(), readConfig.getTextBold());
        setPadding(ScreenUtils.dpToPx(readConfig.getPaddingLeft()), 0, ScreenUtils.dpToPx(readConfig.getPaddingRight()), 0);
        updateBatteryColor(readConfig.getTextColor());
        batteryProgress.setShowBatteryNumber(readConfig.getShowBatteryNumber());
        lineView.setVisibility(readConfig.getShowBottomLine() ? View.VISIBLE : View.GONE);
    }

    public void updatePadding() {
        ReadBookControl readConfig = ReadBookControl.getInstance();
        setPadding(ScreenUtils.dpToPx(readConfig.getPaddingLeft()), 0, ScreenUtils.dpToPx(readConfig.getPaddingRight()), 0);
    }

    private void updateTitlePadding() {
        final int extraOffset = ScreenUtils.dpToPx(16);
        int left = batteryTimeView.getWidth();
        int right = tvChapterIndex.getWidth();
        final int horPadding = Math.max(left, right) + extraOffset;
        tvTitle.setPaddingRelative(horPadding, 0, horPadding, 0);
        tvTitleLeft.setPaddingRelative(0, 0, right + extraOffset, 0);
    }

    public void updateChapterInfo(BookShelfBean bookShelfBean, int durPageSize) {
        if (bookShelfBean == null) {
            return;
        }

        updatePageIndex(bookShelfBean.getDisplayDurChapterName(), bookShelfBean.getDurChapterPage() + 1, durPageSize);
        updateChapterIndex(bookShelfBean.getDurChapter() + 1, bookShelfBean.getChapterListSize());
    }

    private String formatTitle(String titleStr, int durPage, int durPageSize) {
        if (TextUtils.isEmpty(titleStr)) {
            return "";
        }
        return titleStr + String.format(Locale.getDefault(), " (%d/%d)", durPageSize == 0 ? 1 : durPage, durPageSize == 0 ? 1 : durPageSize);
    }
}