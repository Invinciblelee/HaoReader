package com.monke.monkeybook.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ContextUtils {

    private ContextUtils(){

    }

    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    public static Activity scanForActivity(View view) {
        if (view == null) return null;

        Context context = view.getContext();
        return scanForActivity(context);
    }

    public static AppCompatActivity getCompatActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof androidx.appcompat.view.ContextThemeWrapper) {
            return getCompatActivity(((androidx.appcompat.view.ContextThemeWrapper) context).getBaseContext());
        }else  if(context instanceof android.view.ContextThemeWrapper){
            return getCompatActivity(((android.view.ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static AppCompatActivity getCompatActivity(View view) {
        if (view == null) return null;

        Context context = view.getContext();
        return getCompatActivity(context);
    }


}
