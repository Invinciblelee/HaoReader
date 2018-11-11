package com.monke.monkeybook.widget.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.monke.monkeybook.help.ReadBookControl;

/**
 * Created by newbiechen on 17-7-24.
 * 翻页动画抽象类
 */

public abstract class PageAnimation {
    //正在使用的View
    protected View mView;
    //滑动装置
    protected Scroller mScroller;
    //监听器
    protected OnPageChangeListener mListener;
    //移动方向
    protected Direction mDirection = Direction.NONE;

    //是否取消翻页
    protected boolean isCancel = false;
    protected boolean isRunning = false;
    protected boolean isStarted = false;

    //屏幕的尺寸
    protected int mScreenWidth;
    protected int mScreenHeight;
    //屏幕的间距
    protected int mMarginWidth;
    protected int mMarginTop;
    protected int mMarginBottom;
    //视图的尺寸
    protected int mViewWidth;
    protected int mViewHeight;
    //起始点
    protected float mStartX;
    protected float mStartY;
    //触碰点
    protected float mTouchX;
    protected float mTouchY;
    //上一个触碰点
    protected float mLastX;
    protected float mLastY;

    PageAnimation(int w, int h, View view, OnPageChangeListener listener) {
        this(w, h, 0, 0, 0, view, listener);
    }

    PageAnimation(int w, int h, int marginWidth, int marginTop, int marginBottom, View view, OnPageChangeListener listener) {
        mScreenWidth = w;
        mScreenHeight = h;

        mMarginWidth = marginWidth;
        mMarginTop = marginTop;
        mMarginBottom = marginBottom;

        mViewWidth = mScreenWidth - mMarginWidth * 2;
        mViewHeight = mScreenHeight - mMarginTop - mMarginBottom;

        mView = view;
        mListener = listener;

        mScroller = new Scroller(mView.getContext(), new LinearOutSlowInInterpolator());
    }

    public Scroller getScroller() {
        return mScroller;
    }

    public void setStartPoint(float x, float y) {
        mStartX = x;
        mStartY = y;

        mLastX = mStartX;
        mLastY = mStartY;
    }

    public void setTouchPoint(float x, float y) {
        mLastX = mTouchX;
        mLastY = mTouchY;

        mTouchX = x;
        mTouchY = y;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void resetAnim() {
        isStarted = false;
        isCancel = false;
        isRunning = false;
        mDirection = Direction.NONE;
    }

    /**
     * 开启翻页动画
     */
    public void startAnim() {
        isStarted = true;
        isRunning = true;
        mView.invalidate();
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public void clear() {
        mView = null;
    }

    /**
     * 点击事件的处理
     */
    public abstract boolean onTouchEvent(MotionEvent event);

    /**
     * 绘制图形
     */
    public abstract void draw(Canvas canvas);

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    public abstract void scrollAnim();

    /**
     * 取消动画
     */
    public abstract void abortAnim();

    /**
     * 获取背景板
     */
    public abstract Bitmap getBgBitmap();

    /**
     * 获取内容显示版面
     */
    public abstract Bitmap getContentBitmap();

    public interface OnPageChangeListener {
        boolean hasPrev();

        boolean hasNext();

        void pageCancel();
    }
}
