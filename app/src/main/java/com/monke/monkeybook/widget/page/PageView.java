package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.google.android.material.snackbar.Snackbar;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.widget.page.animation.CoverPageAnim;
import com.monke.monkeybook.widget.page.animation.Direction;
import com.monke.monkeybook.widget.page.animation.HorizonPageAnim;
import com.monke.monkeybook.widget.page.animation.NonePageAnim;
import com.monke.monkeybook.widget.page.animation.PageAnimation;
import com.monke.monkeybook.widget.page.animation.SimulationPageAnim;
import com.monke.monkeybook.widget.page.animation.SlidePageAnim;

import java.lang.ref.WeakReference;


/**
 * 绘制页面显示内容的类
 */
public class PageView extends View {

    private WeakReference<ReadBookActivity> activity;

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高

    private int mStartX = 0;
    private int mStartY = 0;
    private boolean isMove = false;
    private int mPageIndex;
    private int mChapterIndex;
    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private Rect mCenterRect = null;
    private boolean isLayoutPrepared;
    // 动画类
    private PageAnimation mPageAnim;
    private boolean drawAfterComputeScroll = false;

    private int mTouchSlop;

    // 动画监听类
    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext() {
            return PageView.this.hasNextPage();
        }

        @Override
        public void pageCancel() {
            PageView.this.pageCancel();
        }
    };

    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;

    private Snackbar mSnackbar;

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        isLayoutPrepared = true;

        if (mPageLoader != null) {
            mPageLoader.setDisplay(w, h);
        }
    }

    //设置翻页的模式
    private void setPageMode(PageMode pageMode) {
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0 || mPageLoader == null) return;
        switch (pageMode) {
            case SIMULATION:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case COVER:
                mPageAnim = new CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SLIDE:
                mPageAnim = new SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case NONE:
                mPageAnim = new NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            default:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
    }

    public ReadBookActivity getActivity() {
        return activity.get();
    }

    void resetPageAnim(PageMode pageMode) {
        if (mPageAnim == null || mPageAnim.getPageMode() != pageMode) {
            setPageMode(pageMode);
        } else {
            mPageAnim.init(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
    }


    public Bitmap getNextBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getNextBitmap();
    }

    public Bitmap getCurrentBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getCurrentBitmap();
    }

    Direction getAnimDirection() {
        if (mPageAnim == null) return null;
        return mPageAnim.getDirection();
    }

    public void autoPrevPage() {
        if (mPageAnim == null) return;
        mPageAnim.startAnim(Direction.PREV);
    }

    public void autoNextPage() {
        if (mPageAnim == null) return;
        mPageAnim.startAnim(Direction.NEXT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制动画
        if (mPageAnim != null) {
            mPageAnim.draw(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPageAnim == null || mPageAnim.isStarted()) {
            return true;
        }

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                isMove = false;
                if (mTouchListener != null) {
                    canTouch = mTouchListener.onTouch();
                }
                if (mPageLoader.isPageScrollable()) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                if (!isMove) {
                    isMove = Math.abs(mStartX - x) > mTouchSlop || Math.abs(mStartY - y) > mTouchSlop;
                }

                if (mPageLoader.isPageScrollable() && isMove) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new Rect(mViewWidth / 3, mViewHeight / 3,
                                mViewWidth * 2 / 3, mViewHeight * 2 / 3);
                    }

                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (mTouchListener != null) {
                            mTouchListener.center();
                        }
                        return true;
                    }

                    if (!ReadBookControl.getInstance().getCanClickTurn()) {
                        return true;
                    }
                }
                if (mPageLoader.isPageScrollable()) {
                    mPageAnim.onTouchEvent(event);
                }
                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     */
    public boolean hasPrevPage() {
        if (mPageLoader.prevPage()) {
            return true;
        } else {
            if (mSnackbar == null) {
                mSnackbar = getActivity().getSnackBar("");
            }

            if (!mSnackbar.isShown()) {
                mSnackbar.setText("没有上一页");
                mSnackbar.show();
            }
            return false;
        }
    }

    /**
     * 判断是否下一页存在
     */
    public boolean hasNextPage() {
        if (mPageLoader.nextPage()) {
            return true;
        } else {
            if (mSnackbar == null) {
                mSnackbar = getActivity().getSnackBar("");
            }

            if (!mSnackbar.isShown()) {
                mSnackbar.setText("没有下一页");
                mSnackbar.show();
            }
            return false;
        }
    }

    private void pageCancel() {
        mPageLoader.pageCancel();
    }

    private void drawPageComputeScroll() {
        if (drawAfterComputeScroll) {
            drawPage(true);
            drawAfterComputeScroll = false;
        }
    }

    @Override
    public void computeScroll() {
        //进行滑动
        if (mPageAnim == null) return;
        mPageAnim.scrollAnim();
        if (mPageAnim.isStarted() && !mPageAnim.getScroller().computeScrollOffset()) {
            mPageAnim.resetAnim();
            drawPageComputeScroll();
            if (mPageLoader.getPagePosition() != mPageIndex | mPageLoader.getChapterPosition() != mChapterIndex) {
                mPageLoader.dispatchPagingEndEvent();
            }
        }
    }

    public void upPagePos(int chapterPos, int pagePos) {
        mChapterIndex = chapterPos;
        mPageIndex = pagePos;
    }

    public boolean isRunning() {
        return mPageAnim != null && mPageAnim.isRunning();
    }

    public boolean isLayoutPrepared() {
        return isLayoutPrepared;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }


    public void postDrawPage() {
        drawAfterComputeScroll = true;
    }


    public void changePage() {
        if (mPageAnim instanceof HorizonPageAnim) {
            ((HorizonPageAnim) mPageAnim).changePage();
        }
    }

    public void drawPage() {
        drawPage(false);
    }

    public void drawPage(boolean changePage) {
        if (!isLayoutPrepared) return;
        if (mPageLoader != null) {
            if (changePage) {
                changePage();
            }
            mPageLoader.drawPage(getNextBitmap());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPageAnim != null) {
            mPageAnim.abortAnim();
            mPageAnim.clear();
        }

        mPageLoader = null;
        mPageAnim = null;
    }

    /**
     * 获取 PageLoader
     */
    public PageLoader getPageLoader(ReadBookActivity activity, BookShelfBean collBook) {
        this.activity = new WeakReference<>(activity);

        if (collBook.isLocalBook()) {
            mPageLoader = new LocalPageLoader(this, collBook);
        } else {
            mPageLoader = new NetPageLoader(this, collBook);
        }

        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.setDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }

    public interface TouchListener {
        boolean onTouch();

        void center();
    }
}