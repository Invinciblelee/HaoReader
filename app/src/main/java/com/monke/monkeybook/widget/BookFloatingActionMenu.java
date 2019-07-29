package com.monke.monkeybook.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.monke.monkeybook.R;

public class BookFloatingActionMenu extends LinearLayout {

    private FloatingActionButton fabMain;

    private boolean isExpanded;
    private int mLastIndex;
    private OnActionMenuClickListener mMenuClickListener;

    private final Runnable mExpandRunnable = this::expandInternal;

    private final Runnable mCollapseRunnable = this::collapseInternal;

    public BookFloatingActionMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnActionMenuClickListener(OnActionMenuClickListener menuClickListener) {
        this.mMenuClickListener = menuClickListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        fabMain = findViewById(R.id.fab_main);

        fabMain.setOnClickListener(v -> {
            if (isExpanded) {
                collapse();
            } else {
                expand();
            }
        });

        fabMain.setOnLongClickListener(v -> {
            if (mMenuClickListener != null) {
                mMenuClickListener.onMainLongClick(v);
            }
            startRefreshAnim();
            return true;
        });

        initFloatingActionMenu();
    }

    private void startRefreshAnim() {
        fabMain.setImageResource(R.drawable.ic_refresh_white_24dp);
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        animationSet.addAnimation(rotateAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fabMain.setImageResource(R.drawable.ic_library_books_black_24dp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fabMain.startAnimation(animationSet);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initFloatingActionMenu() {
        for (int i = 0; i <= getChildCount() - 2; i++) {
            ViewGroup childGroup = (ViewGroup) getChildAt(i);
            final int index = Integer.parseInt(String.valueOf(childGroup.getTag()));
            childGroup.setVisibility(INVISIBLE);
            View labelView = childGroup.getChildAt(0);
            labelView.setVisibility(INVISIBLE);
            FloatingActionButton btnView = (FloatingActionButton) childGroup.getChildAt(1);
            btnView.setTag(btnView.getDrawable());
            if (mLastIndex == index) {
                btnView.setImageResource(R.drawable.ic_check_black_24dp);
            }
            labelView.setOnClickListener(v -> btnView.callOnClick());
            btnView.setOnClickListener(v -> {
                setSelection(index);
                collapse();
                if (mMenuClickListener != null) {
                    mMenuClickListener.onMenuClick(index, v);
                }
            });
            btnView.post(btnView::hide);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isExpanded) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setSelection(int index) {
        if (mLastIndex != index) {
            ViewGroup childLast = findViewWithTag(String.valueOf(mLastIndex));
            if (childLast != null) {
                FloatingActionButton lastBtn = (FloatingActionButton) childLast.getChildAt(1);
                lastBtn.setImageDrawable((Drawable) lastBtn.getTag());
            }

            ViewGroup child = findViewWithTag(String.valueOf(index));
            if (child != null) {
                FloatingActionButton lastBtn = (FloatingActionButton) child.getChildAt(1);
                lastBtn.setImageResource(R.drawable.ic_check_black_24dp);
            }

            mLastIndex = index;
        }
    }

    public void expand() {
        post(mExpandRunnable);
    }

    public void collapse() {
        post(mCollapseRunnable);
    }

    private void expandInternal() {
        if (!isExpanded) {
            isExpanded = true;

            int index = 0;
            for (int i = getChildCount() - 2; i >= 0; i--) {
                ViewGroup childGroup = (ViewGroup) getChildAt(i);
                if (childGroup.getVisibility() != VISIBLE) {
                    childGroup.setVisibility(VISIBLE);
                }
                View labelView = childGroup.getChildAt(0);
                FloatingActionButton btnView = (FloatingActionButton) childGroup.getChildAt(1);
                postDelayed(() -> {
                    btnView.show();
                    animateShowLabelView(labelView);
                }, 50 * index);
                index++;
            }
        }
    }

    private void collapseInternal() {
        if (isExpanded) {
            isExpanded = false;

            int index = 0;
            for (int i = 0; i <= getChildCount() - 2; i++) {
                ViewGroup childGroup = (ViewGroup) getChildAt(i);
                View labelView = childGroup.getChildAt(0);
                FloatingActionButton btnView = (FloatingActionButton) childGroup.getChildAt(1);
                postDelayed(() -> {
                    btnView.hide();
                    animateHideLabelView(labelView);
                }, 50 * index);
                index++;
            }

        }
    }

    private void animateShowLabelView(View labelView) {
        labelView.animate().cancel();
        if (labelView.getVisibility() != VISIBLE) {
            labelView.setTranslationX(1.0f * labelView.getWidth() / 2);
            labelView.setAlpha(0f);
        }
        labelView.animate().translationX(0f).alpha(1f)
                .setDuration(200L)
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        labelView.setVisibility(VISIBLE);
                    }
                })
                .start();
    }

    private void animateHideLabelView(View labelView) {
        labelView.animate().cancel();
        labelView.animate().translationX(1.0f * labelView.getWidth() / 2).alpha(0f)
                .setDuration(200L)
                .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        labelView.setVisibility(GONE);
                    }
                }).start();
    }

    public interface OnActionMenuClickListener {
        void onMainLongClick(View fabMain);

        void onMenuClick(int index, View menuView);
    }
}
