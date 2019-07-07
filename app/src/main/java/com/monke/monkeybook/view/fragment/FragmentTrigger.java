package com.monke.monkeybook.view.fragment;

import android.view.MotionEvent;

public interface FragmentTrigger {

    void onRefresh();

    void onRestore();

    boolean onBackPressed();

    boolean dispatchTouchEvent(MotionEvent ev);

    void onReselected();
}
