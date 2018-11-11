package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    ProgressBar batteryProgress;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_page_index)
    TextView tvPageIndex;
    @BindView(R.id.tv_chapter_index)
    TextView tvChapterIndex;

    private boolean showTimeBattery;

    private int batteryLevel;

    private String durChapterName;
    private int durPage;
    private int pageSize;
    private int durChapter;
    private int chapterSize;


    public ReadBottomStatusBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(ScreenUtils.getStatusBarHeight(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
            tvPageIndex.setVisibility(GONE);
            tvTitle.setVisibility(VISIBLE);

            updateTime();
            updateBattery(batteryLevel);
            updatePageIndex(durChapterName, durPage, pageSize);
        } else {
            tvTime.setVisibility(GONE);
            batteryProgress.setVisibility(GONE);
            tvPageIndex.setVisibility(VISIBLE);
            tvTitle.setVisibility(GONE);
        }
    }

    public void updateTime() {
        if (tvTime.getVisibility() == VISIBLE) {
            String time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME);
            tvTime.setText(time);
        }
    }

    public void updateBattery(int batteryLevel) {
        this.batteryLevel = batteryLevel;

        if (batteryProgress.getVisibility() == VISIBLE) {
            batteryProgress.setProgress(batteryLevel);
        }
    }


    public void updatePageIndex(String durChapterName, int durPage, int durPageSize) {
        this.durChapterName = durChapterName;
        this.durPage = durPage;
        this.pageSize = durPageSize;

        if (showTimeBattery) {
            tvTitle.setText(formatTitle(durChapterName, durPage, durPageSize));
        } else {
            tvPageIndex.setText(formatTitle(durChapterName, durPage, durPageSize));
        }
    }

    public void updateChapterIndex(int durChapter, int durChapterSize) {
        this.durChapter = durChapter;
        this.chapterSize = durChapterSize;

        tvChapterIndex.setText(String.format(Locale.getDefault(), "%d/%d章", durChapter, durChapterSize));
    }

    public void updateTextColor(int color) {
        tvTime.setTextColor(color);
        tvTitle.setTextColor(color);
        tvPageIndex.setTextColor(color);
        tvChapterIndex.setTextColor(color);

        AppCompat.setTint(batteryProgress.getBackground(), color);

        LayerDrawable drawable = (LayerDrawable) batteryProgress.getProgressDrawable();
        ClipDrawable progressDrawable = (ClipDrawable) drawable.findDrawableByLayerId(android.R.id.progress);
        progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void updateTextTypeface(String fontPath) {
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
        tvPageIndex.setTypeface(typeface);
        tvChapterIndex.setTypeface(typeface);
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
        updateTextTypeface(readConfig.getFontPath());
        setPadding(ScreenUtils.dpToPx(readConfig.getPaddingLeft()), 0, ScreenUtils.dpToPx(readConfig.getPaddingRight()), 0);
    }

    public void updatePadding() {
        ReadBookControl readConfig = ReadBookControl.getInstance();
        setPadding(ScreenUtils.dpToPx(readConfig.getPaddingLeft()), 0, ScreenUtils.dpToPx(readConfig.getPaddingRight()), 0);
    }

    public void updateChapterInfo(BookShelfBean bookShelfBean, int durPageSize) {
        if (bookShelfBean == null) {
            return;
        }

        updatePageIndex(bookShelfBean.getDurChapterName(), bookShelfBean.getDurChapterPage() + 1, durPageSize);
        updateChapterIndex(bookShelfBean.getDurChapter() + 1, bookShelfBean.getChapterListSize());

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
    }

    private String formatTitle(String titleStr, int durPage, int durPageSize) {
        if (TextUtils.isEmpty(titleStr)) {
            return "";
        }

        int maxLength = 14;
        ReadBookControl readBookControl = ReadBookControl.getInstance();
        if(!showTimeBattery){
            maxLength = 16;
        }else if (readBookControl.getPaddingLeft() > 30 || readBookControl.getPaddingRight() > 30) {
            maxLength = 10;
        }

        StringBuilder title = new StringBuilder();
        if (titleStr.length() > maxLength) {
            title.append(titleStr.substring(0, maxLength / 2))
                    .append("\u2026")
                    .append(titleStr.substring(titleStr.length() - maxLength / 2, titleStr.length()));
        } else {
            title.append(titleStr);
        }
        title.append(String.format(Locale.getDefault(), " (%d/%d)", durPageSize == 0 ? 1 : durPage, durPageSize == 0 ? 1 : durPageSize));
        return title.toString();
    }
}