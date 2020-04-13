package com.monke.monkeybook.view.adapter;

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

import java.util.ArrayList;
import java.util.List;

public class RecentlyViewedAdapter extends RecyclerView.Adapter<RecentlyViewedAdapter.MyViewHolder> {

    private final List<BookShelfBean> mList = new ArrayList<>();

    private final LayoutInflater mInflater;

    private OnItemClickCallback mCallback;

    public RecentlyViewedAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setItems(List<BookShelfBean> bookShelfBeans) {
        mList.clear();
        mList.addAll(bookShelfBeans);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallback(OnItemClickCallback clickCallback) {
        mCallback = clickCallback;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.item_recently_viewed, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final BookShelfBean item = mList.get(holder.getLayoutPosition());

        Glide.with(holder.itemView.getContext())
                .load(item.getBookInfoBean().getRealCoverUrl())
                .apply(new RequestOptions()
                        .fitCenter().dontAnimate()
                        .placeholder(R.drawable.img_cover_default)
                        .error(R.drawable.img_cover_default))
                .into(holder.ivCover);

        holder.tvName.setText(item.getBookInfoBean().getName());

        holder.itemView.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onClick(v, item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (mCallback != null) {
                mCallback.onLongClick(v, item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    public static interface OnItemClickCallback {

        void onClick(View itemView, BookShelfBean item);

        void onLongClick(View itemView, BookShelfBean item);
    }
}
