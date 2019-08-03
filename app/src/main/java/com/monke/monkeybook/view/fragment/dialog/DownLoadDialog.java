package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ToastUtils;

/**
 * Created by GKF on 2018/1/17.
 * 离线下载
 */

public class DownLoadDialog extends AppCompatDialog {

    private EditText edtStart;
    private EditText edtEnd;

    private OnClickDownload clickDownload;

    public static void show(FragmentManager fragmentManager, int start, int total, OnClickDownload clickDownload) {
        DownLoadDialog dialog = new DownLoadDialog();
        Bundle args = new Bundle();
        args.putInt("start", start);
        args.putInt("total", total);
        dialog.setArguments(args);
        dialog.clickDownload = clickDownload;
        dialog.show(fragmentManager, "download");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_download_choice, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;
        final int start = args.getInt("start");
        final int total = args.getInt("total");

        edtStart = findViewById(R.id.edt_start);
        edtEnd = findViewById(R.id.edt_end);
        TextView tvCancel = findViewById(R.id.btn_cancel);
        TextView tvDownload = findViewById(R.id.tv_download);

        edtStart.setText(String.valueOf(start + 1));
        edtEnd.setText(String.valueOf(total));

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
                        if (temp > total) {
                            edtStart.setText(String.valueOf(total));
                            edtStart.setSelection(edtStart.getText().length());
                            ToastUtils.toast(getContext(), "不能超过总章节数");
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
                        if (temp > total) {
                            edtEnd.setText(String.valueOf(total));
                            edtEnd.setSelection(edtEnd.getText().length());
                            ToastUtils.toast(getContext(), "不能超过总章节数");
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
        tvDownload.setOnClickListener(v -> {
            if (edtStart.getText().length() > 0 && edtEnd.getText().length() > 0) {
                if (Integer.parseInt(edtStart.getText().toString()) > Integer.parseInt(edtEnd.getText().toString())) {
                    ToastUtils.toast(getContext(), "输入错误");
                } else {
                    if (clickDownload != null) {
                        clickDownload.download(Integer.parseInt(edtStart.getText().toString()) - 1, Integer.parseInt(edtEnd.getText().toString()) - 1);
                    }
                    dismissAllowingStateLoss();
                }
            } else {
                ToastUtils.toast(getContext(), "请输入要离线的章节数");
            }
        });

        tvCancel.setOnClickListener(v -> dismissAllowingStateLoss());
    }

    /**
     * 离线下载确定
     */
    public interface OnClickDownload {
        void download(int start, int end);
    }
}
