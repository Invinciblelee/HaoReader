package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;
import com.monke.monkeybook.widget.AppCompat;

import java.util.List;

import androidx.annotation.NonNull;

public class BookmarkListAdapter extends BaseChapterListAdapter<BookmarkBean> {

    public BookmarkListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
        int color = holder.indicator.getResources().getColor(R.color.tv_chapter_color_normal);
        AppCompat.setTint(holder.indicator, color);
        BookmarkBean bookmarkBean = getItem(realPosition);
        holder.tvName.setText(bookmarkBean.getContent());
        holder.llName.setOnClickListener(v -> callOnItemClickListener(bookmarkBean));
        holder.llName.setOnLongClickListener(view -> {
            callOnItemLongClickListener(bookmarkBean);
            return true;
        });
    }
}
