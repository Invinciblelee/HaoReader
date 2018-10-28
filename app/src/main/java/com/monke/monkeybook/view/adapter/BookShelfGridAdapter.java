//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.view.activity.MainActivity;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfGridAdapter extends RecyclerView.Adapter<BookShelfGridAdapter.MyViewHolder> {

    private List<BookShelfBean> books;

    private OnItemClickListenerTwo itemClickListener;
    private String bookshelfPx;
    private Activity activity;

    private int animationIndex = -1;

    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            int start = srcPosition;
            int end = targetPosition;
            if (start > end) {
                start = targetPosition;
                end = srcPosition;
            }
            notifyItemRangeChanged(start, end - start + 1);
            return true;
        }

        @Override
        public void onRelease() {
            ((MainActivity) activity).saveData();
        }
    };

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public BookShelfGridAdapter(Activity activity) {
        this.activity = activity;
        books = new ArrayList<>();
    }

    public boolean updateBook(BookShelfBean bookShelf, boolean sort) {
        if (bookShelf != null) {
            for (int i = 0, size = books.size(); i < size; i++) {
                if (Objects.equals(books.get(i).getNoteUrl(), bookShelf.getNoteUrl())) {
                    books.set(i, bookShelf);
                    if (sort) {
                        BookshelfHelp.order(books, bookshelfPx);
                        notifyDataSetChanged();
                    } else {
                        notifyItemChanged(i);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void addBook(BookShelfBean bookShelf) {
        if (books == null) {
            books = new ArrayList<>();
        }
        if (bookShelf != null && !updateBook(bookShelf, true)) {
            books.add(bookShelf);
            BookshelfHelp.order(books, bookshelfPx);
            notifyDataSetChanged();
        }
    }

    public void removeBook(BookShelfBean bookShelf) {
        if (bookShelf == null || books == null || books.isEmpty()) {
            return;
        }

        int index = -1;
        for (int i = 0, size = books.size(); i < size; i++) {
            if (Objects.equals(books.get(i).getNoteUrl(), bookShelf.getNoteUrl())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            books.remove(index);
            notifyItemRemoved(index);
        }

    }

    public void sort() {
        if (books != null) {
            BookshelfHelp.order(books, bookshelfPx);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        //如果不为0，按正常的流程跑
        return books.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookshelf_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int index) {
        final BookShelfBean item = books.get(holder.getLayoutPosition());
        holder.tvName.setText(item.getBookInfoBean().getName());
        if (!activity.isFinishing()) {
            if (TextUtils.isEmpty(item.getCustomCoverPath())) {
                Glide.with(activity).load(item.getBookInfoBean().getCoverUrl())
                        .apply(new RequestOptions().dontAnimate()
                                .centerCrop().placeholder(R.drawable.img_cover_default)
                                .error(R.drawable.img_cover_default))
                        .into(holder.ivCover);
            } else if (item.getCustomCoverPath().startsWith("http")) {
                Glide.with(activity).load(item.getCustomCoverPath())
                        .apply(new RequestOptions().dontAnimate()
                                .centerCrop().placeholder(R.drawable.img_cover_default)
                                .error(R.drawable.img_cover_default))
                        .into(holder.ivCover);
            } else {
                holder.ivCover.setImageBitmap(BitmapFactory.decodeFile(item.getCustomCoverPath()));
            }
        }
        if (item.getHasUpdate()) {
            holder.ivHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
        }

        holder.content.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, holder.getLayoutPosition());
        });

        if (Objects.equals(bookshelfPx, "2")) {
            holder.tvName.setClickable(true);
            holder.tvName.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, holder.getLayoutPosition());
                }
                return true;
            });
        }

        if (!Objects.equals(bookshelfPx, "2")) {
            holder.tvName.setClickable(false);
            holder.content.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, holder.getLayoutPosition());
                }
                return true;
            });
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

        if (item.isLoading()) {
            holder.ivHasNew.setVisibility(View.INVISIBLE);
            holder.rotateLoading.setVisibility(View.VISIBLE);
            holder.rotateLoading.start();
        } else {
            holder.rotateLoading.setVisibility(View.INVISIBLE);
            holder.rotateLoading.stop();
        }
    }

    public void setItemClickListener(OnItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void replaceAll(List<BookShelfBean> newDataS, String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
        if (null != newDataS && newDataS.size() > 0) {
            BookshelfHelp.order(newDataS, bookshelfPx);
            books = newDataS;
        } else {
            books.clear();
        }
        notifyDataSetChanged();
    }

    public List<BookShelfBean> getBooks() {
        return books;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        ImageView ivHasNew;
        AutofitTextView tvName;
        MHorProgressBar mpbDurProgress;
        RotateLoading rotateLoading;
        View content;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivHasNew = itemView.findViewById(R.id.iv_has_new);
            tvName = itemView.findViewById(R.id.tv_name);
            mpbDurProgress = itemView.findViewById(R.id.mpb_durProgress);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            content = itemView.findViewById(R.id.content_card);
        }
    }
}