//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private Activity activity;
    private final List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int realPosition = holder.getLayoutPosition();
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        final SearchBookBean item = searchBooks.get(realPosition);
        if (!activity.isFinishing()) {
            Glide.with(activity)
                    .load(item.getCoverUrl())
                    .apply(new RequestOptions()
                            .centerCrop()
                            .dontAnimate().placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(myViewHolder.ivCover);
        }
        myViewHolder.tvName.setText(searchBooks.get(position).getName());

        if(!TextUtils.isEmpty(searchBooks.get(position).getAuthor())){
            myViewHolder.tvAuthor.setText(searchBooks.get(position).getAuthor());
        }else {
            myViewHolder.tvAuthor.setText(R.string.author_unknown);
        }
        String state = item.getState();
        if (state == null || state.length() == 0) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        long words = item.getWords();
        if (words <= 0) {
            myViewHolder.tvWords.setVisibility(View.GONE);
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
        if (kind == null || kind.length() <= 0) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
        }
        if (item.getLastChapter() != null && item.getLastChapter().length() > 0)
            myViewHolder.tvLasted.setText(item.getLastChapter());
        else if (item.getDesc() != null && item.getDesc().length() > 0) {
            myViewHolder.tvLasted.setText(item.getDesc());
        } else
            myViewHolder.tvLasted.setText("");
        if (item.getOrigin() != null && item.getOrigin().length() > 0) {
            myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
            myViewHolder.tvOrigin.setText(item.getOrigin());
        } else {
            myViewHolder.tvOrigin.setVisibility(View.GONE);
        }
        myViewHolder.tvOriginNum.setText(String.format("共%d个源", item.getOriginNum()));

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
        TextView tvOrigin;
        TextView tvOriginNum;

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
            tvOriginNum = itemView.findViewById(R.id.tv_origin_num);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        if (newDataS != null && newDataS.size() > 0) {
            saveSearchToDb(newDataS);
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (searchBooks.size() == 0) {
                searchBooks.addAll(newDataS);
                Collections.sort(searchBooks, (o1, o2) -> {
                    if (TextUtils.equals(keyWord, o1.getName())
                            || TextUtils.equals(keyWord, o1.getAuthor())) {
                        return -1;
                    } else if (TextUtils.equals(keyWord, o2.getName())
                            || TextUtils.equals(keyWord, o2.getAuthor())) {
                        return 1;
                    } else if (o1.getName().contains(keyWord) || o1.getAuthor().contains(keyWord)) {
                        return -1;
                    } else if (o2.getName().contains(keyWord) || o2.getAuthor().contains(keyWord)) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
                notifyDataSetChanged();
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {
                    Boolean hasSame = false;
                    for (int i = 0, size = searchBooks.size(); i < size; i++) {
                        SearchBookBean searchBook = searchBooks.get(i);
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            searchBook.addOriginUrl(temp.getTag());
                            notifyItemChanged(i);
                            break;
                        }
                    }

                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.getName())) {
                        for (int i = 0; i < searchBooks.size(); i++) {
                            SearchBookBean searchBook = searchBooks.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                searchBooks.add(i, temp);
                                notifyItemInserted(i);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < searchBooks.size(); i++) {
                            SearchBookBean searchBook = searchBooks.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                searchBooks.add(i, temp);
                                notifyItemInserted(i);
                                break;
                            }
                        }
                    } else if (temp.getName().contains(keyWord) || temp.getAuthor().contains(keyWord)) {
                        for (int i = 0; i < searchBooks.size(); i++) {
                            SearchBookBean searchBook = searchBooks.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                searchBooks.add(i, temp);
                                notifyItemInserted(i);
                                break;
                            }
                        }
                    } else {
                        searchBooks.add(temp);
                        notifyItemInserted(searchBooks.size() - 1);
                    }
                }
            }
        }
    }

    public void clearAll() {
        int bookSize = searchBooks.size();
        if (bookSize > 0) {
            try {
                Glide.with(activity).onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchBooks.clear();
            notifyItemRangeRemoved(0, bookSize);
        }
    }

    public List<SearchBookBean> getSearchBooks() {
        return searchBooks;
    }

    private void saveSearchToDb(List<SearchBookBean> newDataS) {
        Observable.create(e -> {
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao()
                    .insertOrReplaceInTx(newDataS);
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

}