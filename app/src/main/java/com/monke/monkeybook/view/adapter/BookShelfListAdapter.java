//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.view.adapter.base.BaseBookListAdapter;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.util.Locale;
import java.util.Objects;

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
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        final BookShelfBean item = getItem(holder.getLayoutPosition());
        assert item != null;
        if (TextUtils.isEmpty(item.getCustomCoverPath())) {
            Glide.with(getContext()).load(item.getBookInfoBean().getCoverUrl())
                    .apply(new RequestOptions().dontAnimate()
                            .centerCrop().placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(holder.ivCover);
        } else {
            Glide.with(getContext()).load(item.getCustomCoverPath())
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
            holder.tvRead.setText(getContext().getString(R.string.read_dur_progress, getContext().getString(R.string.text_placeholder)));
        } else {
            holder.tvRead.setText(holder.tvRead.getContext().getString(R.string.read_dur_progress, FormatWebText.trim(durChapterName)));
        }
        String lastChapterName = item.getLastChapterName();
        if (TextUtils.isEmpty(lastChapterName)) {
            holder.tvLast.setText(getContext().getString(R.string.book_search_last, getContext().getString(R.string.text_placeholder)));
        } else {
            holder.tvLast.setText(holder.tvLast.getContext().getString(R.string.book_search_last, FormatWebText.trim(lastChapterName)));
        }

        if (item.getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }

        if (item.getChapterListSize() == 0) {
            holder.tvCurChapter.setText("--/--");
        } else {
            holder.tvCurChapter.setText(String.format(Locale.getDefault(), "%d/%d", item.getDurChapter() + 1, item.getChapterListSize()));
        }

        //进度条
        holder.mpbDurProgress.setVisibility(View.VISIBLE);
        holder.mpbDurProgress.setMaxProgress(item.getChapterListSize());
        float speed = item.getChapterListSize() * 1.0f / 60;

        holder.mpbDurProgress.setSpeed(speed <= 0 ? 1 : speed);

        if (animationIndex < holder.getLayoutPosition()) {
            holder.mpbDurProgress.setDurProgressWithAnim(item.getDurChapter() + 1);
            animationIndex = holder.getLayoutPosition();
        } else {
            holder.mpbDurProgress.setDurProgress(item.getDurChapter() + 1);
        }

        holder.content.setOnClickListener(v -> onClick(v, item));

        if (Objects.equals(getBookshelfPx(), "2")) {
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

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        ImageView ivHasNew;
        TextView tvName;
        TextView tvAuthor;
        TextView tvRead;
        TextView tvLast;
        TextView tvCurChapter;
        MHorProgressBar mpbDurProgress;
        RotateLoading rotateLoading;
        View content;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvRead = itemView.findViewById(R.id.tv_read);
            tvLast = itemView.findViewById(R.id.tv_last);
            mpbDurProgress = itemView.findViewById(R.id.mpb_durProgress);
            tvCurChapter = itemView.findViewById(R.id.tv_current_chapter);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            content = itemView.findViewById(R.id.content_card);
        }
    }
}