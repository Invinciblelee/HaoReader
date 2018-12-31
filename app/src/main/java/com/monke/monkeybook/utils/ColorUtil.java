package com.monke.monkeybook.utils;

import android.graphics.Bitmap;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ColorUtil {

    public static final int IS_LIGHT = 0;
    public static final int IS_DARK = 1;
    public static final int LIGHTNESS_UNKNOWN = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IS_LIGHT, IS_DARK, LIGHTNESS_UNKNOWN})
    @interface Lightness {
    }

    public static String intToString(int intColor) {
        return String.format("#%06X", 0xFFFFFF & intColor);
    }

    public static boolean isDrak(Bitmap bitmap, int backupPixelX, int backupPixelY) {
        Palette palette = Palette.from(bitmap).maximumColorCount(3).generate();
        if (palette.getSwatches().size() > 0) {
            return isDark(palette) == IS_DARK;
        } else {
            return isDark(bitmap.getPixel(backupPixelX, backupPixelY));
        }
    }

    @Lightness
    public static int isDark(Palette palette) {
        Palette.Swatch mostPopulous = getMostPopulousSwatch(palette);
        if (mostPopulous == null) {
            return LIGHTNESS_UNKNOWN;
        }
        if (isDark(mostPopulous.getRgb())) {
            return IS_DARK;
        } else {
            return IS_LIGHT;
        }
    }

    public static boolean isDark(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

    public static Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }
}
