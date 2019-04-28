package com.monke.monkeybook.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class CustomDrawerLayout extends DrawerLayout {

    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;

    private boolean isClosed = true;

    public CustomDrawerLayout(Context context) {
        this(context, null);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();

        addDrawerListener(new DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                isClosed = false;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                isClosed = true;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isClosed){
            return super.onInterceptTouchEvent(ev);
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int xDiff = (int) Math.abs(x - mLastMotionX);
                int yDiff = (int) Math.abs(y - mLastMotionY);
                final int x_yDiff = xDiff * xDiff + yDiff * yDiff;

                boolean xMoved = x_yDiff > mTouchSlop * mTouchSlop;

                if (xMoved) {
                    return xDiff > yDiff * 4;
                }
                break;
            default:

                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

}