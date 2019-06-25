package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindBean;

import java.util.List;

public class ChoiceBookCategoryAdapter extends RecyclerView.Adapter<ChoiceBookCategoryAdapter.MyViewHolder> {

    private final List<FindKindBean> mKindBeans;

    private final LayoutInflater mInflater;

    private OnItemClickListener mItemClickListener;


    public ChoiceBookCategoryAdapter(Context context, List<FindKindBean> findKindBeans) {
        mInflater = LayoutInflater.from(context);
        mKindBeans = findKindBeans;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.item_choice_book_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FindKindBean item = mKindBeans.get(holder.getLayoutPosition());
        holder.tvName.setText(item.getKindName());
        holder.itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(item, holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mKindBeans == null ? 0 : mKindBeans.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = (TextView) itemView;
        }
    }

    public interface OnItemClickListener {

        void onItemClick(FindKindBean kindBean, int position);
    }

}
