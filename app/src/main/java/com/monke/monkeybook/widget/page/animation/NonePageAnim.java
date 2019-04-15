package com.monke.monkeybook.widget.page.animation;

import android.graphics.Canvas;

import com.monke.monkeybook.widget.page.PageMode;
import com.monke.monkeybook.widget.page.PageView;

/**
 * Created by newbiechen on 17-7-24.
 */

public class NonePageAnim extends HorizonPageAnim {

    public NonePageAnim(int w, int h, PageView view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @Override
    public void drawMove(Canvas canvas) {
        canvas.drawBitmap(mCurBitmap, 0, 0, null);
    }

    @Override
    public void startAnim() {
        super.startAnim();
        isRunning = false;
    }

    @Override
    public PageMode getPageMode() {
        return PageMode.NONE;
    }
}
