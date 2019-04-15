package com.monke.monkeybook.widget.page.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

import androidx.annotation.CallSuper;

/**
 * Created by newbiechen on 17-7-24.
 * 横向动画的模板
 */

public abstract class HorizonPageAnim extends PageAnimation {
    //动画速度
    Bitmap mCurBitmap;
    Bitmap mNextBitmap;

    //可以使用 mLast代替
    private int mMoveX = 0;
    private int mMoveY = 0;
    //是否移动了
    private boolean isMove = false;
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private boolean isNext = false;

    //是否没下一页或者上一页
    private boolean noNext = false;

    private boolean lockPage = false;

    private int mTouchSlop;

    HorizonPageAnim(int w, int h, PageView view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @CallSuper
    @Override
    public void init(int w, int h, PageView view, OnPageChangeListener listener) {
        super.init(w, h, view, listener);
        //创建图片
        mCurBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);
        mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565);

        mTouchSlop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    private void changePage() {
        Bitmap bitmap = mCurBitmap;
        mCurBitmap = mNextBitmap;
        mNextBitmap = bitmap;
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取点击位置
        int x = (int) event.getX();
        int y = (int) event.getY();
        //设置触摸点
        setTouchPoint(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //移动的点击位置
                mMoveX = 0;
                mMoveY = 0;
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
                setStartPoint(x, y);
                abortAnim();
                break;
            case MotionEvent.ACTION_MOVE:
                //判断是否移动了
                if (!isMove) {
                    isMove = Math.abs(mStartX - x) > mTouchSlop || Math.abs(mStartX - y) > mTouchSlop;
                }

                if (isMove) {
                    if (!lockPage) {
                        changePage();
                        lockPage = true;
                    }

                    //判断是否是准备移动的状态(将要移动但是还没有移动)
                    if (mMoveX == 0 && mMoveY == 0) {
                        //判断翻得是上一页还是下一页
                        if (x - mStartX > 0) {
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
                    } else {
                        //判断是否取消翻页
                        isCancel = isNext ? x - mMoveX > 0 : x - mMoveX < 0;
                    }

                    mMoveX = x;
                    mMoveY = y;
                    isRunning = true;
                    mView.postInvalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    isNext = x > mViewWidth / 2 || ReadBookControl.getInstance().getClickAllNext();

                    changePage();

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
                }

                // 是否取消翻页
                if (isCancel) {
                    mListener.pageCancel();
                }

                // 开启翻页效果
                if (!noNext) {
                    startAnim();
                }

                break;
        }
        return true;
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
            mView.postInvalidate();
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
            setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            mView.postInvalidate();
        }
    }

    @Override
    public void resetAnim() {
        super.resetAnim();
        lockPage = false;
    }

    @Override
    public void startAnim(Direction direction) {
        if (isStarted) return;
        changePage();
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
