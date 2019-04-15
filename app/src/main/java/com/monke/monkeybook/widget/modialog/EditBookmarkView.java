package com.monke.monkeybook.widget.modialog;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.utils.KeyboardUtil;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditBookmarkView {
    private TextView tvChapterName;
    private TextInputEditText tvContent;
    private View llEdit;
    private View tvOk;

    private MoDialogView moDialogView;
    private OnBookmarkClick bookmarkClick;
    private Context context;
    private BookmarkBean bookmarkBean;

    public static EditBookmarkView newInstance(MoDialogView moDialogView) {
        return new EditBookmarkView(moDialogView);
    }

    private EditBookmarkView(MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        bindView();
    }

    void showBookmark(@NonNull BookmarkBean bookmarkBean, boolean isAdd, final OnBookmarkClick bookmarkClick) {
        this.bookmarkClick = bookmarkClick;
        this.bookmarkBean = bookmarkBean;

        if (isAdd) {
            llEdit.setVisibility(View.GONE);
            tvOk.setVisibility(View.VISIBLE);
            tvChapterName.setEnabled(false);
        } else {
            llEdit.setVisibility(View.VISIBLE);
            tvOk.setVisibility(View.GONE);
            tvChapterName.setEnabled(true);
        }

        tvChapterName.setText(bookmarkBean.getChapterName());
        tvContent.setText(bookmarkBean.getContent());
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_bookmark, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);

        TextInputLayout tilReplaceTo = moDialogView.findViewById(R.id.til_content);
        tilReplaceTo.setHint(context.getString(R.string.content));
        tvChapterName = moDialogView.findViewById(R.id.tvChapterName);
        tvChapterName.setOnClickListener(view -> {
            bookmarkClick.openBookmark(bookmarkBean);
            dismiss();
        });
        tvContent = moDialogView.findViewById(R.id.tie_content);

        tvOk = moDialogView.findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            dismiss();
        });

        View tvSave = moDialogView.findViewById(R.id.tv_save);
        tvSave.setOnClickListener(view -> {
            bookmarkBean.setContent(tvContent.getText().toString());
            bookmarkClick.saveBookmark(bookmarkBean);
            dismiss();
        });
        View tvDel = moDialogView.findViewById(R.id.tv_del);
        tvDel.setOnClickListener(view -> {
            bookmarkClick.delBookmark(bookmarkBean);
            dismiss();
        });

        llEdit = moDialogView.findViewById(R.id.llEdit);

        KeyboardUtil.resetViewPosition((Activity) context, moDialogView.findViewById(R.id.cv_root));
    }

    private void dismiss() {
        moDialogView.getMoDialogHUD().dismiss();
    }

    /**
     * 输入替换规则完成
     */
    public interface OnBookmarkClick {
        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void openBookmark(BookmarkBean bookmarkBean);
    }
}
