package com.monke.monkeybook.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class SquareImageView extends CircleImageView {

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
