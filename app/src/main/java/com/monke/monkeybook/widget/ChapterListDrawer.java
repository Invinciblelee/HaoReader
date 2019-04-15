package com.monke.monkeybook.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.monke.monkeybook.utils.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChapterListDrawer extends FrameLayout {

    private int mWidth;

    public ChapterListDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mWidth = screenWidth - ScreenUtils.dpToPx(48);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
