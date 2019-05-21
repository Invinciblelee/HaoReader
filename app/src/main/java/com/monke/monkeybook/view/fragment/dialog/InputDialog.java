package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;

/**
 * 输入框
 */

public class InputDialog extends AppCompatDialog {
    private TextView tvTitle;
    private AutoCompleteTextView etInput;
    private TextView tvOk;

    private OnInputOk onInputOk;

    public static void show(FragmentManager fragmentManager, String title, String defValue, String[] adapterValues, OnInputOk onInputOk) {
        InputDialog dialog = new InputDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("defValues", defValue);
        args.putStringArray("adapterValues", adapterValues);
        dialog.setArguments(args);
        dialog.onInputOk = onInputOk;
        dialog.show(fragmentManager, "input");
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_input, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;
        final String title = args.getString("title");
        final String defValue = args.getString("defValue");
        final String[] adapterValues = args.getStringArray("adapterValues");

        tvTitle = findViewById(R.id.tv_title);
        etInput = findViewById(R.id.et_input);
        tvOk = findViewById(R.id.tv_ok);

        tvTitle.setText(title);
        if (defValue != null) {
            etInput.setText(defValue);
            etInput.setSelectAllOnFocus(true);
        }

        if (adapterValues != null) {
            ArrayAdapter mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, adapterValues);
            etInput.setAdapter(mAdapter);
        }

        tvOk.setOnClickListener(v -> {
            if(onInputOk != null) {
                onInputOk.setInputText(etInput.getText().toString());
            }
            dismissAllowingStateLoss();
        });
    }

    /**
     * 输入book地址确定
     */
    public interface OnInputOk {
        void setInputText(String inputText);
    }
}
