package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.List;

public class BookmarkListAdapter extends BaseChapterListAdapter<BookmarkBean> {

    public BookmarkListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        BookmarkBean bookmarkBean = getItem(realPosition);
        holder.tvName.setText(bookmarkBean.getContent());
        holder.llName.setOnClickListener(v -> callOnItemClickListener(bookmarkBean));
        holder.llName.setOnLongClickListener(view -> {
            callOnItemLongClickListener(bookmarkBean);
            return true;
        });
    }
}
