package com.monke.monkeybook.widget.page.animation;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

/**
 * Created by newbiechen on 17-7-24.
 */

public class SlidePageAnim extends HorizonPageAnim {
    private Rect mSrcRect, mDestRect, mNextSrcRect, mNextDestRect;

    public SlidePageAnim(int w, int h, PageView view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @Override
    public void init(int w, int h, PageView view, OnPageChangeListener listener) {
        super.init(w, h, view, listener);
        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
    }

    @Override
    public void drawMove(Canvas canvas) {
        int dis;
        switch (mDirection) {
            case NEXT:
                //左半边的剩余区域
                dis = (int) (mViewWidth - mStartX + mTouchX);
                if (dis > mViewWidth) {
                    dis = mViewWidth;
                }
                //计算bitmap截取的区域
                mSrcRect.left = mViewWidth - dis;
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis;
                //计算下一页截取的区域
                mNextSrcRect.right = mViewWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mNextBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
                break;
            default:
                dis = (int) (mTouchX - mStartX);
                if (dis < 0) {
                    dis = 0;
                    mStartX = mTouchX;
                }
                mSrcRect.left = mViewWidth - dis;
                mDestRect.right = dis;

                //计算下一页截取的区域
                mNextSrcRect.right = mViewWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mCurBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null);
                break;
        }
    }

    @Override
    public void startAnim() {
        int dx;
        switch (mDirection) {
            case NEXT:
                if (isCancel) {
                    int dis = (int) ((mViewWidth - mStartX) + mTouchX);
                    if (dis > mViewWidth) {
                        dis = mViewWidth;
                    }
                    dx = mViewWidth - dis;
                } else {
                    dx = (int) -(mTouchX + (mViewWidth - mStartX));
                }
                break;
            default:
                if (isCancel) {
                    dx = (int) -Math.abs(mTouchX - mStartX);
                } else {
                    dx = (int) (mViewWidth - (mTouchX - mStartX));
                }
                break;
        }

        int animationSpeed = ReadBookControl.getInstance().getAnimSpeed();
        //滑动速度保持一致
        int duration = (animationSpeed * Math.abs(dx)) / mViewWidth;
        mScroller.startScroll((int) mTouchX, 0, dx, 0, duration);
        super.startAnim();
    }

    @Override
    public PageMode getPageMode() {
        return PageMode.SLIDE;
    }
}
