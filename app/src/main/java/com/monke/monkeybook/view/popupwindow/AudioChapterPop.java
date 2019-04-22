package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioChapterPop extends PopupWindow {

    @BindView(R.id.iv_chapter_sort)
    View btnSort;
    @BindView(R.id.recycler_view)
    RecyclerView rvList;
    @BindView(R.id.btn_close)
    View btnClose;

    private ChapterListAdapter adapter;
    private Context context;

    private LinearLayoutManager linearLayoutManager;

    private boolean isChapterReverse;
    private SharedPreferences preferences;

    private OnChapterSelectListener listener;


    public AudioChapterPop(Context context, OnChapterSelectListener listener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(context, 550));
        this.context = context;
        this.listener = listener;

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowslide);

        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_audio_chapter, null);
        setContentView(contentView);
        ButterKnife.bind(this, contentView);

        init();
    }

    private void init() {
        adapter = new ChapterListAdapter(context);

        preferences = AppConfigHelper.get().getPreferences();
        isChapterReverse = preferences.getBoolean("isAudioChapterReverse", false);
        rvList.setLayoutManager(linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, isChapterReverse));
        rvList.setHasFixedSize(true);
        rvList.setAdapter(adapter);

        btnSort.setOnClickListener(v -> {
            isChapterReverse = !isChapterReverse;
            preferences.edit().putBoolean("isAudioChapterReverse", isChapterReverse).apply();
            linearLayoutManager.setReverseLayout(isChapterReverse);
            linearLayoutManager.scrollToPositionWithOffset(adapter.getIndex(), 0);
        });

        btnClose.setOnClickListener(v -> dismiss());

        adapter.setOnItemClickListener(new BaseChapterListAdapter.OnItemClickListener<ChapterBean>() {
            @Override
            public void itemClick(ChapterBean item) {
                if(listener != null){
                    listener.onSelected(item);
                }
                dismiss();
            }
        });
    }

    public void upIndex(int index){
        if(adapter != null){
            adapter.upChapterIndex(index);
        }
    }

    public void setDataSet(List<ChapterBean> chapterBeans){
        if(adapter != null){
            adapter.setDataList(chapterBeans);
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        linearLayoutManager.scrollToPositionWithOffset(adapter.getIndex(), 0);
    }

    public interface OnChapterSelectListener {
        void onSelected(ChapterBean chapterBean);
    }
}
