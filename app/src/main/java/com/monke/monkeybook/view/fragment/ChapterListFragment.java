package com.monke.monkeybook.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.ChapterHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.Locale;

import androidx.annotation.Nullable;
import butterknife.BindView;

public class ChapterListFragment extends BaseChapterListFragment<ChapterListAdapter> {

    @BindView(R.id.tv_current_chapter_info)
    TextView tvChapterInfo;
    @BindView(R.id.iv_chapter_sort)
    ImageView ivChapterSort;
    @BindView(R.id.ll_chapter_base_info)
    View llBaseInfo;

    private boolean isChapterReverse;

    private SharedPreferences preferences;


    public static ChapterListFragment newInstance() {

        Bundle args = new Bundle();

        ChapterListFragment fragment = new ChapterListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);

        preferences = AppConfigHelper.get().getPreferences();
        isChapterReverse = preferences.getBoolean("isChapterReverse", false);
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_chapter_list;
    }

    @Override
    public ChapterListAdapter createAdapter() {
        return new ChapterListAdapter(getContext());
    }

    @Override
    public boolean reverseLayout() {
        return isChapterReverse;
    }

    @Override
    public void initView() {
        ivChapterSort.setOnClickListener(v -> {
            isChapterReverse = !isChapterReverse;
            preferences.edit().putBoolean("isChapterReverse", isChapterReverse).apply();
            getLayoutManager().setReverseLayout(isChapterReverse);
            getLayoutManager().scrollToPositionWithOffset(bookShelf.getDurChapter(), 0);
        });

        getAdapter().setOnItemClickListener(new BaseChapterListAdapter.OnItemClickListener<ChapterBean>() {

            @Override
            public void itemClick(ChapterBean chapterBean) {
                RxBus.get().post(RxBusTag.OPEN_CHAPTER, chapterBean);
            }

        });

    }

    @Override
    public void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    private void updateIndex() {
        final int durChapter = bookShelf.getDurChapter();
        getAdapter().upChapterIndex(durChapter);
        getLayoutManager().scrollToPositionWithOffset(durChapter, 0);
    }

    private void updateChapterInfo() {
        if (bookShelf != null) {
            String durChapterName = ChapterHelp.getFormatChapterName(bookShelf.getDurChapterName());
            if (getAdapter().getItemCount() == 0) {
                tvChapterInfo.setText(durChapterName);
            } else {
                tvChapterInfo.setText(String.format(Locale.getDefault(), "%s (%d/%d章)", durChapterName, bookShelf.getDurChapter() + 1, bookShelf.getChapterListSize()));
            }
        }
    }

    void scrollToTarget() {
        if (bookShelf != null && getLayoutManager() != null) {
            final int durChapter = bookShelf.getDurChapter();
            rvList.post(() -> getLayoutManager().scrollToPositionWithOffset(durChapter, 0));
        }
    }

    @Override
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        getAdapter().setBook(bookShelfBean);

        rvList.post(() -> {
            updateIndex();
            updateChapterInfo();
        });
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHAPTER_CHANGE)})
    public void chapterChange(BookContentBean bookContentBean) {
        if (bookShelf != null && bookShelf.getNoteUrl().equals(bookContentBean.getNoteUrl())) {
            getAdapter().upChapter(bookContentBean.getDurChapterIndex());
        }
    }
}
