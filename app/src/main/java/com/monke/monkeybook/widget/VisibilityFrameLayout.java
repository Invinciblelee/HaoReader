package com.monke.monkeybook.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VisibilityFrameLayout extends FrameLayout {

    private OnVisibilityChangeListener mVisibilityChangeListener;


    public void setOnVisibilityChangeListener(OnVisibilityChangeListener visibilityChangeListener) {
        this.mVisibilityChangeListener = visibilityChangeListener;
    }

    public VisibilityFrameLayout(@NonNull Context context) {
        super(context);
    }

    public VisibilityFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VisibilityFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(changedView == this && mVisibilityChangeListener != null){
            mVisibilityChangeListener.onChanged(visibility);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    public interface OnVisibilityChangeListener{
        void onChanged(int visibility);
    }
}
