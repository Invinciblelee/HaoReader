//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookKindBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.SearchBookHelp;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private WeakReference<Activity> activityRef;
    private final List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity, boolean needLoadMore) {
        super(needLoadMore);
        this.activityRef = new WeakReference<>(activity);
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        final int realPosition = holder.getLayoutPosition();
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        final SearchBookBean item = searchBooks.get(realPosition);
        if (!payloads.isEmpty()) {
            myViewHolder.tvOrigin.setText(String.format("%s  共%d个源", item.getOrigin(), item.getOriginNum()));
            return;
        }
        Activity activity = activityRef.get();
        Glide.with(activity)
                .load(item.getCoverUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .dontAnimate().placeholder(R.drawable.img_cover_default)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(R.drawable.img_cover_default))
                .into(myViewHolder.ivCover);

        StringBuilder builder = new StringBuilder(item.getName());
        String bookType = item.getBookType();
        if (TextUtils.equals(bookType, BookType.AUDIO)) {
            builder.insert(0, activity.getString(R.string.book_audio));
        }
        myViewHolder.tvName.setText(builder.toString());

        if (!StringUtils.isBlank(item.getAuthor())) {
            myViewHolder.tvAuthor.setText(item.getAuthor());
        } else {
            myViewHolder.tvAuthor.setText(R.string.author_unknown);
        }

        BookKindBean kindBean = new BookKindBean(item.getKind());
        String state = kindBean.getState();
        if (StringUtils.isBlank(state)) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        String words = kindBean.getWordsS();
        if (StringUtils.isBlank(words)) {
            myViewHolder.tvWords.setVisibility(View.INVISIBLE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(words);
        }
        String kind = kindBean.getKind();
        if (StringUtils.isBlank(kind)) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
        }

        final String lastChapter = item.getDisplayLastChapter();
        String desc = !StringUtils.isBlank(lastChapter) ? lastChapter
                : !StringUtils.isBlank(item.getIntroduce()) ? item.getIntroduce() : "";
        myViewHolder.tvLasted.setText(StringUtils.trim(desc));

        if (!StringUtils.isBlank(item.getOrigin())) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(String.format("%s  共%d个源", item.getOrigin(), item.getOriginNum()));
        } else {
            myViewHolder.tvOrigin.setVisibility(View.INVISIBLE);
        }

        myViewHolder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(v, realPosition, item);
        });
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks == null ? 0 : searchBooks.size();
    }

    public boolean isEmpty() {
        return getICount() == 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvAuthor;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvOrigin;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBookBean> newDataS, String keyWord) {
        synchronized (searchBooks) {
            Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                SearchBookHelp.addSearchBooks(searchBooks, newDataS, keyWord);
                activity.runOnUiThread(this::notifyDataSetChanged);
            }
        }
    }


    public void clearAll() {
        synchronized (searchBooks) {
            searchBooks.clear();
            notifyDataSetChanged();
        }
    }

}