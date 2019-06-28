package com.monke.basemvplib;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;


public class ContextHolder {

    private static Context sAppContext;

    private ContextHolder() {
    }

    public static void initialize(Context context) {
        sAppContext = context;
    }

    @SuppressLint("PrivateApi")
    public static Context getContext() {
        if (sAppContext == null) {
            try {
                Application application = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null, (Object[]) null);
                if (application != null) {
                    sAppContext = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Application application = (Application) Class.forName("android.app.AppGlobals")
                        .getMethod("getInitialApplication").invoke(null, (Object[]) null);
                if (application != null) {
                    sAppContext = application;
                    return application;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            throw new IllegalStateException("ContextHolder is not initialed, it is recommend to init with application context.");
        }
        return sAppContext;
    }
}