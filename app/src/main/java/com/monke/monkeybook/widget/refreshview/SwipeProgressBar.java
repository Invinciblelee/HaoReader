//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.monke.monkeybook.widget.refreshview;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.monke.monkeybook.R;

final class SwipeProgressBar {
    private static final int COLOR1 = Color.RED;
    private static final int COLOR2 = Color.YELLOW;
    private static final int COLOR3 = Color.BLUE;
    private static final int COLOR4 = Color.GREEN;
    private static final int ANIMATION_DURATION_MS = 2000;
    private static final int FINISH_ANIMATION_DURATION_MS = 1000;
    private static final Interpolator INTERPOLATOR = BakedBezierInterpolator.getInstance();
    private final Paint mPaint = new Paint();
    private final RectF mClipRect = new RectF();
    private float mTriggerPercentage;
    private long mStartTime;
    private long mFinishTime;
    private boolean mRunning;
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    private View mParent;
    private Rect mBounds = new Rect();

    public SwipeProgressBar(View parent) {
        this.mParent = parent;
        this.mColor1 = Color.parseColor("#05c1e0");
        this.mColor2 = Color.parseColor("#81b002");
        this.mColor3 = Color.parseColor("#e0a020");
        this.mColor4 = Color.parseColor("#dc3232");
    }

    void setColorScheme(int color1, int color2, int color3, int color4) {
        this.mColor1 = color1;
        this.mColor2 = color2;
        this.mColor3 = color3;
        this.mColor4 = color4;
    }

    void setTriggerPercentage(float triggerPercentage) {
        this.mTriggerPercentage = triggerPercentage;
        this.mStartTime = 0L;
        ViewCompat.postInvalidateOnAnimation(this.mParent);
    }

    void start() {
        if (!this.mRunning) {
            this.mTriggerPercentage = 0.0F;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = true;
            this.mParent.postInvalidate();
        }

    }

    void stop() {
        if (this.mRunning) {
            this.mTriggerPercentage = 0.0F;
            this.mFinishTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = false;
            this.mParent.postInvalidate();
        }

    }

    boolean isRunning() {
        return this.mRunning || this.mFinishTime > 0L;
    }

    void draw(Canvas canvas) {
        int width = this.mBounds.width();
        int height = this.mBounds.height();
        int cx = width / 2;
        int cy = height / 2;
        boolean drawTriggerWhileFinishing = false;
        int restoreCount = canvas.save();
        canvas.clipRect(this.mBounds);
        if (!this.mRunning && this.mFinishTime <= 0L) {
            if (this.mTriggerPercentage > 0.0F && (double)this.mTriggerPercentage <= 1.0D) {
                this.drawTrigger(canvas, cx, cy);
            }
        } else {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long elapsed = (now - this.mStartTime) % 2000L;
            long iterations = (now - this.mStartTime) / 2000L;
            float rawProgress = (float)elapsed / 20.0F;
            if (!this.mRunning) {
                if (now - this.mFinishTime >= 1000L) {
                    this.mFinishTime = 0L;
                    return;
                }

                long finishElapsed = (now - this.mFinishTime) % 1000L;
                float finishProgress = (float)finishElapsed / 10.0F;
                float pct = finishProgress / 100.0F;
                float clearRadius = (float)(width / 2) * INTERPOLATOR.getInterpolation(pct);
                this.mClipRect.set((float)cx - clearRadius, 0.0F, (float)cx + clearRadius, (float)height);
                canvas.saveLayerAlpha(this.mClipRect, 0, 0);
                drawTriggerWhileFinishing = true;
            }

            if (iterations == 0L) {
                canvas.drawColor(this.mColor1);
            } else if (rawProgress >= 0.0F && rawProgress < 25.0F) {
                canvas.drawColor(this.mColor4);
            } else if (rawProgress >= 25.0F && rawProgress < 50.0F) {
                canvas.drawColor(this.mColor1);
            } else if (rawProgress >= 50.0F && rawProgress < 75.0F) {
                canvas.drawColor(this.mColor2);
            } else {
                canvas.drawColor(this.mColor3);
            }

            float pct;
            if (rawProgress >= 0.0F && rawProgress <= 25.0F) {
                pct = (rawProgress + 25.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor1, pct);
            }

            if (rawProgress >= 0.0F && rawProgress <= 50.0F) {
                pct = rawProgress * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor2, pct);
            }

            if (rawProgress >= 25.0F && rawProgress <= 75.0F) {
                pct = (rawProgress - 25.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor3, pct);
            }

            if (rawProgress >= 50.0F && rawProgress <= 100.0F) {
                pct = (rawProgress - 50.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor4, pct);
            }

            if (rawProgress >= 75.0F && rawProgress <= 100.0F) {
                pct = (rawProgress - 75.0F) * 2.0F / 100.0F;
                this.drawCircle(canvas, (float)cx, (float)cy, this.mColor1, pct);
            }

            if (this.mTriggerPercentage > 0.0F && drawTriggerWhileFinishing) {
                canvas.restoreToCount(restoreCount);
                restoreCount = canvas.save();
                canvas.clipRect(this.mBounds);
                this.drawTrigger(canvas, cx, cy);
            }

            ViewCompat.postInvalidateOnAnimation(this.mParent);
        }

        canvas.restoreToCount(restoreCount);
    }

    private void drawTrigger(Canvas canvas, int cx, int cy) {
        this.mPaint.setColor(this.mColor1);
        canvas.drawCircle((float)cx, (float)cy, (float)cx * this.mTriggerPercentage, this.mPaint);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, int color, float pct) {
        this.mPaint.setColor(color);
        canvas.save();
        canvas.translate(cx, cy);
        float radiusScale = INTERPOLATOR.getInterpolation(pct);
        canvas.scale(radiusScale, radiusScale);
        canvas.drawCircle(0.0F, 0.0F, cx, this.mPaint);
        canvas.restore();
    }

    void setBounds(int left, int top, int right, int bottom) {
        this.mBounds.left = left;
        this.mBounds.top = top;
        this.mBounds.right = right;
        this.mBounds.bottom = bottom;
    }
}
