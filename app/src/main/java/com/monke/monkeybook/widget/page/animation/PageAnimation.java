package com.monke.monkeybook.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

import androidx.annotation.CallSuper;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

/**
 * Created by newbiechen on 17-7-24.
 * 翻页动画抽象类
 */

public abstract class PageAnimation {
    //正在使用的View
    protected PageView mView;
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

    PageAnimation(int w, int h, PageView view, OnPageChangeListener listener) {
        init(w, h, view, listener);
    }

    @CallSuper
    public void init(int w, int h, PageView view, OnPageChangeListener listener) {
        mViewWidth = w;
        mViewHeight = h;

        mView = view;
        mListener = listener;

        mScroller = new Scroller(mView.getContext(), new FastOutLinearInInterpolator());
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
        mView.postInvalidate();
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

    public abstract PageMode getPageMode();

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
     * 自动加载动画
     */
    public abstract void startAnim(Direction direction);

    /**
     * 获取内容显示版面
     */
    public abstract Bitmap getNextBitmap();

    public abstract Bitmap getCurrentBitmap();

    public interface OnPageChangeListener {
        boolean hasPrev();

        boolean hasNext();

        void pageCancel();
    }
}
