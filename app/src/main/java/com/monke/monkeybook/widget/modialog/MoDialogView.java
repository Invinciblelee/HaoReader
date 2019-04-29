package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.MarkdownUtils;
import com.monke.monkeybook.utils.ReadAssets;


/**
 * 对话框
 */
public class MoDialogView extends LinearLayout {

    private MoDialogHUD moDialogHUD;
    private Context context;

    private OnDismissListener dismissListener;

    public MoDialogView(Context context) {
        this(context, null);
    }

    public MoDialogView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(VERTICAL);
    }

    public MoDialogHUD getMoDialogHUD() {
        return moDialogHUD;
    }

    public void onAttach(MoDialogHUD moDialogHUD) {
        this.moDialogHUD = moDialogHUD;
    }

    //转圈的载入
    public void showLoading(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_loading, this, true);
        TextView msgTv = findViewById(R.id.msg_tv);
        if (text != null && text.length() > 0) {
            msgTv.setText(text);
        }
    }

    //单个按钮的信息提示框
    public void showInfo(String msg, final OnClickListener listener) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_infor, this, true);
        View llContent = findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        TextView msgTv = findViewById(R.id.msg_tv);
        msgTv.setText(msg);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setOnClickListener(listener);
    }

    //单个按钮的信息提示框
    public void showInfo(String msg, String btnText, final OnClickListener listener) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_infor, this, true);
        View llContent = findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        TextView msgTv = findViewById(R.id.msg_tv);
        msgTv.setText(msg);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setText(btnText);
        tvClose.setOnClickListener(listener);
    }

    //////////////////////两个不同等级的按钮//////////////////////
    public void showTwoButton(String msg, String b_f, OnClickListener c_f, String b_s, OnClickListener c_s) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_two, this, true);
        TextView tvMsg = findViewById(R.id.tv_msg);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnDone = findViewById(R.id.btn_done);
        tvMsg.setText(msg);
        btnCancel.setText(b_f);
        btnCancel.setOnClickListener(c_f);
        btnDone.setText(b_s);
        btnDone.setOnClickListener(c_s);
    }

    /**
     * 显示一段文本
     */
    public void showText(String text) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_text_large, this, true);
        TextView textView = findViewById(R.id.tv_can_copy);
        textView.setText(text);
    }

    /**
     * 显示asset Markdown
     */
    public void showAssetMarkdown(String assetFileName) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.moprogress_dialog_markdown, this, true);
        TextView tvMarkdown = findViewById(R.id.tv_markdown);

        MarkdownUtils.setText(tvMarkdown, ReadAssets.getText(context, assetFileName));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    public void setOnDismissListener(OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}