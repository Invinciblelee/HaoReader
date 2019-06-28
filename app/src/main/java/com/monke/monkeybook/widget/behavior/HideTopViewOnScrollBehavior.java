package com.monke.monkeybook.widget.behavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.animation.AnimationUtils;

public class HideTopViewOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  protected static final int ENTER_ANIMATION_DURATION = 225;
  protected static final int EXIT_ANIMATION_DURATION = 175;

  private static final int STATE_SCROLLED_DOWN = 1;
  private static final int STATE_SCROLLED_UP = 2;

  private int height = 0;
  private int currentState = STATE_SCROLLED_DOWN;
  private int additionalHiddenOffsetY = 0;
  private ViewPropertyAnimator currentAnimator;

  public HideTopViewOnScrollBehavior() {}

  public HideTopViewOnScrollBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
    ViewGroup.MarginLayoutParams paramsCompat =
        (ViewGroup.MarginLayoutParams) child.getLayoutParams();
    height = child.getMeasuredHeight() + paramsCompat.topMargin;
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  /** Sets an additional offset for the y position used to hide the view. */
  public void setAdditionalHiddenOffsetY(V child, int offset) {
    additionalHiddenOffsetY = offset;

    if (currentState == STATE_SCROLLED_DOWN) {
      child.setTranslationY(height + additionalHiddenOffsetY);
    }
  }

  @Override
  public boolean onStartNestedScroll(
      CoordinatorLayout coordinatorLayout,
      V child,
      View directTargetChild,
      View target,
      int nestedScrollAxes) {
    return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
  }

  @Override
  public void onNestedScroll(
      CoordinatorLayout coordinatorLayout,
      V child,
      View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed) {
    if (dyConsumed > 0) {
      slideUp(child);
    } else if (dyConsumed < 0) {
      slideDown(child);
    }
  }

  /**
   * Perform an animation that will slide the child from it's current position to be totally on the
   * screen.
   */
  public void slideUp(V child) {
    if (currentState == STATE_SCROLLED_UP) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_UP;
    animateChildTo(
        child, -height + additionalHiddenOffsetY, ENTER_ANIMATION_DURATION, AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
  }

  /**
   * Perform an animation that will slide the child from it's current position to be totally off the
   * screen.
   */
  public void slideDown(V child) {
    if (currentState == STATE_SCROLLED_DOWN) {
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
      child.clearAnimation();
    }
    currentState = STATE_SCROLLED_DOWN;
    animateChildTo(
        child,
        0,
        EXIT_ANIMATION_DURATION,
        AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
  }

  private void animateChildTo(V child, int targetY, long duration, TimeInterpolator interpolator) {
    currentAnimator =
        child
            .animate()
            .translationY(targetY)
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(
                new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                    currentAnimator = null;
                  }
                });
  }
}
