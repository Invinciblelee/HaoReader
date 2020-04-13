package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;

import com.monke.basemvplib.ContextHolder;

import java.util.Set;

public class AppConfigHelper {

    private volatile static AppConfigHelper mInstance;
    private SharedPreferences preferences;

    private AppConfigHelper() {
    }

    public static AppConfigHelper get() {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new AppConfigHelper();
                }
            }
        }
        return mInstance;
    }

    public SharedPreferences.Editor edit() {
        return getPreferences().edit();
    }

    public SharedPreferences getPreferences() {
        if(preferences == null){
            preferences = ContextHolder.getContext().getSharedPreferences("CONFIG", 0);
        }
        return preferences;
    }

    public boolean put(String key, Object val){
        if(val == null) return false;

        if (val instanceof String){
            return edit().putString(key, (String) val).commit();
        }else if(val instanceof Integer){
            return edit().putInt(key, (Integer) val).commit();
        }else if(val instanceof Long){
            return edit().putLong(key, (Long) val).commit();
        }else if(val instanceof Float){
            return edit().putFloat(key, (Float) val).commit();
        }else if(val instanceof Boolean){
            return edit().putBoolean(key, (Boolean) val).commit();
        }else if(val instanceof Set){
            return edit().putStringSet(key, (Set<String>) val).commit();
        }
        return false;
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
