package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;

public class CookiePersistentHelper {
    private volatile static CookiePersistentHelper mInstance;
    private SharedPreferences preferences;

    private CookiePersistentHelper(Context context) {
        preferences = context.getSharedPreferences("COOKIES", 0);
    }

    public static CookiePersistentHelper get(Context context) {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new CookiePersistentHelper(context);
                }
            }
        }
        return mInstance;
    }

    public void setCookie(String url, String cookie){
        preferences.edit().putString(url, cookie).apply();
    }

    public String getCookie(String url){
        return preferences.getString(url, "");
    }
}
