package com.monke.monkeybook.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
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
    @BindView(R.id.ll_title)
    LinearLayout titleView;
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
        updateTitle(durChapterName);
        updatePageIndex(durPage, pageSize);
        updateChapterIndex(durChapter, chapterSize);
    }

    public void setShowTimeBattery(boolean showTimeBattery) {
        this.showTimeBattery = showTimeBattery;
        if (showTimeBattery) {
            tvTime.setVisibility(VISIBLE);
            batteryProgress.setVisibility(VISIBLE);
            if (batteryTimeView.indexOfChild(tvPageIndex) >= 0) {
                batteryTimeView.removeView(tvPageIndex);
                titleView.addView(tvPageIndex);
            }
        } else {
            tvTime.setVisibility(GONE);
            batteryProgress.setVisibility(GONE);

            if (batteryTimeView.indexOfChild(tvPageIndex) < 0) {
                titleView.removeView(tvPageIndex);
                batteryTimeView.addView(tvPageIndex);
            }
        }
        updateTime();
        updateBattery(batteryLevel);
        updatePageIndex(durPage, pageSize);
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

    public void updateTitle(String durChapterName) {
        if (durChapterName == null) {
            return;
        }

        this.durChapterName = durChapterName;

        tvTitle.setText(durChapterName);
    }

    public void updatePageIndex(int durPage, int pageSize) {
        this.durPage = durPage;
        this.pageSize = pageSize;

        if (pageSize > 0) {
            tvPageIndex.setText(String.format(Locale.getDefault(), showTimeBattery ? "【%d/%d】" : "%d/%d", durPage, pageSize));
        } else {
            tvPageIndex.setText(null);
        }
    }

    public void updateChapterIndex(int durChapter, int chapterSize) {
        this.durChapter = durChapter;
        this.chapterSize = chapterSize;

        if(chapterSize > 0) {
            tvChapterIndex.setText(String.format(Locale.getDefault(), "%d/%d章", durChapter, chapterSize));
        }else {
            tvChapterIndex.setText(null);
        }
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

    public void refreshUI(ReadBookControl readConfig) {
        boolean hideStatusBar = readConfig.getHideStatusBar();
        if (hideStatusBar) {
            setShowTimeBattery(readConfig.getShowTimeBattery());
        } else {
            setShowTimeBattery(false);
        }
        updateTextColor(readConfig.getTextColor());
    }

    public void updateOnPageChanged(BookShelfBean bookShelfBean) {
        if (bookShelfBean == null) {
            return;
        }

        if(getVisibility() != VISIBLE){
            setVisibility(VISIBLE);
        }

        updateTitle(bookShelfBean.getDurChapterName());
        updateChapterIndex(bookShelfBean.getDurChapter() + 1, bookShelfBean.getChapterListSize());
        updatePageIndex(bookShelfBean.getDurChapterPage() + 1, bookShelfBean.getDurChapterPageSize());
    }
}