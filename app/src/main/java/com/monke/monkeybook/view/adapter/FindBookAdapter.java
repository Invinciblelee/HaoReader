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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FindBookAdapter extends RecyclerView.Adapter<FindBookAdapter.MyViewHolder> implements Filterable {

    private final List<FindKindGroupBean> mGroupBeans;

    private List<FindKindGroupBean> mOriginalList;

    private MyFilter myFilter;

    private final Context mContext;
    private final LayoutInflater mInflater;

    private OnMultiItemClickListener mItemClickListener;

    public FindBookAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mGroupBeans = new ArrayList<>();
    }

    public void setOnMultiItemClickListener(OnMultiItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setItems(List<FindKindGroupBean> items) {
        synchronized (mGroupBeans) {
            mOriginalList = null;
            mGroupBeans.clear();
            if (items != null) {
                mGroupBeans.addAll(items);
            }
            getFilter().filter();
        }
    }

    public void updateItem(FindKindGroupBean item) {
        synchronized (mGroupBeans) {
            if (item != null) {
                int index = mGroupBeans.indexOf(item);
                if (index >= 0) {
                    mGroupBeans.set(index, item);
                    notifyItemChanged(index, 0);
                }

                if (mOriginalList != null) {
                    index = mOriginalList.indexOf(item);
                    if (index >= 0) {
                        mOriginalList.set(index, item);
                    }
                }
            }
        }
    }

    public void removeItem(FindKindGroupBean item) {
        synchronized (mGroupBeans) {
            if (item != null) {
                int index = mGroupBeans.indexOf(item);
                if (index >= 0) {
                    mGroupBeans.remove(item);
                    notifyItemRemoved(index);
                }
                if (mOriginalList != null) {
                    mOriginalList.remove(item);
                }
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.item_find_book, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FindKindGroupBean item = getItem(holder.getLayoutPosition());
        holder.tvSourceName.setText(item.getGroupName());
        PreviewBooksAdapter adapter = (PreviewBooksAdapter) holder.rvPreview.getAdapter();
        if (adapter == null) {
            adapter = new PreviewBooksAdapter(mContext, item, mItemClickListener);
            holder.rvPreview.setAdapter(adapter);
        } else {
            adapter.setGroupData(item);
        }
        holder.tvSourceName.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemGroupClick(item);
            }
        });
        holder.tvSourceName.setOnLongClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemGroupLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mGroupBeans.size();
    }

    private FindKindGroupBean getItem(int position) {
        return mGroupBeans.get(position);
    }

    @Override
    public MyFilter getFilter() {
        if (myFilter == null) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }

    public interface OnMultiItemClickListener {

        void onItemGroupClick(FindKindGroupBean groupBean);

        void onItemGroupLongClick(FindKindGroupBean groupBean);

        void onItemPreviewClick(SearchBookBean searchBookBean);

    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvSourceName;
        RecyclerView rvPreview;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSourceName = itemView.findViewById(R.id.tv_source_name);
            rvPreview = itemView.findViewById(R.id.rv_book_list);
        }
    }


    private static class PreviewBooksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ITEM = 1;
        private static final int TYPE_FOOTER = 2;

        private FindKindGroupBean mGroupBean;

        private final Context mContext;
        private final LayoutInflater mInflater;

        private OnMultiItemClickListener mItemClickListener;


        PreviewBooksAdapter(Context context, FindKindGroupBean groupBean, OnMultiItemClickListener listener) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
            mGroupBean = groupBean;
            mItemClickListener = listener;
        }

        void setGroupData(FindKindGroupBean groupBean) {
            mGroupBean = groupBean;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            List<SearchBookBean> books = mGroupBean == null ? null : mGroupBean.getBooks();
            if (books == null) {
                return 0;
            }
            return position >= books.size() ? TYPE_FOOTER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                return new MyFooterViewHolder(mInflater.inflate(R.layout.item_find_book_preview_more, parent, false));
            }
            return new MyViewHolder(mInflater.inflate(R.layout.item_find_book_preview, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_ITEM) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                SearchBookBean item = getItem(holder.getLayoutPosition());
                Glide.with(mContext).load(item.getCoverUrl())
                        .apply(new RequestOptions().dontAnimate()
                                .centerCrop().placeholder(R.drawable.img_cover_default)
                                .error(R.drawable.img_cover_default))
                        .into(viewHolder.ivCover);

                viewHolder.tvName.setText(item.getName());
                viewHolder.itemView.setOnClickListener(v -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemPreviewClick(item);
                    }
                });
            } else {
                MyFooterViewHolder viewHolder = (MyFooterViewHolder) holder;
                viewHolder.itemView.setOnClickListener(v -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemGroupClick(mGroupBean);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            List<SearchBookBean> books = mGroupBean == null ? null : mGroupBean.getBooks();
            if (books == null) {
                return 0;
            }
            final int size = books.size();
            return size == 0 ? 0 : size + 1;
        }

        private SearchBookBean getItem(int position) {
            return mGroupBean.getBooks().get(position);
        }

        static class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivCover;
            TextView tvName;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }

        static class MyFooterViewHolder extends RecyclerView.ViewHolder {

            MyFooterViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }


    public class MyFilter extends Filter {

        private CharSequence constraint;

        private void filter() {
            filter(constraint);
        }

        public boolean clearFilter() {
            if (!TextUtils.isEmpty(constraint)) {
                filter(null);
                return true;
            }
            return false;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (mOriginalList == null) {
                synchronized (mGroupBeans) {
                    mOriginalList = new ArrayList<>(mGroupBeans);
                }
            }

            if (TextUtils.isEmpty(constraint)) {
                synchronized (mGroupBeans) {
                    ArrayList<FindKindGroupBean> list = new ArrayList<>(mOriginalList);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                ArrayList<FindKindGroupBean> list = new ArrayList<>();
                final String key = constraint.toString();
                for (FindKindGroupBean groupBean : mOriginalList) {
                    if (StringUtils.containsIgnoreCase(groupBean.getGroupName(), key)) {
                        list.add(groupBean);
                    }
                }
                results.values = list;
                results.count = list.size();

            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            this.constraint = constraint;
            mGroupBeans.clear();
            notifyDataSetChanged();
            if (results.count > 0) {
                mGroupBeans.addAll((List<FindKindGroupBean>) results.values);
                notifyDataSetChanged();
            }
        }
    }
}
