package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

/**
 * 输入框
 */

public class InputView {
    private TextView tvTitle;
    private EditText etInput;
    private TextView tvOk;

    private MoDialogHUD moDialogHUD;
    private MoDialogView moDialogView;
    private OnInputOk onInputOk;
    private Context context;

    public static InputView getInstance(MoDialogView moDialogView) {
        return new InputView(moDialogView);
    }

    private InputView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
        tvOk.setOnClickListener(view -> {
            onInputOk.setInputText(etInput.getText().toString());
            moDialogHUD.dismiss();
        });
    }

    void showInputView(final OnInputOk onInputOk, MoDialogHUD moDialogHUD, String title, String defaultValue) {
        this.moDialogHUD = moDialogHUD;
        this.onInputOk = onInputOk;
        tvTitle.setText(title);
        if (defaultValue != null) {
            etInput.setText(defaultValue);
        }
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_input, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = moDialogView.findViewById(R.id.tv_title);
        etInput = moDialogView.findViewById(R.id.et_input);
        tvOk = moDialogView.findViewById(R.id.tv_ok);

        ImmersionBar.resetBoxPosition((Activity) context, moDialogView, R.id.cv_root);
    }

    /**
     * 输入book地址确定
     */
    public interface OnInputOk {
        void setInputText(String inputText);
    }
}
