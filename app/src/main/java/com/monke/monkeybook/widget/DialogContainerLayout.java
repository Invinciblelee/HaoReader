package com.monke.monkeybook.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ScreenUtils;

public class DialogContainerLayout extends CardView {

    private int mMaxHeight;
    private int mMaxWidth;

    public DialogContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int statusBarHeight = ScreenUtils.getStatusBarHeight();
        int offset = getResources().getDimensionPixelSize(R.dimen.alert_dialog_spacing) * 2;
        mMaxHeight = screenHeight - statusBarHeight - offset;
        mMaxWidth = screenWidth - offset;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(mMaxHeight, height), MeasureSpec.EXACTLY);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(width, mMaxWidth), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
