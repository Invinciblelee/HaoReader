package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.monke.monkeybook.R;

public class ProgressDialog extends AppCompatDialog {

    private TextView mMessageView;
    private CharSequence mMessageText;

    public static ProgressDialog show(@NonNull Fragment fragment, CharSequence message) {
        return show(fragment, message, false);
    }

    public static ProgressDialog show(@NonNull Fragment fragment, CharSequence message, boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog();
        dialog.setCancelable(cancelable);
        Bundle bundle = new Bundle();
        bundle.putCharSequence("message", message);
        dialog.setArguments(bundle);
        dialog.show(fragment.getChildFragmentManager(), "waiting");
        return dialog;
    }

    public static ProgressDialog show(@NonNull AppCompatActivity activity, CharSequence message) {
        return show(activity, message, false);
    }

    public static ProgressDialog show(@NonNull AppCompatActivity activity, CharSequence message, boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog();
        dialog.setCancelable(cancelable);
        Bundle bundle = new Bundle();
        bundle.putCharSequence("message", message);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "waiting");
        return dialog;
    }

    public void setMessage(CharSequence message) {
        mMessageText = message;
        if (mMessageView != null) {
            mMessageView.setText(mMessageText);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMessageText = args.getCharSequence("message");
        }
    }

    @Nullable
    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_loading, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mMessageView = view.findViewById(R.id.msg_tv);
        if (mMessageText != null) {
            mMessageView.setText(mMessageText);
        }
    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }

    public void show(AppCompatActivity activity) {
        if (!isShowing() && activity != null) {
            super.show(activity.getSupportFragmentManager(), "waiting");
        }
    }

    public void show(Fragment fragment) {
        if (!isShowing() && fragment != null) {
            super.show(fragment.getChildFragmentManager(), "waiting");
        }
    }
}
