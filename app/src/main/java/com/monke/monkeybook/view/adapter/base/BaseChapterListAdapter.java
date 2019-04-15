package com.monke.monkeybook.view.adapter.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FilterBean;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseChapterListAdapter<T extends FilterBean> extends RecyclerView.Adapter<BaseChapterListAdapter.ThisViewHolder> implements Filterable {

    private final Context mContext;
    private final LayoutInflater mInflater;

    private final Object lock = new Object();

    private List<T> dataList;
    private List<T> originalList;

    private MyFilter myFilter;

    private OnItemClickListener<T> itemClickListener;


    public BaseChapterListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener<T> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    protected void callOnItemClickListener(T item) {
        if (itemClickListener != null) {
            itemClickListener.itemClick(item);
        }
    }

    protected void callOnItemLongClickListener(T item) {
        if (itemClickListener != null) {
            itemClickListener.itemLongClick(item);
        }
    }

    public void setDataList(List<T> dataList) {
        synchronized (lock){
            this.originalList = null;
            this.dataList = dataList;
            getFilter().filter();
        }
    }

    public void removeData(T data){
        synchronized (lock){
            this.originalList = null;
            if(this.dataList != null){
                int index = this.dataList.indexOf(data);
                if(index >= 0){
                    this.dataList.remove(index);
                    notifyItemRemoved(index);
                }
            }
        }
    }

    public T getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @NonNull
    @Override
    public final ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(mInflater.inflate(R.layout.item_chapter_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
    }

    @Override
    public final int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public MyFilter getFilter() {
        if (myFilter == null) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }

    protected static class ThisViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public View line;
        public View llName;
        public View indicator;

        ThisViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            line = itemView.findViewById(R.id.v_line);
            llName = itemView.findViewById(R.id.ll_name);
            indicator = itemView.findViewById(R.id.iv_indicator);
        }
    }

    public abstract static class OnItemClickListener<T> {
        public void itemClick(T item){

        }

        public void itemLongClick(T item){

        }
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
                    ArrayList<T> list = new ArrayList<>(originalList);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                ArrayList<T> list = new ArrayList<>();
                for (T item : originalList) {
                    String[] filters = item.getFilters();
                    for (String filter : filters) {
                        if (StringUtils.containsIgnoreCase(filter, constraint.toString())) {
                            list.add(item);
                            break;
                        }
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
            dataList = (List<T>) results.values;
            notifyDataSetChanged();
        }
    }
}
