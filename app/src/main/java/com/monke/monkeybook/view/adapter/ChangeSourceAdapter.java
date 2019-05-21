package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerViewAdapter;
import com.monke.monkeybook.widget.refreshview.scroller.FastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class ChangeSourceAdapter extends RefreshRecyclerViewAdapter implements FastScroller.SectionIndexer {
    private final List<SearchBookBean> searchBookBeans;
    private OnItemClickListener mOnItemClickListener;
    private LayoutInflater inflater;
    private boolean selectCover;
    private int lastSelectIndex = -1;

    public ChangeSourceAdapter(Context context, boolean selectCover) {
        super(false);
        this.selectCover = selectCover;
        this.inflater = LayoutInflater.from(context);

        searchBookBeans = new ArrayList<>();
    }

    public void addAllSourceAdapter(List<SearchBookBean> value) {
        synchronized (searchBookBeans) {
            searchBookBeans.addAll(value);
            ListUtils.removeDuplicate(searchBookBeans);
            Collections.sort(searchBookBeans);
            notifyDataSetChanged();
        }
    }

    public void reSetSourceAdapter() {
        synchronized (searchBookBeans) {
            searchBookBeans.clear();
            notifyDataSetChanged();
        }
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
        return new MyViewHolder(inflater.inflate(R.layout.item_change_source, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (!payloads.isEmpty()) {
            myViewHolder.ivChecked.setChecked(false);
        } else {
            final int realPosition = holder.getLayoutPosition();
            final SearchBookBean item = searchBookBeans.get(realPosition);
            myViewHolder.tvBookSource.setText(item.getOrigin());
            if (isEmpty(item.getLastChapter())) {
                myViewHolder.tvLastChapter.setText(R.string.no_last_chapter);
            } else {
                myViewHolder.tvLastChapter.setText(item.getLastChapter());
            }

            if (!selectCover) {
                myViewHolder.ivChecked.setChecked(item.isCurrentSource());
                if (myViewHolder.ivChecked.isChecked()) {
                    lastSelectIndex = realPosition;
                }
            }

            final View.OnClickListener clickListener = view -> {
                if (selectCover) return;

                if (lastSelectIndex != -1 && lastSelectIndex != holder.getLayoutPosition()) {
                    notifyItemChanged(lastSelectIndex, 0);
                }
                if (!myViewHolder.ivChecked.isChecked()) {
                    myViewHolder.ivChecked.setChecked(true);
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, item);
                }
            };

            myViewHolder.itemView.setOnClickListener(clickListener);
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

    @Override
    public CharSequence getSectionText(int position) {
        if (!searchBookBeans.isEmpty()) {
            String name = searchBookBeans.get(position).getOrigin();
            return name.substring(0, 1);
        }
        return "";
    }
}
