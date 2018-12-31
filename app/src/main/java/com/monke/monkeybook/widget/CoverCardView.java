package com.monke.monkeybook.widget;

import android.content.Context;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;

public class CoverCardView extends CardView {



    public CoverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = measuredWidth * 7 / 5;
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
    }


}
