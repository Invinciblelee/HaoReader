package com.monke.monkeybook.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.R;

public class ToastUtils {

    private static int Y_OFFSET = DensityUtil.dp2px(ContextHolder.getContext(), 78);

    private ToastUtils() {

    }

    public static void toast(Context context, @NonNull CharSequence msg) {
        if (context == null) return;
        createToast(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, @StringRes int resId) {
        if (context == null) return;
        createToast(context, context.getText(resId), Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context, @NonNull CharSequence msg) {
        if (context == null) return;
        createToast(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void longToast(Context context, @StringRes int resId) {
        if (context == null) return;
        createToast(context, context.getText(resId), Toast.LENGTH_LONG).show();
    }

    private static Toast createToast(Context context, CharSequence msg, int length) {
        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, msg, length);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, Y_OFFSET);
        toast.setView(createToastView(context, msg));
        return toast;
    }

    private static View createToastView(@NonNull Context context, @NonNull CharSequence message) {
        @SuppressLint("InflateParams") View toastView = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
        TextView messageView = toastView.findViewById(R.id.tv_message);
        messageView.setText(message);
        return toastView;
    }
}
