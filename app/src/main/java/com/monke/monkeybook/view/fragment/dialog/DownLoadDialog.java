package com.monke.monkeybook.view.fragment.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ToastUtils;

/**
 * Created by GKF on 2018/1/17.
 * 离线下载
 */

public class DownLoadDialog {

    public static void show(FragmentManager fragmentManager, int start, int end, OnClickDownload clickDownload) {
        new AlertDialog.Builder(fragmentManager)
                .setTitle(R.string.download_offline)
                .setView(R.layout.dialog_download_choice)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.download_start, (dialog, which) -> {
                    EditText edtStart = dialog.findViewById(R.id.edt_start);
                    EditText edtEnd = dialog.findViewById(R.id.edt_end);

                    if (edtStart.getText().length() > 0 && edtEnd.getText().length() > 0) {
                        if (Integer.parseInt(edtStart.getText().toString()) > Integer.parseInt(edtEnd.getText().toString())) {
                            ToastUtils.toast(dialog.getContext(), "输入错误");
                        } else {
                            if (clickDownload != null) {
                                clickDownload.download(Integer.parseInt(edtStart.getText().toString()) - 1, Integer.parseInt(edtEnd.getText().toString()) - 1);
                            }
                        }
                    } else {
                        ToastUtils.toast(dialog.getContext(), "请输入要离线的章节数");
                    }
                })
                .setOnViewCreatedCallback((dialog, dialogView) -> initView(dialogView, start, end))
                .show();

    }

    public static void initView(@NonNull View view, int start, int end) {
        final Context context = view.getContext();

        EditText edtStart = view.findViewById(R.id.edt_start);
        EditText edtEnd = view.findViewById(R.id.edt_end);

        edtStart.setText(String.valueOf(start + 1));
        edtEnd.setText(String.valueOf(end));

        view.post(() -> {
            edtEnd.requestFocus();
            edtEnd.setSelection(edtEnd.length());
        });

        edtStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtStart.getText().length() > 0) {
                    try {
                        int temp = Integer.parseInt(edtStart.getText().toString().trim());
                        if (temp > end) {
                            edtStart.setText(String.valueOf(end));
                            edtStart.setSelection(edtStart.getText().length());
                            ToastUtils.toast(context, "不能超过总章节数");
                        } else if (temp <= 0) {
                            edtStart.setText(String.valueOf(1));
                            edtStart.setSelection(edtStart.getText().length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        edtEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtEnd.getText().length() > 0) {
                    try {
                        int temp = Integer.parseInt(edtEnd.getText().toString().trim());
                        if (temp > end) {
                            edtEnd.setText(String.valueOf(end));
                            edtEnd.setSelection(edtEnd.getText().length());
                            ToastUtils.toast(context, "不能超过总章节数");
                        } else if (temp <= 0) {
                            edtEnd.setText(String.valueOf(1));
                            edtEnd.setSelection(edtEnd.getText().length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 离线下载确定
     */
    public interface OnClickDownload {
        void download(int start, int end);
    }
}
