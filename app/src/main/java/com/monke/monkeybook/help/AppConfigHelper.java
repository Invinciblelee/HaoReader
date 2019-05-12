package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfigHelper {

    private volatile static AppConfigHelper mInstance;
    private SharedPreferences preferences;

    private AppConfigHelper(Context context) {
        preferences = context.getSharedPreferences("CONFIG", 0);
    }

    public static AppConfigHelper get() {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new AppConfigHelper(ContextHolder.getContext());
                }
            }
        }
        return mInstance;
    }

    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public String getString(String key, String def) {
        return preferences.getString(key, def);
    }

    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    public float getFloat(String key, float def) {
        return preferences.getFloat(key, def);
    }

    public long getLong(String key, long def) {
        return preferences.getLong(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

}
