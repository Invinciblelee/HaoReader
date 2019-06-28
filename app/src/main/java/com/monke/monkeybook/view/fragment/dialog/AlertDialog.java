package com.monke.monkeybook.view.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;


public class AlertDialog extends AppCompatDialog {

    public static final int BUTTON_POSITIVE = -1;
    public static final int BUTTON_NEGATIVE = -2;

    private AlertController mAlert;
    private AlertController.AlertParams mAlertParams;

    void setAlertParams(AlertController.AlertParams params) {
        mAlertParams = params;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mAlert = new AlertController(context, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAlert.restoreState(savedInstanceState);
        }
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mAlert.onCreateView(inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAlert.applyAlertParams(mAlertParams);
        mAlert.onViewCreated(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlert.onDestroy();
    }


    public void show() {
        if (mAlert != null) {
            mAlert.show();
        }
    }

    public interface OnViewCreatedCallback {
        void onViewCreated(@NonNull AlertDialog dialog, @NonNull View dialogView);
    }

    public interface OnClickListener {
        void onClick(@NonNull AlertDialog dialog, int which);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public static class Builder {
        private final AlertController.AlertParams P;

        public Builder(FragmentManager fragmentManager) {
            this(fragmentManager, R.style.Style_Custom_Dialog);
        }

        public Builder(FragmentManager fragmentManager, int theme) {
            P = new AlertController.AlertParams();
            P.mFragmentManager = fragmentManager;
            P.mTheme = theme;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            P.mTitleText = title;
            P.mTitleTextResId = 0;
            return this;
        }

        public Builder setTitle(@StringRes int titleResId) {
            P.mTitleText = null;
            P.mTitleTextResId = titleResId;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            P.mMessageText = message;
            P.mMessageTextResId = 0;
            return this;
        }

        public Builder setMessage(@StringRes int messageResId) {
            P.mMessageText = null;
            P.mMessageTextResId = messageResId;
            return this;
        }

        public Builder setMessageTextAlignment(int textAlignment) {
            P.mMessageTextAlignment = textAlignment;
            return this;
        }

        public Builder setView(@LayoutRes int layoutResId) {
            P.mViewLayoutResId = layoutResId;
            return this;
        }

        public Builder setView(@LayoutRes int layoutResId, FrameLayout.LayoutParams params) {
            P.mViewLayoutResId = layoutResId;
            P.mViewParams = params;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener clickListener) {
            P.mPositiveText = text;
            P.mPositiveTextResId = 0;
            P.mPositiveClickListener = clickListener;
            return this;
        }

        public Builder setPositiveButton(@StringRes int textResId, OnClickListener clickListener) {
            P.mPositiveText = null;
            P.mPositiveTextResId = textResId;
            P.mPositiveClickListener = clickListener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener clickListener) {
            P.mNegativeText = text;
            P.mNegativeTextResId = 0;
            P.mNegativeClickListener = clickListener;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textResId, OnClickListener clickListener) {
            P.mNegativeText = null;
            P.mNegativeTextResId = textResId;
            P.mNegativeClickListener = clickListener;
            return this;
        }

        public Builder setOnViewCreatedCallback(OnViewCreatedCallback callback) {
            P.mViewCreatedCallback = callback;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            P.mDismissListener = listener;
            return this;
        }

        public AlertDialog create() {
            AlertDialog dialog = new AlertDialog();
            dialog.setArguments(P.createArguments());
            dialog.setAlertParams(P);
            return dialog;
        }

        public AlertDialog show() {
            final AlertDialog dialog = create();
            dialog.show(P.mFragmentManager, "alert");
            return dialog;
        }
    }
}

