package com.monke.monkeybook.widget;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ContextUtils;
import com.monke.monkeybook.utils.ScreenUtils;

public class DialogContainerLayout extends CardView {

    private int mMaxHeight;
    private int mMaxWidth;

    public DialogContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int statusBarHeight = ScreenUtils.getStatusBarHeight();
        int offset = getResources().getDimensionPixelSize(R.dimen.alert_dialog_spacing) * 2;
        mMaxHeight = screenHeight - statusBarHeight - offset;
        mMaxWidth = screenWidth - offset;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(width, mMaxWidth), MeasureSpec.EXACTLY);

        View child = getChildAt(0);
        if(child != null && child.getLayoutParams().height != ViewGroup.LayoutParams.WRAP_CONTENT){
            final int height;
            if(child.getLayoutParams().height > 0){
                height = child.getLayoutParams().height;
            }else {
                height = MeasureSpec.getSize(heightMeasureSpec);
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(mMaxHeight, height), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 2) {
            throw new IllegalStateException("DialogContainerLayout can host only one direct child");
        }

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 2) {
            throw new IllegalStateException("DialogContainerLayout can host only one direct child");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 2) {
            throw new IllegalStateException("DialogContainerLayout can host only one direct child");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 2) {
            throw new IllegalStateException("DialogContainerLayout can host only one direct child");
        }

        super.addView(child, index, params);
    }

}
