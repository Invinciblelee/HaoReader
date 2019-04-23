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

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookKindBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private WeakReference<Activity> activityRef;
    private List<SearchBookBean> searchBooks;

    public interface OnItemClickListener {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    private OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity, boolean needLoadMore) {
        super(needLoadMore);
        this.activityRef = new WeakReference<>(activity);
        searchBooks = new ArrayList<>();
    }

    public SearchBookAdapter(Activity activity) {
        this(activity, true);
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
        } else if (TextUtils.equals(bookType, BookType.DOWNLOAD)) {
            builder.insert(0, activity.getString(R.string.book_download));
        }
        myViewHolder.tvName.setText(builder.toString());

        if (!StringUtils.isEmpty(item.getAuthor())) {
            myViewHolder.tvAuthor.setText(item.getAuthor());
        } else {
            myViewHolder.tvAuthor.setText(R.string.author_unknown);
        }

        BookKindBean kindBean = new BookKindBean(item.getKind());
        String state = kindBean.getState();
        if (StringUtils.isEmpty(state)) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(state);
        }
        String words = kindBean.getWordsS();
        if (StringUtils.isEmpty(words)) {
            myViewHolder.tvWords.setVisibility(View.INVISIBLE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(words);
        }
        String kind = kindBean.getKind();
        if (StringUtils.isEmpty(kind)) {
            myViewHolder.tvKind.setVisibility(View.GONE);
        } else {
            myViewHolder.tvKind.setVisibility(View.VISIBLE);
            myViewHolder.tvKind.setText(kind);
        }

        String desc = !StringUtils.isEmpty(item.getLastChapter()) ? item.getLastChapter()
                : !StringUtils.isEmpty(item.getDesc()) ? item.getDesc() : "";
        myViewHolder.tvLasted.setText(desc);

        if (!StringUtils.isEmpty(item.getOrigin())) {
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

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(newDataS);
                sortSearchBooks(copyDataS, keyWord);
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {
                    boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (TextUtils.equals(temp.getBookType(), searchBook.getBookType())
                                && TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            searchBook.addTag(temp.getTag());
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
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (temp.getName().contains(keyWord) || temp.getAuthor().contains(keyWord)) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }
            searchBooks = copyDataS;
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(this::notifyDataSetChanged);
            }
        }
    }


    public void clearAll() {
        if (searchBooks == null || searchBooks.isEmpty()) {
            return;
        }
        int previousSize = searchBooks.size();
        searchBooks.clear();
        notifyItemRangeRemoved(0, previousSize);
    }

    private void sortSearchBooks(List<SearchBookBean> searchBookBeans, String keyWord) {
        Collections.sort(searchBookBeans, (o1, o2) -> {
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
    }

}