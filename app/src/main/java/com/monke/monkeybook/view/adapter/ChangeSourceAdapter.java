package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RefreshRecyclerViewAdapter {
    private List<SearchBookBean> searchBookBeans;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private boolean noSelected;
    private int lastSelectIndex = -1;

    public ChangeSourceAdapter(Context context, boolean noSelected) {
        super(false);
        this.mContext = context;
        this.noSelected = noSelected;

        searchBookBeans = new ArrayList<>();
    }

    public synchronized void addSourceAdapter(SearchBookBean value) {
        searchBookBeans.add(value);
        Collections.sort(searchBookBeans);
        notifyDataSetChanged();
    }

    public synchronized void addAllSourceAdapter(List<SearchBookBean> value) {
        searchBookBeans.addAll(value);
        Collections.sort(searchBookBeans);
        notifyDataSetChanged();
    }

    public synchronized void reSetSourceAdapter() {
        searchBookBeans.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, SearchBookBean searchBookBean);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return searchBookBeans;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookSource;
        TextView tvLastChapter;
        RadioButton ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            tvBookSource = itemView.findViewById(R.id.tv_source_name);
            tvLastChapter = itemView.findViewById(R.id.tv_lastChapter);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_source, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (!payloads.isEmpty()) {
            myViewHolder.ivChecked.setChecked(false);
        }else {
            final int realPosition = holder.getLayoutPosition();
            final SearchBookBean item = searchBookBeans.get(realPosition);
            myViewHolder.tvBookSource.setText(item.getOrigin());
            if (isEmpty(item.getLastChapter())) {
                myViewHolder.tvLastChapter.setText(R.string.no_last_chapter);
            } else {
                myViewHolder.tvLastChapter.setText(item.getLastChapter());
            }

            if (!noSelected) {
                myViewHolder.ivChecked.setChecked(item.getIsCurrentSource());
                if (myViewHolder.ivChecked.isChecked()) {
                    lastSelectIndex = realPosition;
                }
            }

            myViewHolder.itemView.setOnClickListener(view -> {
                if (lastSelectIndex != -1 && lastSelectIndex != holder.getLayoutPosition()) {
                    notifyItemChanged(lastSelectIndex, 0);
                }
                if (!myViewHolder.ivChecked.isChecked()) {
                    myViewHolder.ivChecked.setChecked(true);
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, item);
                }
            });
        }
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBookBeans.size();
    }
}
