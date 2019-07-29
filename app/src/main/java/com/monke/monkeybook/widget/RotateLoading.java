package com.monke.monkeybook.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.DensityUtil;

public class RotateLoading extends View {

    private static final int DEFAULT_WIDTH = 3;
    private static final int DEFAULT_SHADOW_POSITION = 2;
    private static final int DEFAULT_SPEED_OF_DEGREE = 10;

    private Paint mPaint;

    private RectF loadingRectF;
    private RectF shadowRectF;

    private int topDegree = 10;
    private int bottomDegree = 190;

    private float arc;

    private int width;

    private boolean changeBigger = true;

    private int shadowPosition;

    private boolean isStart = false;

    private int color;

    private int speedOfDegree;

    private float speedOfArc;

    private final Runnable mShown = this::startInternal;

    private final Runnable mHidden = this::stopInternal;

    public RotateLoading(Context context) {
        super(context);
        initView(context, null);
    }

    public RotateLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public RotateLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        color = getResources().getColor(R.color.colorAccent);
        width = DensityUtil.dp2px(context, DEFAULT_WIDTH);
        shadowPosition = DensityUtil.dp2px(getContext(), DEFAULT_SHADOW_POSITION);
        speedOfDegree = DEFAULT_SPEED_OF_DEGREE;

        if (null != attrs) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RotateLoading);
            color = typedArray.getColor(R.styleable.RotateLoading_loadingColor, color);
            width = typedArray.getDimensionPixelSize(R.styleable.RotateLoading_loadingWidth, DensityUtil.dp2px(context, DEFAULT_WIDTH));
            shadowPosition = typedArray.getInt(R.styleable.RotateLoading_shadowPosition, DEFAULT_SHADOW_POSITION);
            speedOfDegree = typedArray.getInt(R.styleable.RotateLoading_loadingSpeed, DEFAULT_SPEED_OF_DEGREE);
            typedArray.recycle();
        }
        speedOfArc = 1.0F * speedOfDegree / 4;
        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(width);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        arc = 10;

        loadingRectF = new RectF(2 * width, 2 * width, w - 2 * width, h - 2 * width);
        shadowRectF = new RectF(2 * width + shadowPosition, 2 * width + shadowPosition, w - 2 * width + shadowPosition, h - 2 * width + shadowPosition);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isStart) {
            return;
        }

        mPaint.setColor(Color.parseColor("#1a000000"));
        canvas.drawArc(shadowRectF, topDegree, arc, false, mPaint);
        canvas.drawArc(shadowRectF, bottomDegree, arc, false, mPaint);

        mPaint.setColor(color);
        canvas.drawArc(loadingRectF, topDegree, arc, false, mPaint);
        canvas.drawArc(loadingRectF, bottomDegree, arc, false, mPaint);

        topDegree += speedOfDegree;
        bottomDegree += speedOfDegree;
        if (topDegree > 360) {
            topDegree = topDegree - 360;
        }
        if (bottomDegree > 360) {
            bottomDegree = bottomDegree - 360;
        }

        if (changeBigger) {
            if (arc < 160) {
                arc += speedOfArc;
                invalidate();
            }
        } else {
            if (arc > speedOfDegree) {
                arc -= 2 * speedOfArc;
                invalidate();
            }
        }
        if (arc >= 160 || arc <= 10) {
            changeBigger = !changeBigger;
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == View.VISIBLE) {
            startInternal();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isStart = false;
        animate().cancel();
    }

    @Override
    public boolean isShown() {
        return super.isShown();
    }

    @Deprecated
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    public void setLoadingColor(int color) {
        this.color = color;
    }

    public int getLoadingColor() {
        return color;
    }

    public void show() {
        removeCallbacks(mShown);
        removeCallbacks(mHidden);
        post(mShown);
    }

    public void hide() {
        removeCallbacks(mShown);
        removeCallbacks(mHidden);
        post(mHidden);
    }

    private void startInternal() {
        startAnimator();

        isStart = true;
        invalidate();
    }

    private void stopInternal() {
        stopAnimator();
        invalidate();
    }

    public boolean isStart() {
        return isStart;
    }

    private void startAnimator() {
        animate().cancel();
        animate().scaleX(1.0f)
                .scaleY(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void stopAnimator() {
        animate().cancel();
        animate().scaleX(0.0f)
                .scaleY(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isStart = false;
                        setVisibility(View.GONE);
                    }
                })
                .start();
    }

}