package com.monke.monkeybook.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.monke.monkeybook.R;

public class LabelView extends AppCompatTextView {

    private Paint mPaint;
    private int mColor;
    private int mSinkWidth;

    private Path mPath;

    public LabelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LabelView, defStyleAttr, 0);
        mColor = a.getColor(R.styleable.LabelView_labelColor, Color.WHITE);
        mSinkWidth = a.getDimensionPixelSize(R.styleable.LabelView_labelSinkWidth, 10);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        float width = getWidth();
        float height = getHeight();

        if (mPath == null) {
            mPath = new Path();
        } else {
            mPath.reset();
        }

        mPath.moveTo(0, 0);
        mPath.lineTo(width, 0);
        mPath.lineTo(width - mSinkWidth, height / 2);
        mPath.lineTo(width, height);
        mPath.lineTo(0, height);
        mPath.close();

        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        super.onDraw(canvas);
    }
}
