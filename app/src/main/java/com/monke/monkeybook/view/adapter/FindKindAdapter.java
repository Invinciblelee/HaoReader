package com.monke.monkeybook.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindGroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class FindKindAdapter extends BaseExpandableListAdapter {
    private List<FindKindGroupBean> dataList;

    private OnGroupItemClickListener itemClickListener;

    public FindKindAdapter() {
        dataList = new ArrayList<>();
    }

    public void resetDataS(List<FindKindGroupBean> dataS) {
        this.dataList = dataS;
        notifyDataSetChanged();
    }

    public List<FindKindGroupBean> getDataList() {
        return dataList;
    }

    public void setOnGroupItemClickListener(OnGroupItemClickListener longClickLitener){
        this.itemClickListener = longClickLitener;
    }

    @Override
    public int getGroupCount() {
        return dataList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return dataList.get(i).getChildrenCount();
    }

    @Override
    public Object getGroup(int i) {
        return dataList.get(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataList.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_group, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = convertView.findViewById(R.id.tv_kind_name);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(dataList.get(i).getGroupName());

        convertView.setOnClickListener(v -> {
            if(itemClickListener != null){
                itemClickListener.onGroupItemClick(i, v);
            }
        });

        convertView.setOnLongClickListener(v -> {
           if(itemClickListener != null){
               itemClickListener.onGroupItemLongClick(dataList.get(i));
           }
            return true;
        });
        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_kind, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = convertView.findViewById(R.id.tv_kind_name);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        childViewHolder.tvTitle.setText(dataList.get(i).getChildren().get(i1).getKindName());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    private static class GroupViewHolder {
        TextView tvTitle;
    }

    private static class ChildViewHolder {
        TextView tvTitle;
    }

    public interface OnGroupItemClickListener {
        void onGroupItemClick(int groupPosition, View view);

        void onGroupItemLongClick(FindKindGroupBean groupBean);
    }
}
