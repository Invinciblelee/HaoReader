//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.monke.monkeybook.widget.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

public class SwipeRefreshLayout extends ViewGroup {
    private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();
    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 300L;
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5F;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0F;
    private static final float PROGRESS_BAR_HEIGHT = 4.0F;
    private static final float MAX_SWIPE_DISTANCE_FACTOR = 0.6F;
    private static final int REFRESH_TRIGGER_DISTANCE = 120;
    private static final int INVALID_POINTER = -1;
    private SwipeProgressBar mProgressBar;
    private View mTarget;
    private int mOriginalOffsetTop;
    private SwipeRefreshLayout.OnRefreshListener mListener;
    private int mFrom;
    private boolean mRefreshing;
    private int mTouchSlop;
    private float mDistanceToTriggerSync;
    private int mMediumAnimationDuration;
    private float mFromPercentage;
    private float mCurrPercentage;
    private int mProgressBarHeight;
    private int mCurrentTargetOffsetTop;
    private float mInitialMotionY;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId;
    private boolean mReturningToStart;
    private boolean mLayoutDraggingTogether;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private final AccelerateInterpolator mAccelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{16842766};
    private final Animation mAnimateToStartPosition;
    private Animation mShrinkTrigger;
    private final AnimationListener mReturnToStartPositionListener;
    private final AnimationListener mShrinkAnimationListener;
    private final Runnable mReturnToStartPosition;
    private final Runnable mCancel;

    public SwipeRefreshLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRefreshing = false;
        this.mDistanceToTriggerSync = -1.0F;
        this.mFromPercentage = 0.0F;
        this.mCurrPercentage = 0.0F;
        this.mActivePointerId = -1;
        this.mAnimateToStartPosition = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                int targetTop = 0;
                if (SwipeRefreshLayout.this.mFrom != SwipeRefreshLayout.this.mOriginalOffsetTop) {
                    targetTop = SwipeRefreshLayout.this.mFrom + (int) ((float) (SwipeRefreshLayout.this.mOriginalOffsetTop - SwipeRefreshLayout.this.mFrom) * interpolatedTime);
                }

                int offset = targetTop - SwipeRefreshLayout.this.mTarget.getTop();
                int currentTop = SwipeRefreshLayout.this.mTarget.getTop();
                if (offset + currentTop < 0) {
                    offset = 0 - currentTop;
                }

                SwipeRefreshLayout.this.setTargetOffsetTopAndBottom(offset);
            }
        };
        this.mShrinkTrigger = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float percent = SwipeRefreshLayout.this.mFromPercentage + (0.0F - SwipeRefreshLayout.this.mFromPercentage) * interpolatedTime;
                SwipeRefreshLayout.this.mProgressBar.setTriggerPercentage(percent);
            }
        };
        this.mReturnToStartPositionListener = new SwipeRefreshLayout.BaseAnimationListener() {
            public void onAnimationEnd(Animation animation) {
                SwipeRefreshLayout.this.mCurrentTargetOffsetTop = 0;
            }
        };
        this.mShrinkAnimationListener = new SwipeRefreshLayout.BaseAnimationListener() {
            public void onAnimationEnd(Animation animation) {
                SwipeRefreshLayout.this.mCurrPercentage = 0.0F;
            }
        };
        this.mReturnToStartPosition = new Runnable() {
            public void run() {
                SwipeRefreshLayout.this.mReturningToStart = true;
                if (SwipeRefreshLayout.this.mLayoutDraggingTogether) {
                    SwipeRefreshLayout.this.animateOffsetToStartPosition(SwipeRefreshLayout.this.mCurrentTargetOffsetTop + SwipeRefreshLayout.this.getPaddingTop(), SwipeRefreshLayout.this.mReturnToStartPositionListener);
                }
            }
        };
        this.mCancel = new Runnable() {
            public void run() {
                SwipeRefreshLayout.this.mReturningToStart = true;
                if (SwipeRefreshLayout.this.mProgressBar != null) {
                    SwipeRefreshLayout.this.mFromPercentage = SwipeRefreshLayout.this.mCurrPercentage;
                    SwipeRefreshLayout.this.mShrinkTrigger.setDuration((long) SwipeRefreshLayout.this.mMediumAnimationDuration);
                    SwipeRefreshLayout.this.mShrinkTrigger.setAnimationListener(SwipeRefreshLayout.this.mShrinkAnimationListener);
                    SwipeRefreshLayout.this.mShrinkTrigger.reset();
                    SwipeRefreshLayout.this.mShrinkTrigger.setInterpolator(SwipeRefreshLayout.this.mDecelerateInterpolator);
                    SwipeRefreshLayout.this.startAnimation(SwipeRefreshLayout.this.mShrinkTrigger);
                }

                if (SwipeRefreshLayout.this.mLayoutDraggingTogether) {
                    SwipeRefreshLayout.this.animateOffsetToStartPosition(SwipeRefreshLayout.this.mCurrentTargetOffsetTop + SwipeRefreshLayout.this.getPaddingTop(), SwipeRefreshLayout.this.mReturnToStartPositionListener);
                }
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = this.getResources().getInteger(android.R.integer.config_longAnimTime);
        this.setWillNotDraw(false);
        this.mProgressBar = new SwipeProgressBar(this);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        this.mProgressBarHeight = (int) (metrics.density * 4.0F);
        this.mDecelerateInterpolator = new DecelerateInterpolator(2.0F);
        this.mAccelerateInterpolator = new AccelerateInterpolator(1.5F);
        TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        this.setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.removeCallbacks(this.mCancel);
        this.removeCallbacks(this.mReturnToStartPosition);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeCallbacks(this.mReturnToStartPosition);
        this.removeCallbacks(this.mCancel);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration((long) this.mMediumAnimationDuration);
        this.mAnimateToStartPosition.setAnimationListener(listener);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        this.mTarget.startAnimation(this.mAnimateToStartPosition);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.mListener = listener;
    }

    private void setTriggerPercentage(float percent) {
        if (percent == 0.0F) {
            this.mCurrPercentage = 0.0F;
        } else {
            this.mCurrPercentage = percent;
            this.mProgressBar.setTriggerPercentage(percent);
        }
    }

    public void setLayoutDraggingTogether(boolean layoutDraggingTogether) {
        this.mLayoutDraggingTogether = layoutDraggingTogether;
    }

    public void startRefreshing(boolean callEvent) {
        setRefreshing(true);
        if (callEvent && this.mListener != null) {
            this.mListener.onRefresh();
        }
    }

    public void startRefreshing() {
        startRefreshing(true);
    }

    public void stopRefreshing() {
        setRefreshing(false);
    }

    private void setRefreshing(boolean refreshing) {
        if (this.mRefreshing != refreshing) {
            this.ensureTarget();
            this.mCurrPercentage = 0.0F;
            this.mRefreshing = refreshing;
            if (this.mRefreshing) {
                this.mProgressBar.start();
            } else {
                this.mProgressBar.stop();
            }
        }
    }

    public void setColorSchemeResources(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        Resources res = this.getResources();
        this.setColorSchemeColors(res.getColor(colorRes1), res.getColor(colorRes2), res.getColor(colorRes3), res.getColor(colorRes4));
    }

    public void setColorSchemeColors(int color1, int color2, int color3, int color4) {
        this.ensureTarget();
        this.mProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            if (this.getChildCount() > 1 && !this.isInEditMode()) {
                throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
            }

            this.mTarget = this.getChildAt(0);
            this.mOriginalOffsetTop = this.mTarget.getTop() + this.getPaddingTop();
        }

        if (this.mDistanceToTriggerSync == -1.0F && this.getParent() != null && ((View) this.getParent()).getHeight() > 0) {
            DisplayMetrics metrics = this.getResources().getDisplayMetrics();
            this.mDistanceToTriggerSync = (float) ((int) Math.min((float) ((View) this.getParent()).getHeight() * 0.6F, 120.0F * metrics.density));
        }

    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.mProgressBar.draw(canvas);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();
        this.mProgressBar.setBounds(0, 0, width, this.mProgressBarHeight);
        if (this.getChildCount() != 0) {
            View child = this.getChildAt(0);
            int childLeft = this.getPaddingLeft();
            int childTop = this.mCurrentTargetOffsetTop + this.getPaddingTop();
            int childWidth = width - this.getPaddingLeft() - this.getPaddingRight();
            int childHeight = height - this.getPaddingTop() - this.getPaddingBottom();
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.getChildCount() > 1 && !this.isInEditMode()) {
            throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
        } else {
            if (this.getChildCount() > 0) {
                this.getChildAt(0).measure(MeasureSpec.makeMeasureSpec(this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom(), MeasureSpec.EXACTLY));
            }

        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public boolean canChildScrollUp() {
        if (VERSION.SDK_INT < 14) {
            if (!(this.mTarget instanceof AbsListView)) {
                return this.mTarget.getScrollY() > 0;
            } else {
                AbsListView absListView = (AbsListView) this.mTarget;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            }
        } else {
            return this.mTarget.canScrollVertically(-1);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.ensureTarget();
        int action = ev.getActionMasked();
        if (!this.mRefreshing && this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }

        if (this.isEnabled() && !this.mReturningToStart && !this.canChildScrollUp()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    this.mLastMotionY = this.mInitialMotionY = ev.getY();
                    this.mActivePointerId = ev.getPointerId(0);
                    this.mIsBeingDragged = false;
                    this.mCurrPercentage = 0.0F;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.mIsBeingDragged = false;
                    this.mCurrPercentage = 0.0F;
                    this.mActivePointerId = -1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (this.mActivePointerId == -1) {
                        Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                        return false;
                    }

                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex < 0) {
                        Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    float y = ev.getY(pointerIndex);
                    float yDiff = y - this.mInitialMotionY;
                    if (yDiff > (float) this.mTouchSlop) {
                        this.mLastMotionY = y;
                        this.mIsBeingDragged = true;
                    }
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_POINTER_DOWN:
                default:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    this.onSecondaryPointerUp(ev);
            }

            return this.mIsBeingDragged;
        } else {
            return false;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (!this.mRefreshing && this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }

        if (this.isEnabled() && !this.mReturningToStart && !this.canChildScrollUp()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    this.mLastMotionY = this.mInitialMotionY = ev.getY();
                    this.mActivePointerId = ev.getPointerId(0);
                    this.mIsBeingDragged = false;
                    this.mCurrPercentage = 0.0F;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = -1;
                    if(!this.mRefreshing && this.mCurrPercentage != 0.0F){
                        updatePositionTimeout(50L);
                    }
                    return false;
                case MotionEvent.ACTION_MOVE:
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex < 0) {
                        Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    float y = ev.getY(pointerIndex);
                    float yDiff = y - this.mInitialMotionY;
                    if (!this.mIsBeingDragged && yDiff > (float) this.mTouchSlop) {
                        this.mIsBeingDragged = true;
                    }

                    if (this.mIsBeingDragged) {
                        if (yDiff > this.mDistanceToTriggerSync) {
                            this.startRefresh();
                        } else {
                            this.setTriggerPercentage(this.mAccelerateInterpolator.getInterpolation(yDiff / this.mDistanceToTriggerSync));
                            if (this.mLayoutDraggingTogether) {
                                this.updateContentOffsetTop((int) yDiff);
                            }
                            if (this.mLastMotionY > y && this.mTarget.getTop() == this.getPaddingTop()) {
                                this.removeCallbacks(this.mCancel);
                            } else {
                                this.updatePositionTimeout(300L);
                            }
                        }

                        this.mLastMotionY = y;
                    }
                case MotionEvent.ACTION_OUTSIDE:
                default:
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    int index = ev.getActionIndex();
                    this.mLastMotionY = ev.getY(index);
                    this.mActivePointerId = ev.getPointerId(index);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    this.onSecondaryPointerUp(ev);
            }

            return true;
        } else {
            return false;
        }
    }

    private void startRefresh() {
        this.removeCallbacks(this.mCancel);
        this.mReturnToStartPosition.run();
        this.startRefreshing();
    }

    private void updateContentOffsetTop(int targetTop) {
        int currentTop = this.mTarget.getTop();
        if ((float) targetTop > this.mDistanceToTriggerSync) {
            targetTop = (int) this.mDistanceToTriggerSync;
        } else if (targetTop < 0) {
            targetTop = 0;
        }

        this.setTargetOffsetTopAndBottom(targetTop - currentTop);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        this.mTarget.offsetTopAndBottom(offset);
        this.mCurrentTargetOffsetTop = this.mTarget.getTop();
    }

    private void updatePositionTimeout(long delay) {
        this.removeCallbacks(this.mCancel);
        this.postDelayed(this.mCancel, delay);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionY = ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }

    }

    private class BaseAnimationListener implements AnimationListener {
        private BaseAnimationListener() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
