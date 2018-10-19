package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.view.adapter.ChapterListAdapter;
import com.monke.monkeybook.widget.refreshview.scroller.FastScrollRecyclerView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChapterListView extends ScrimInsetsFrameLayout {
    @BindView(R.id.rv_list)
    FastScrollRecyclerView rvList;
    @BindView(R.id.toolbar_tab)
    TabLayout toolbarTab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_chapter_info)
    TextView tvInfo;
    @BindView(R.id.tv_chapter_current)
    TextView tvCurrent;
    @BindView(R.id.v_line)
    View lineView;
    @BindView(R.id.ll_chapter_info)
    View chapterInfoView;
    @BindView(R.id.ll_chapter_list_update)
    View updateView;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;

    SearchView searchView;

    private ChapterListAdapter chapterListAdapter;
    private OnItemClickListener itemClickListener;
    private BookShelfBean bookShelfBean;

    private OnFocusChangedListener focusChangedListener;

    private Animation animUp;
    private OnUpdateListener updateListener;

    private boolean hasInsetsChanged = true;

    public ChapterListView(@NonNull Context context) {
        this(context, null);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterListView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnUpdateListener(OnUpdateListener listener) {
        this.updateListener = listener;
    }

    public void setOnFocusChangedListener(OnFocusChangedListener focusChangedListener) {
        this.focusChangedListener = focusChangedListener;
    }

    public void setHasInsetsChanged(boolean hasChanged) {
        this.hasInsetsChanged = hasChanged;
    }

    @Override
    public void applyWindowInsets(Rect insets) {
        if (hasInsetsChanged) {
            super.applyWindowInsets(insets);
            hasInsetsChanged = false;
        }
    }

    private void init() {
        setVisibility(INVISIBLE);
        LayoutInflater.from(getContext()).inflate(R.layout.view_chapterlist, this, true);
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true));
        rvList.setItemAnimator(null);
        toolbarTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showCurrentInfo(tab.getPosition() == 0);
                if (chapterListAdapter != null) {
                    chapterListAdapter.tabChange(tab.getPosition());
                    if (tab.getPosition() == 0) {
                        upIndex(bookShelfBean.getDurChapter());
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (chapterListAdapter != null) {
                    chapterListAdapter.tabChange(tab.getPosition());
                    if (tab.getPosition() == 0) {
                        upIndex(bookShelfBean.getDurChapter());
                    }
                }
            }
        });
        assert toolbar.getNavigationIcon() != null;
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.menu_color_default));
        toolbar.inflateMenu(R.menu.menu_search_view);
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.search));
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        searchView.setOnCloseListener(() -> {
            toolbarTab.setVisibility(VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(view -> toolbarTab.setVisibility(GONE));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chapterListAdapter.search(newText);
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (focusChangedListener != null) {
                focusChangedListener.onFocusChanged(hasFocus);
            }
        });

        toolbar.setNavigationOnClickListener(view -> dismiss());

        updateView.setEnabled(false);
        updateView.setOnClickListener(v -> {
            if (updateListener != null) {
                updateStart();
                updateListener.onUpdate();
            }
        });
    }


    /**
     * 显示章节列表，并定位当前阅读章节
     */
    public void show(int durChapter) {
        ViewParent parent = getParent();
        if (parent instanceof DrawerLayout && !((DrawerLayout) parent).isDrawerOpen(GravityCompat.START)) {
            ((DrawerLayout) parent).openDrawer(GravityCompat.START);
        }

        upIndex(durChapter);
        scrollToPosition(durChapter);
    }

    private void upIndex(int durChapter) {
        updateCurrentInfo(durChapter, bookShelfBean.getChapterListSize());
        if (toolbarTab.getSelectedTabPosition() == 0) {
            chapterListAdapter.setIndex(durChapter);
        } else {
            chapterListAdapter.notifyDataSetChanged();
        }
    }

    private void updateCurrentInfo(int durIndex, int total) {
        if (bookShelfBean != null) {
            updateView.setEnabled(true);
            tvInfo.setText(bookShelfBean.getChapter(durIndex).getDurChapterName());
            tvCurrent.setText(String.format(Locale.getDefault(), "(%d/%d)", total == 0 ? 0 : durIndex + 1, total));
        }
    }

    private void showCurrentInfo(boolean show) {
        if (show) {
            lineView.setVisibility(View.VISIBLE);
            chapterInfoView.setVisibility(View.VISIBLE);
        } else {
            lineView.setVisibility(View.GONE);
            chapterInfoView.setVisibility(View.GONE);
        }
    }

    public Boolean hasData() {
        return bookShelfBean != null;
    }

    public void setData(BookShelfBean bookShelfBean, OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
        this.bookShelfBean = bookShelfBean;
        if (this.bookShelfBean.getTag().equals(BookShelfBean.LOCAL_TAG)) {
            this.updateView.setVisibility(View.GONE);
        }
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, new OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                if (itemClickListener != null) {
                    if (tabPosition == 0) {
                        searchViewCollapsed();
                        dismiss();
                        if (index != bookShelfBean.getDurChapter()) {
                            postDelayed(() -> itemClickListener.itemClick(index, page, tabPosition), 300L);
                        }
                    }
                }
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                if (itemClickListener != null && tabPosition == 1) {
                    dismiss();
                    itemClickListener.itemLongClick(bookmarkBean, tabPosition);
                }
            }
        });
        rvList.setAdapter(chapterListAdapter);
        updateCurrentInfo(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize());
    }

    public void upChapterList(BookShelfBean bookShelfBean) {
        updateFinish();
        if (chapterListAdapter != null && bookShelfBean != null) {
            this.bookShelfBean = bookShelfBean;
            updateCurrentInfo(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize());
            chapterListAdapter.upChapterList(bookShelfBean);
            scrollToPosition(bookShelfBean.getDurChapter());
        }
    }

    public void upChapter(int chapterIndex) {
        if (chapterListAdapter != null) {
            chapterListAdapter.upChapter(chapterIndex);
        }
    }

    public void updateStart() {
        updateView.setEnabled(false);
        if (animUp == null) {
            animUp = new RotateAnimation(0f, 359f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animUp.setFillAfter(true);
            animUp.setInterpolator(new LinearInterpolator());
            animUp.setRepeatMode(Animation.RESTART);
            animUp.setRepeatCount(Integer.MAX_VALUE);
            animUp.setDuration(2000L);
        }
        ivRefresh.startAnimation(animUp);
    }

    public void updateFinish() {
        updateView.setEnabled(true);
        if (animUp != null) {
            animUp.cancel();
        }
    }

    private void scrollToPosition(int position) {
        RecyclerView.LayoutManager layoutManager = rvList.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
        }
    }

    private void searchViewCollapsed() {
        searchView.onActionViewCollapsed();
        toolbarTab.setVisibility(VISIBLE);
    }

    public void dismiss() {
        if (toolbarTab.getVisibility() != VISIBLE) {
            searchViewCollapsed();
        } else {
            ViewParent parent = getParent();
            if (parent instanceof DrawerLayout) {
                ((DrawerLayout) parent).closeDrawer(GravityCompat.START);
            }
        }
    }

    public interface OnFocusChangedListener {
        void onFocusChanged(boolean hasFocus);
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page, int tabPosition);

        void itemLongClick(BookmarkBean bookmarkBean, int tabPosition);
    }

    public interface OnUpdateListener {
        void onUpdate();
    }
}