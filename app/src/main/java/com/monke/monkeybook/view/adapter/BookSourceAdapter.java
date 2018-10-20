package com.monke.monkeybook.view.adapter;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.view.activity.BookSourceActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> dataList;
    private List<BookSourceBean> allDataList;
    private BookSourceActivity activity;
    private int index;

    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

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
        this.dataList = bookSourceBeanList;
        notifyDataSetChanged();
        activity.upDateSelectAll();
        activity.upSearchView(dataList.size());
        activity.upGroupMenu();
    }

    private void allDataList(List<BookSourceBean> bookSourceBeanList) {
        this.allDataList = bookSourceBeanList;
        notifyDataSetChanged();
        activity.upDateSelectAll();
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
        if (TextUtils.isEmpty(item.getBookSourceGroup())) {
            holder.cbView.setText(item.getBookSourceName());
        } else {
            holder.cbView.setText(String.format("%s(%s)", item.getBookSourceName(), item.getBookSourceGroup()));
        }
        holder.cbView.setChecked(item.getEnable());
        holder.cbView.setOnClickListener((View view) -> {
            item.setEnable(holder.cbView.isChecked());
            activity.saveDate(item);
            activity.upDateSelectAll();
        });
        holder.editView.getDrawable().mutate();
        holder.editView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.editView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, SourceEditActivity.class);
            String key = String.valueOf(System.currentTimeMillis());
            intent.putExtra("data_key", key);
            BitIntentDataManager.getInstance().putData(key, item.clone());
            activity.startActivityForResult(intent, BookSourceActivity.EDIT_SOURCE);
        });
        holder.delView.getDrawable().mutate();
        holder.delView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.delView.setOnClickListener(view -> {
            activity.delBookSource(item);
            dataList.remove(realPosition);
            notifyDataSetChanged();
            activity.saveDate(dataList);
            activity.upSearchView(dataList.size());
        });
        holder.topView.getDrawable().mutate();
        holder.topView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        holder.topView.setOnClickListener(view -> {
            allDataList(BookSourceManage.getAllBookSource());

            BookSourceBean moveData = dataList.get(realPosition);
            int maxWeight = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .orderDesc(BookSourceBeanDao.Properties.Weight).limit(1).unique().getWeight();
            moveData.setWeight(maxWeight + 1);
            BookshelfHelp.saveBookSource(moveData);

            dataList.remove(realPosition);
            notifyItemInserted(0);
            dataList.add(0, item);
            notifyItemRemoved(realPosition + 1);

            if (dataList.size() != allDataList.size()){
                for (int i = 0;i < allDataList.size();i++){
                    if (item.equals(allDataList.get(i))){
                        index = i;
                        break;
                    }
                }
                BookSourceBean moveDataA = allDataList.get(index);
                allDataList.remove(index);
                notifyItemInserted(0);
                allDataList.add(0,moveDataA);
                notifyItemRemoved(index + 1);
            }
            notifyDataSetChanged();
            activity.saveDate(dataList);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbView;
        ImageView editView;
        ImageView delView;
        ImageView topView;

        MyViewHolder(View itemView) {
            super(itemView);
            cbView = itemView.findViewById(R.id.cb_book_source);
            editView = itemView.findViewById(R.id.iv_edit_source);
            delView = itemView.findViewById(R.id.iv_del_source);
            topView = itemView.findViewById(R.id.iv_top_source);
        }
    }
}
