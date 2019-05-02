package com.monke.monkeybook.widget.page.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;

import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

/**
 * Created by newbiechen on 17-7-24.
 */

public class CoverPageAnim extends HorizonPageAnim {

    private Rect mSrcRect, mDestRect;
    private GradientDrawable mBackShadowDrawableLR;

    public CoverPageAnim(int w, int h, PageView view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @Override
    public void init(int w, int h, PageView view, OnPageChangeListener listener) {
        super.init(w, h, view, listener);
        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        int[] mBackShadowColors = new int[]{0x66111111, 0x00000000};
        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    @Override
    public void drawMove(Canvas canvas) {
        int dis;
        if (mDirection == Direction.NEXT) {
            dis = (int) (mViewWidth - mStartX + mTouchX);
            if (dis > mViewWidth) {
                dis = mViewWidth;
            }
            //计算bitmap截取的区域
            mSrcRect.left = mViewWidth - dis;
            //计算bitmap在canvas显示的区域
            mDestRect.right = dis;
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
            canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
            addShadow(dis, canvas);
        } else {
            dis = (int) (mTouchX - mStartX);
            if (dis > mViewWidth) {
                dis = mViewWidth;
            }
            mSrcRect.left = mViewWidth - dis;
            mDestRect.right = dis;
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
            canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null);
            addShadow(dis, canvas);
        }
    }

    //添加阴影
    private void addShadow(int left, Canvas canvas) {
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, mViewHeight);
        mBackShadowDrawableLR.draw(canvas);
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
                    dx = (int) -(mTouchX - mStartX);
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
        return PageMode.COVER;
    }
}
