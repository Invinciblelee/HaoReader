package com.monke.monkeybook.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class ToastUtils {

    private ToastUtils() {

    }

    public static void toast(Context context, @NonNull CharSequence msg) {
        if(context == null) return;
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setView(createToastView(context, msg));
        toast.show();
    }

    public static void toast(Context context, @StringRes int resId) {
        if(context == null) return;
        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        toast.setView(createToastView(context, context.getText(resId)));
        toast.show();
    }

    public static void longToast(Context context, @NonNull CharSequence msg) {
        if(context == null) return;
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setView(createToastView(context, msg));
        toast.show();
    }

    public static void longToast(Context context, @StringRes int resId) {
        if(context == null) return;
        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        toast.setView(createToastView(context, context.getText(resId)));
        toast.show();
    }

    private static View createToastView(@NonNull Context context, @NonNull CharSequence message) {
        @SuppressLint("InflateParams") View toastView = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
        TextView messageView = toastView.findViewById(R.id.tv_message);
        messageView.setText(message);
        return toastView;
    }
}
