//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.monke.monkeybook.R;

public class CheckAddShelfPop extends PopupWindow {
    private Context mContext;
    private View view;
    private boolean isAudioBook;

    public interface OnItemClickListener {
        void clickExit();

        void clickAddShelf();
    }

    private OnItemClickListener itemClick;
    private String bookName;

    public CheckAddShelfPop(Context context, @NonNull String bookName, @NonNull OnItemClickListener itemClick, boolean isAudioBook) {
        super(context.getResources().getDisplayMetrics().widthPixels - context.getResources().getDimensionPixelSize(R.dimen.alert_dialog_spacing) * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.isAudioBook = isAudioBook;
        this.bookName = bookName;
        this.itemClick = itemClick;
        view = LayoutInflater.from(mContext).inflate(R.layout.dialog_alert, null);
        this.setContentView(view);

        initView();
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.Animation_Pop_AddShelf);
    }

    private void initView() {
        TextView tvBookName = view.findViewById(R.id.tv_msg);
        tvBookName.setText(mContext.getString(R.string.check_add_bookshelf, bookName));
        Button btnExit = view.findViewById(R.id.btn_cancel);
        btnExit.setText(isAudioBook ? "退出听书" : "退出阅读");
        btnExit.setOnClickListener(v -> {
            dismiss();
            itemClick.clickExit();
        });
        Button btnAddShelf = view.findViewById(R.id.btn_done);
        btnAddShelf.setText("放入书架");
        btnAddShelf.setOnClickListener(v -> itemClick.clickAddShelf());
    }
}
