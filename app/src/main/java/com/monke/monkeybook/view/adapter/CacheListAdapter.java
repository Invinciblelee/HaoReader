package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CacheListAdapter extends RecyclerView.Adapter<CacheListAdapter.ThisViewHolder> {

    private final List<BookShelfBean> dataList = new ArrayList<>();

    private final Context context;
    private final LayoutInflater inflater;

    private OnExtractCacheListener listener;

    public CacheListAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setOnExtractCacheListener(OnExtractCacheListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(inflater.inflate(R.layout.item_cache_extract, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
        BookShelfBean item = dataList.get(position);
        Glide.with(context)
                .load(item.getBookInfoBean().getRealCoverUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                        .dontAnimate().placeholder(R.drawable.img_cover_default)
                        .error(R.drawable.img_cover_default))
                .into(holder.ivCover);

        holder.tvName.setText(item.getBookInfoBean().getName());
        holder.tvCount.setText(String.format(Locale.getDefault(), "共%s章", BookshelfHelp.getCacheChapterCount(item)));
        holder.btnExtract.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExtract(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setDataList(List<BookShelfBean> bookShelfBeans) {
        synchronized (dataList) {
            this.dataList.clear();
            if (bookShelfBeans != null) {
                this.dataList.addAll(bookShelfBeans);
            }
            notifyDataSetChanged();
        }
    }

    public void remove(BookShelfBean bookShelfBean) {
        synchronized (dataList) {
            if (bookShelfBean != null) {
                int index = dataList.indexOf(bookShelfBean);
                if (index >= 0) {
                    dataList.remove(index);
                    notifyItemRemoved(index);
                }
            }
        }
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvName;
        TextView tvCount;
        Button btnExtract;

        ThisViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCount = itemView.findViewById(R.id.tv_chapter_count);
            btnExtract = itemView.findViewById(R.id.btn_extract);
        }
    }

    public interface OnExtractCacheListener {

        void onExtract(BookShelfBean bookShelfBean);

    }
}
