package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.widget.refreshview.scroller.FastScroller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class FindKindAdapter extends RecyclerView.Adapter<FindKindAdapter.ItemViewHolder> implements Filterable, FastScroller.SectionIndexer {
    private List<FindKindGroupBean> dataList;

    private List<FindKindGroupBean> originalList;

    private MyFilter myFilter;

    private final HashSet<String> expandTags = new HashSet<>();

    private final Object lock = new Object();

    private OnGroupItemLongClickListener groupItemLongClickListener;
    private OnChildItemClickListener childItemClickListener;

    private RecyclerView mParent;

    private final LayoutInflater mInflater;
    private boolean autoExpand;

    public FindKindAdapter(Context context, boolean autoExpand) {
        mInflater = LayoutInflater.from(context);
        dataList = new ArrayList<>();
        this.autoExpand = autoExpand;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.item_find_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        FindKindGroupBean item = dataList.get(position);
        holder.tvTitle.setText(item.getGroupName());
        holder.itemView.setOnLongClickListener(v -> {
            if (groupItemLongClickListener != null) {
                groupItemLongClickListener.onGroupItemLongClick(item);
            }
            return true;
        });

        final View.OnClickListener childClickListener = v -> {
            if (childItemClickListener != null) {
                childItemClickListener.onChildItemClick((FindKindBean) v.getTag());
            }
        };
        holder.mTagBox.setVisibility(item.isExpand() ? View.VISIBLE : View.GONE);
        holder.expandView.setSelected(item.isExpand());

        if (item.isExpand()) {
            holder.mTagBox.removeAllViews();
            List<FindKindBean> kindBeans = item.getChildren();
            for (int i = 0, size = kindBeans.size(); i < size; i++) {
                FindKindBean kindBean = kindBeans.get(i);
                addTagView(kindBean, holder.mTagBox, childClickListener);
            }
        }

        final View.OnClickListener expandClickListener = v -> {
            holder.expandView.setSelected(!holder.expandView.isSelected());
            item.setExpand(holder.expandView.isSelected());
            if (holder.mTagBox.isShown()) {
                expandTags.remove(item.getTag());
                holder.mTagBox.setVisibility(View.GONE);
                holder.mTagBox.removeAllViews();
            } else {
                expandTags.add(item.getTag());
                holder.mTagBox.setVisibility(View.VISIBLE);
                holder.mTagBox.removeAllViews();
                List<FindKindBean> kindBeans = item.getChildren();
                for (int i = 0, size = kindBeans.size(); i < size; i++) {
                    FindKindBean kindBean = kindBeans.get(i);
                    addTagView(kindBean, holder.mTagBox, childClickListener);
                }
                mParent.post(() -> mParent.smoothScrollToPosition(holder.getLayoutPosition()));
            }
        };
        holder.itemView.setOnClickListener(expandClickListener);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mParent = recyclerView;
    }


    private void addTagView(FindKindBean kindBean, ViewGroup parent, View.OnClickListener clickListener) {
        TextView tagView = (TextView) mInflater.inflate(R.layout.item_search_history, parent, false);
        tagView.setTag(kindBean);
        tagView.setText(kindBean.getKindName());
        tagView.setOnClickListener(clickListener);
        parent.addView(tagView);
    }

    public void resetDataS(List<FindKindGroupBean> dataS) {
        this.originalList = null;
        this.dataList = dataS;
        if (this.dataList != null && !this.dataList.isEmpty() && autoExpand) {
            this.dataList.get(0).setExpand(true);
        }
        if (this.dataList != null) {
            for (FindKindGroupBean groupBean : this.dataList) {
                if (expandTags.contains(groupBean.getTag())) {
                    groupBean.setExpand(true);
                }
            }
        }

        getFilter().filter();
    }

    public List<FindKindGroupBean> getDataList() {
        return dataList;
    }

    public void setOnGroupItemClickListener(OnGroupItemLongClickListener longClickListener) {
        this.groupItemLongClickListener = longClickListener;
    }

    public void setOnChildItemClickListener(OnChildItemClickListener clickListener) {
        this.childItemClickListener = clickListener;
    }

    @Override
    public MyFilter getFilter() {
        if (myFilter == null) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }

    @Override
    public String getSectionText(int position) {
        String groupName = dataList.get(position % dataList.size()).getGroupName();
        return (dataList == null || StringUtils.isEmpty(groupName)) ? null : groupName.substring(0, 1);
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        FlexboxLayout mTagBox;
        ImageView expandView;

        private ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_kind_name);
            mTagBox = itemView.findViewById(R.id.flex_box_tag);
            expandView = itemView.findViewById(R.id.btn_expander);
        }
    }

    public interface OnGroupItemLongClickListener {

        void onGroupItemLongClick(FindKindGroupBean groupBean);
    }

    public interface OnChildItemClickListener {
        void onChildItemClick(FindKindBean childBean);
    }

    public class MyFilter extends Filter {

        private CharSequence constraint;

        private void filter() {
            filter(constraint);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (originalList == null) {
                synchronized (lock) {
                    originalList = new ArrayList<>(dataList);
                }
            }

            if (TextUtils.isEmpty(constraint)) {
                synchronized (lock) {
                    ArrayList<FindKindGroupBean> list = new ArrayList<>(originalList);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                ArrayList<FindKindGroupBean> list = new ArrayList<>();
                for (FindKindGroupBean groupBean : originalList) {
                    if (StringUtils.containsIgnoreCase(groupBean.getGroupName(), constraint.toString())) {
                        list.add(groupBean);
                    }
                }
                results.values = list;
                results.count = list.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            this.constraint = constraint;
            dataList = (List<FindKindGroupBean>) results.values;
            notifyDataSetChanged();
        }
    }
}
