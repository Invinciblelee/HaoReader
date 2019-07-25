//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookKindBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private final List<SearchBookBean> searchBooks;

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
            myViewHolder.tvOrigin.setText(item.getOrigin());
        } else {
            myViewHolder.tvOrigin.setVisibility(View.INVISIBLE);
        }

        myViewHolder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickItem(v, realPosition, item);
        });
        myViewHolder.btnAddShelf.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.clickToSearch(myViewHolder.btnAddShelf, realPosition, item);
        });

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
        Button btnAddShelf;
        TextView tvOrigin;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            btnAddShelf = itemView.findViewById(R.id.btn_search_book);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            btnAddShelf.setVisibility(View.VISIBLE);

            int paddingEnd = ScreenUtils.dpToPx(72);
            tvAuthor.setPadding(0, 0, paddingEnd, 0);
            tvOrigin.setPadding(0, 0, paddingEnd, 0);
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