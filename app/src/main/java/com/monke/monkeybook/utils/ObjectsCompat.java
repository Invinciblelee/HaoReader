package com.monke.monkeybook.utils;

import android.os.Build;

import java.util.Arrays;
import java.util.Objects;

import io.reactivex.annotations.Nullable;

public class ObjectsCompat {

    public static boolean isNull(@Nullable Object object) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Objects.isNull(object);
        } else {
            return object == null;
        }
    }


    public static boolean nonNull(@Nullable Object object) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Objects.nonNull(object);
        } else {
            return object != null;
        }
    }

    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.equals(a, b);
        } else {
            return (a == b) || (a != null && a.equals(b));
        }
    }

    public static int hashCode(@Nullable Object o) {
        return o != null ? o.hashCode() : 0;
    }


    public static int hash(@Nullable Object... values) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.hash(values);
        } else {
            return Arrays.hashCode(values);
        }
    }
}
