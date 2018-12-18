//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.app.Activity;
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
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickToSearch(View clickView, int position, SearchBookBean searchBookBean);

        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public ChoiceBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @Override
    public void onBindIViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final int realPosition = holder.getLayoutPosition();
        final SearchBookBean item = searchBooks.get(realPosition);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(item.getCoverUrl())
                    .apply(new RequestOptions()
                            .fitCenter().dontAnimate()
                            .placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(item.getName());

        if (!TextUtils.isEmpty(item.getAuthor())) {
            myViewHolder.tvAuthor.setText(item.getAuthor());
        } else {
            myViewHolder.tvAuthor.setText(R.string.author_unknown);
        }

        String state = item.getState();
        if (TextUtils.isEmpty(state)) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        long words = item.getWords();
        if (words <= 0) {
            myViewHolder.tvWords.setVisibility(View.INVISIBLE);
        } else {
            String wordsS = Long.toString(words) + "字";
            if (words > 10000) {
                DecimalFormat df = new DecimalFormat("#.#");
                wordsS = df.format(words * 1.0f / 10000f) + "万字";
            }
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(wordsS);
        }
        String kind = item.getKind();
        if (TextUtils.isEmpty(kind)) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
        }

        String desc = !TextUtils.isEmpty(item.getLastChapter()) ? item.getLastChapter()
                : !TextUtils.isEmpty(item.getDesc()) ? item.getDesc() : "";
        myViewHolder.tvLasted.setText(desc);

        if (!TextUtils.isEmpty(item.getOrigin())) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(item.getOrigin());
        } else {
            myViewHolder.tvOrigin.setVisibility(View.INVISIBLE);
        }

        myViewHolder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(v, realPosition, item);
        });
        myViewHolder.tvAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickToSearch(myViewHolder.tvAddShelf, realPosition, item);
        });

        myViewHolder.originView.setPadding(0, 0, ScreenUtils.dpToPx(72), 0);
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvAuthor;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvAddShelf;
        TextView tvOrigin;
        View originView;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvAddShelf = itemView.findViewById(R.id.btn_search_book);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            originView = itemView.findViewById(R.id.ll_origin);
            tvAddShelf.setVisibility(View.VISIBLE);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getICount();
            if (newData.size() > 0) {
                searchBooks.addAll(newData);
            }
            notifyItemInserted(position);
            notifyItemRangeChanged(position, newData.size());
        }
    }

    public void replaceAll(List<SearchBookBean> newData) {
        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }
}