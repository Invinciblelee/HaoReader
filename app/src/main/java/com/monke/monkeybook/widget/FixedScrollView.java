package com.monke.monkeybook.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class FixedScrollView extends ScrollView {
    public FixedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setVerticalScrollBarEnabled(false);
        setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        setWillNotDraw(false);
    }
}
