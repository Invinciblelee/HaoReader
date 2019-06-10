package com.monke.monkeybook.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;


public class AppCompatDialog extends AppCompatDialogFragment {

    public AppCompatDialog() {
        setStyle(STYLE_NO_TITLE, R.style.Style_Custom_Dialog);
    }

    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = onCreateDialogView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }
        ViewGroup containerView = (ViewGroup) inflater.inflate(R.layout.dialog_design_container, container, false);
        onCreateDialogContentView(inflater, containerView, savedInstanceState);
        return containerView;
    }

    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    public final boolean isViewCreated() {
        return getView() != null;
    }

    @SuppressWarnings("unchecked")
    public final <T> T findViewById(@IdRes int id) {
        if (getView() == null) {
            throw new NullPointerException();
        }
        return (T) getView().findViewById(id);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        manager.executePendingTransactions();
        if (!isAdded()) {
            super.show(manager, tag);
        }
    }

    @Override
    public void showNow(@NonNull FragmentManager manager, @Nullable String tag) {
        manager.executePendingTransactions();
        if (!isAdded()) {
            super.showNow(manager, tag);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                onDialogAttachWindow(window);
            }
        }
    }

    public final boolean isShowing() {
        if (isAdded()) {
            return true;
        }
        final Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    protected void onDialogAttachWindow(@NonNull Window window) {
        window.setGravity(Gravity.CENTER);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
