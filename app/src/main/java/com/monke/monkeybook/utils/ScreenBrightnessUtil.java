package com.monke.monkeybook.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.WindowManager;

public class ScreenBrightnessUtil {

    private ScreenBrightnessUtil() {
    }

    public static void setScreenBrightness(Activity activity, int value) {
        WindowManager.LayoutParams params = (activity).getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        (activity).getWindow().setAttributes(params);
    }

    public static void setScreenBrightness(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(params);
    }

    public static int getScreenBrightness(Activity activity) {
        int value = 1;
        ContentResolver cr = activity.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

}
