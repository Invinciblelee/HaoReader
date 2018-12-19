package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;

public class CookieHelper {
    private volatile static CookieHelper mInstance;
    private SharedPreferences preferences;

    private CookieHelper(Context context) {
        preferences = context.getSharedPreferences("COOKIES", 0);
    }

    public static CookieHelper get(Context context) {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new CookieHelper(context);
                }
            }
        }
        return mInstance;
    }

    public void setCookie(String url, String cookie) {
        preferences.edit().putString(url, cookie).apply();
    }

    public String getCookie(String url) {
        return preferences.getString(url, "");
    }

    public void removeCookie(String url) {
        preferences.edit().remove(url).apply();
    }

    public void clearCookies() {
        preferences.edit().clear().apply();
    }
}
