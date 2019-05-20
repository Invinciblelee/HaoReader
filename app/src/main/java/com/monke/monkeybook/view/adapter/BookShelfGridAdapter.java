//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.base.BaseBookListAdapter;
import com.monke.monkeybook.widget.RotateLoading;

import java.util.List;

public class BookShelfGridAdapter extends BaseBookListAdapter<BookShelfGridAdapter.MyViewHolder> {

    public BookShelfGridAdapter(Context context, int group, int bookPx) {
        super(context, group, bookPx);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
        final BookShelfBean item = getItem(holder.getLayoutPosition());
        holder.tvName.setText(item.getBookInfoBean().getName());
        Glide.with(getContext()).load(item.getBookInfoBean().getRealCoverUrl())
                .apply(new RequestOptions().dontAnimate()
                        .centerCrop().placeholder(R.drawable.img_cover_default)
                        .error(R.drawable.img_cover_default))
                .into(holder.ivCover);

        holder.content.setOnClickListener(v -> onClick(v, item));

        if (getBookshelfPx() == 2) {
            holder.tvName.setClickable(true);
            holder.tvName.setOnClickListener(v -> onLongClick(v, item));
            holder.content.setOnLongClickListener(null);
        } else {
            holder.tvName.setClickable(false);
            holder.content.setOnLongClickListener(v -> {
                onLongClick(v, item);
                return true;
            });
        }

        if (item.isFlag()) {
            holder.rotateLoading.setVisibility(View.VISIBLE);
        } else {
            holder.rotateLoading.setVisibility(View.INVISIBLE);
        }

        if (item.getHasUpdate() && !item.isLocalBook()) {
            holder.tvHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.tvHasNew.setVisibility(View.INVISIBLE);
        }

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvHasNew;
        TextView tvName;
        RotateLoading rotateLoading;
        public View content;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvHasNew = itemView.findViewById(R.id.tv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            content = itemView.findViewById(R.id.content_card);
        }
    }
}