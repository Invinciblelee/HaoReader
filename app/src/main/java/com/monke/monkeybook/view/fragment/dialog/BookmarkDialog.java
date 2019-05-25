package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.utils.StringUtils;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class BookmarkDialog extends AppCompatDialog {
    private TextInputEditText tvContent;

    private boolean isAdd;
    private BookmarkBean bookmarkBean;

    private OnBookmarkClick bookmarkClick;

    public static void show(FragmentManager fragmentManager, BookmarkBean bookmarkBean, boolean isAdd, OnBookmarkClick bookmarkClick) {
        BookmarkDialog dialog = new BookmarkDialog();
        Bundle args = new Bundle();
        args.putBoolean("isAdd", isAdd);
        args.putParcelable("bookmarkBean", bookmarkBean);
        dialog.setArguments(args);
        dialog.bookmarkClick = bookmarkClick;
        dialog.show(fragmentManager, "bookmark");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        isAdd = args.getBoolean("isAdd");
        bookmarkBean = args.getParcelable("bookmarkBean");
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bookmark, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvContent = findViewById(R.id.tie_content);

        TextInputLayout tilReplaceTo = findViewById(R.id.til_content);
        tilReplaceTo.setHint(getResources().getString(R.string.content));
        TextView tvChapterName = findViewById(R.id.tvChapterName);
        tvChapterName.setOnClickListener(v -> {
            if(bookmarkClick != null) {
                bookmarkClick.openBookmark(bookmarkBean);
            }
            dismissAllowingStateLoss();
        });

        View tvOk = findViewById(R.id.tv_ok);
        tvOk.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            bookmarkBean.setContent(StringUtils.valueOf(tvContent.getText()));
            if(bookmarkClick != null) {
                bookmarkClick.saveBookmark(bookmarkBean);
            }
        });

        View tvSave = findViewById(R.id.tv_save);
        tvSave.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            bookmarkBean.setContent(StringUtils.valueOf(tvContent.getText()));
            if(bookmarkClick != null) {
                bookmarkClick.saveBookmark(bookmarkBean);
            }
        });
        View tvDel = findViewById(R.id.tv_del);
        tvDel.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            if(bookmarkClick != null) {
                bookmarkClick.delBookmark(bookmarkBean);
            }
        });

        View llEdit = findViewById(R.id.llEdit);

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


    /**
     * 输入替换规则完成
     */
    public interface OnBookmarkClick {
        void saveBookmark(BookmarkBean bookmarkBean);

        void delBookmark(BookmarkBean bookmarkBean);

        void openBookmark(BookmarkBean bookmarkBean);
    }
}
