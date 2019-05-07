package com.monke.monkeybook.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.CallSuper;

import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

/**
 * Created by newbiechen on 17-7-24.
 * 横向动画的模板
 */

public abstract class HorizonPageAnim extends PageAnimation implements GestureDetector.OnGestureListener {
    //动画速度
    Bitmap mCurBitmap;
    Bitmap mNextBitmap;

    //是否移动了
    private boolean isMove = false;
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private boolean isNext = false;

    //是否没下一页或者上一页
    private boolean noNext = false;

    private GestureDetector mDetector;

    HorizonPageAnim(int w, int h, PageView view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @CallSuper
    @Override
    public void init(int w, int h, PageView view, OnPageChangeListener listener) {
        super.init(w, h, view, listener);
        //创建图片
        if (mCurBitmap == null || mCurBitmap.isRecycled()) {
            mCurBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        }

        if (mNextBitmap == null || mNextBitmap.isRecycled()) {
            mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        }

        mDetector = new GestureDetector(view.getContext(), this);
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    public void changePage() {
        Bitmap bitmap = mCurBitmap;
        mCurBitmap = mNextBitmap;
        mNextBitmap = bitmap;
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isMove && event.getAction() == MotionEvent.ACTION_UP) {
                // 是否取消翻页
                if (isCancel) {
                    mListener.pageCancel();
                }

                // 开启翻页效果
                if (!noNext) {
                    startAnim();
                }
            return true;
        }
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //是否移动
        isMove = false;
        //是否存在下一章
        noNext = false;
        //是下一章还是前一章
        isNext = false;
        //是否正在执行动画
        isRunning = false;
        //取消
        isCancel = false;
        //设置起始位置的触摸点
        setStartPoint(e.getX(), e.getY());
        abortAnim();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        isNext = x > mViewWidth / 2 || ReadBookControl.getInstance().getClickAllNext();
        if (isNext) {
            //判断是否下一页存在
            boolean hasNext = mListener.hasNext();
            //设置动画方向
            setDirection(Direction.NEXT);
            if (!hasNext) {
                return true;
            }
        } else {
            boolean hasPrev = mListener.hasPrev();
            setDirection(Direction.PREV);
            if (!hasPrev) {
                return true;
            }
        }
        setTouchPoint(x, y);
        startAnim();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isMove) {
            if (distanceX < 0) {
                //上一页的参数配置
                isNext = false;
                boolean hasPrev = mListener.hasPrev();
                setDirection(Direction.PREV);
                //如果上一页不存在
                if (!hasPrev) {
                    noNext = true;
                    return true;
                }
            } else {
                //进行下一页的配置
                isNext = true;
                //判断是否下一页存在
                boolean hasNext = mListener.hasNext();
                //如果存在设置动画方向
                setDirection(Direction.NEXT);

                //如果不存在表示没有下一页了
                if (!hasNext) {
                    noNext = true;
                    return true;
                }
            }
            isMove = true;
        }
        isCancel = isNext ? distanceX < 0 : distanceX > 0;
        isRunning = true;
        //设置触摸点
        setTouchPoint(e2.getX(), e2.getY());
        mView.invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRunning) {
            drawMove(canvas);
        } else {
            if (isCancel) {
                mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true);
                canvas.drawBitmap(mCurBitmap, 0, 0, null);
            } else {
                canvas.drawBitmap(mNextBitmap, 0, 0, null);
            }
        }
    }

    @Override
    public void scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            setTouchPoint(x, y);

            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isRunning = false;
            }
            mView.invalidate();
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
            setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            mView.invalidate();
        }
    }


    @Override
    public void startAnim(Direction direction) {
        if (isStarted) return;
        if (direction == Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            if (getPageMode() == PageMode.SIMULATION) {
                y = y * 2 / 3;
            }
            //初始化动画
            setStartPoint(x, y);
            //设置点击点
            setTouchPoint(x, y);
            //设置方向
            boolean hasNext = mView.hasNextPage();

            setDirection(direction);
            if (!hasNext) {
                return;
            }
        } else {
            int x = 0;
            int y = mViewHeight;
            //初始化动画
            setStartPoint(x, y);
            //设置点击点
            setTouchPoint(x, y);
            setDirection(direction);
            //设置方向方向
            boolean hashPrev = mView.hasPrevPage();
            if (!hashPrev) {
                return;
            }
        }
        startAnim();
    }

    @Override
    public Bitmap getNextBitmap() {
        return mNextBitmap;
    }

    @Override
    public Bitmap getCurrentBitmap() {
        return mCurBitmap;
    }
}
