//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.view.adapter.base.BaseBookListAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.util.List;
import java.util.Locale;

public class BookShelfListAdapter extends BaseBookListAdapter<BookShelfListAdapter.MyViewHolder> {

    public BookShelfListAdapter(Context context, int group, int bookPx) {
        super(context, group, bookPx);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
        final BookShelfBean item = getItem(holder.getLayoutPosition());
        assert item != null;
        if (TextUtils.isEmpty(item.getBookInfoBean().getCustomCoverPath())) {
            Glide.with(getContext()).load(item.getBookInfoBean().getCoverUrl())
                    .apply(new RequestOptions().dontAnimate()
                            .centerCrop().placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(holder.ivCover);
        } else {
            Glide.with(getContext()).load(item.getBookInfoBean().getCustomCoverPath())
                    .apply(new RequestOptions().dontAnimate()
                            .centerCrop().placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(holder.ivCover);
        }
        holder.tvName.setText(item.getBookInfoBean().getName());

        if (!TextUtils.isEmpty(item.getBookInfoBean().getAuthor())) {
            holder.tvAuthor.setText(item.getBookInfoBean().getAuthor());
        } else {
            holder.tvAuthor.setText(R.string.author_unknown);
        }

        String durChapterName = item.getDurChapterName();
        if (TextUtils.isEmpty(durChapterName)) {
            String bookType = item.getBookInfoBean().getBookType();
            holder.tvRead.setText(getContext().getString(TextUtils.equals(bookType, Constant.BookType.AUDIO) ?
                    R.string.play_dur_progress : R.string.read_dur_progress, getContext().getString(R.string.text_placeholder)));
        } else {
            holder.tvRead.setText(FormatWebText.trim(durChapterName));
        }
        String lastChapterName = item.getLastChapterName();
        if (TextUtils.isEmpty(lastChapterName)) {
            holder.tvLast.setText(getContext().getString(R.string.book_search_last, getContext().getString(R.string.text_placeholder)));
        } else {
            holder.tvLast.setText(FormatWebText.trim(lastChapterName));
        }

        if (item.getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }

        holder.content.setOnClickListener(v -> onClick(v, item));

        if (getBookshelfPx() == 2) {
            holder.ivCover.setClickable(true);
            holder.ivCover.setOnClickListener(v -> onLongClick(v, item));
            holder.content.setOnLongClickListener(null);
        } else {
            holder.ivCover.setClickable(false);
            holder.content.setOnLongClickListener(v -> {
                onLongClick(v, item);
                return true;
            });
        }

        if (item.isLoading()) {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
            holder.rotateLoading.setVisibility(View.VISIBLE);
            holder.rotateLoading.start();
        } else {
            holder.rotateLoading.setVisibility(View.INVISIBLE);
            holder.rotateLoading.stop();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        ImageView ivHasNew;
        TextView tvName;
        TextView tvAuthor;
        TextView tvRead;
        TextView tvLast;
        RotateLoading rotateLoading;
        public View content;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            content = itemView.findViewById(R.id.content_card);
        }
    }
}