package com.monke.monkeybook.widget.refreshview.scroller;

import android.content.Context;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class TopLinearSmoothScroller extends LinearSmoothScroller {
    public TopLinearSmoothScroller(Context context) {
        super(context);
    }
    
    @Override
    public int getVerticalSnapPreference() {
        return SNAP_TO_START;
    }
}