package com.monke.monkeybook.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.monke.monkeybook.R;

import androidx.annotation.Nullable;

public class BookFloatingActionMenu extends LinearLayout {

    private boolean isExpanded;
    private int mLastIndex;
    private final Handler mAnimationHandler = new Handler();
    private OnActionMenuClickListener mMenuClickListener;

    public BookFloatingActionMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnActionMenuClickListener(OnActionMenuClickListener menuClickListener) {
        this.mMenuClickListener = menuClickListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        FloatingActionButton fabMain = findViewById(R.id.fab_main);

        fabMain.setOnClickListener(v -> {
            if (isExpanded) {
                collapse();
            } else {
                expand();
            }
        });

        initFloatingActionMenu();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initFloatingActionMenu() {
        for (int i = 0; i <= getChildCount() - 2; i++) {
            final int index = i;
            ViewGroup childGroup = (ViewGroup) getChildAt(index);
            childGroup.setVisibility(INVISIBLE);
            View labelView = childGroup.getChildAt(0);
            labelView.setVisibility(INVISIBLE);
            FloatingActionButton btnView = (FloatingActionButton) childGroup.getChildAt(1);
            btnView.setTag(btnView.getDrawable());
            if (mLastIndex == index) {
                btnView.setImageResource(R.drawable.ic_check_black_24dp);
            }
            labelView.setOnClickListener(v -> btnView.callOnClick());
            labelView.setOnTouchListener((v, event) -> {
                btnView.onTouchEvent(event);
                return false;
            });
            btnView.setOnClickListener(v -> {
                setSelection(index);
                collapse();
                if (mMenuClickListener != null) {
                    mMenuClickListener.onMenuClick(index, v);
                }
            });
            btnView.setOnTouchListener((v, event) -> {
                labelView.onTouchEvent(event);
                return false;
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
        ViewGroup childLast = (ViewGroup) getChildAt(mLastIndex);
        if (childLast != null) {
            FloatingActionButton lastBtn = (FloatingActionButton) childLast.getChildAt(1);
            lastBtn.setImageDrawable((Drawable) lastBtn.getTag());
        }

        ViewGroup child = (ViewGroup) getChildAt(index);
        if (child != null) {
            FloatingActionButton lastBtn = (FloatingActionButton) child.getChildAt(1);
            lastBtn.setImageResource(R.drawable.ic_check_black_24dp);
        }

        mLastIndex = index;
    }

    public void expand() {
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
                mAnimationHandler.postDelayed(() -> {
                    btnView.show();
                    animateShowLabelView(labelView);
                }, 50 * index);
                index++;
            }
        }
    }

    public void collapse() {
        if (isExpanded) {
            isExpanded = false;

            int index = 0;
            for (int i = 0; i <= getChildCount() - 2; i++) {
                ViewGroup childGroup = (ViewGroup) getChildAt(i);
                View labelView = childGroup.getChildAt(0);
                FloatingActionButton btnView = (FloatingActionButton) childGroup.getChildAt(1);
                mAnimationHandler.postDelayed(() -> {
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
        void onMenuClick(int index, View menuView);
    }
}
