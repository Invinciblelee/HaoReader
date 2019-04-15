package com.monke.basemvplib;

import android.os.Handler;
import android.os.Looper;

public enum Poster {
    INSTANCE;

    private Handler mHandler;

    public void post(Runnable runnable){
        getHandler().post(runnable);
    }

    public void postDelayed(Runnable runnable, long delay){
        getHandler().postDelayed(runnable, delay);
    }

    public void removeCallbacks(Runnable runnable){
        getHandler().removeCallbacks(runnable);
    }

    private Handler getHandler(){
        if(mHandler == null){
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}
