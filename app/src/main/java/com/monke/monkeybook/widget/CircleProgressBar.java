package com.monke.monkeybook.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.monke.monkeybook.R;

public class CircleProgressBar extends View {

    private float mStrokeWidth;
    private int mStrokeColor;
    private float mMaxProgress;
    private float mProgress;

    private RectF mRingRect;
    private Paint mPaint;

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleProgressBar, 0, 0);
        mStrokeWidth = typeArray.getDimension(R.styleable.CircleProgressBar_strokeWidth, 10);
        mStrokeColor = typeArray.getColor(R.styleable.CircleProgressBar_strokeColor, 0xFFFFFFFF);
        mMaxProgress = typeArray.getFloat(R.styleable.CircleProgressBar_maxProgress, 100f);
        mProgress = typeArray.getFloat(R.styleable.CircleProgressBar_progress, 0f);
        typeArray.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mStrokeColor);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.BEVEL);
        mPaint.setStrokeCap(Paint.Cap.ROUND); //  设置笔触为圆形
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }

    public void setMaxProgress(float maxProgress) {
        this.mMaxProgress = maxProgress;
        invalidate();
    }

    public void setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (mRingRect == null) {
            mRingRect = new RectF();
        }

        mRingRect.set(mStrokeWidth / 2, mStrokeWidth / 2, width - mStrokeWidth / 2, height - mStrokeWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mRingRect, -90, mProgress / mMaxProgress * 360, false, mPaint);
    }
}
