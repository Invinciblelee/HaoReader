package com.monke.monkeybook.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;
import com.monke.monkeybook.widget.refreshview.scroller.FastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> implements FastScroller.SectionIndexer {
    private List<BookSourceBean> dataList;
    private List<BookSourceBean> allDataList;
    private BookSourceActivity activity;
    private boolean canTop;

    private final Object lock = new Object();

    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int position) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            BookSourceBean src = dataList.get(srcPosition);
            BookSourceBean target = dataList.get(targetPosition);
            int srcWeight = src.getWeight();
            int targetWeight = target.getWeight();
            src.setWeight(targetWeight);
            target.setWeight(srcWeight);
            Collections.swap(dataList, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            return true;
        }

        @Override
        public void onRelease() {
            activity.saveDate(dataList);
        }
    };

    public BookSourceAdapter(BookSourceActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void resetDataS(List<BookSourceBean> bookSourceBeanList) {
        synchronized (lock) {
            this.dataList = bookSourceBeanList;
            notifyDataSetChanged();
            activity.upDateSelectAll();
            activity.upSearchView(dataList.size());
            activity.upGroupMenu();
        }
    }

    private void setAllDataList(List<BookSourceBean> bookSourceBeanList) {
        synchronized (lock) {
            this.allDataList = bookSourceBeanList;
            notifyDataSetChanged();
            activity.upDateSelectAll();
        }
    }

    public List<BookSourceBean> getDataList() {
        return dataList;
    }

    public List<BookSourceBean> getSelectDataList() {
        List<BookSourceBean> selectDataS = new ArrayList<>();
        for (BookSourceBean data : dataList) {
            if (data.getEnable()) {
                selectDataS.add(data);
            }
        }
        return selectDataS;
    }

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }


    public void setCanTop(boolean canTop) {
        this.canTop = canTop;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_source, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int realPosition = holder.getLayoutPosition();
        final BookSourceBean item = dataList.get(realPosition);
        if (canTop) {
            holder.topView.setVisibility(View.VISIBLE);
        } else {
            holder.topView.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(item.getBookSourceGroup())) {
            holder.cbView.setText(item.getBookSourceName());
        } else {
            holder.cbView.setText(String.format("%s(%s)", item.getBookSourceName(), item.getBookSourceGroup()));
        }
        holder.cbView.setChecked(item.getEnable());
        holder.itemView.setOnClickListener((View view) -> {
            holder.cbView.setChecked(!holder.cbView.isChecked());
            item.setEnable(holder.cbView.isChecked());
            activity.saveDate(item);
            activity.upDateSelectAll();
        });
        holder.editView.setOnClickListener(view -> SourceEditActivity.startThis(activity, item));
        holder.delView.setOnClickListener(view -> delSource(realPosition, item));
        holder.topView.setOnClickListener(view -> topSource(realPosition));
    }

    private void delSource(int position, BookSourceBean bookSourceBean){
        activity.delBookSource(bookSourceBean);
        dataList.remove(position);
        notifyDataSetChanged();
        activity.saveDate(dataList);
        activity.upSearchView(dataList.size());
    }

    private void topSource(int position) {
        setAllDataList(BookSourceManager.getAll());
        BookSourceBean moveData = dataList.get(position);
        dataList.remove(position);
        notifyItemRemoved(position);
        dataList.add(0, moveData);
        notifyItemInserted(0);
        if (canTop) {
            BookSourceBean first = allDataList.get(0);
            int maxWeight = first.getWeight();
            moveData.setWeight(maxWeight + 1);
            int maxNumber = first.getSerialNumber();
            moveData.setSerialNumber(maxNumber + 1);
        }
        if (dataList.size() != allDataList.size()) {
            allDataList.remove(moveData);
            allDataList.add(0, moveData);
            activity.saveDate(allDataList);
        } else {
            activity.saveDate(dataList);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public CharSequence getSectionText(int element) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }
        String groupName = dataList.get(element % dataList.size()).getBookSourceName();
        return StringUtils.isBlank(groupName) ? "" : groupName.substring(0, 1);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbView;
        View editView;
        View delView;
        View topView;

        MyViewHolder(View itemView) {
            super(itemView);
            cbView = itemView.findViewById(R.id.cb_book_source);
            editView = itemView.findViewById(R.id.iv_edit_source);
            delView = itemView.findViewById(R.id.iv_del_source);
            topView = itemView.findViewById(R.id.iv_top_source);
        }
    }
}
