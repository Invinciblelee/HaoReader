//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.presenter.SearchBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.SearchBookContract;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;
import com.monke.monkeybook.view.adapter.SearchHistoryAdapter;
import com.monke.monkeybook.widget.AppCompat;
import com.monke.monkeybook.widget.flowlayout.TagFlowLayout;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import tyrantgit.explosionfield.ExplosionField;

public class SearchBookActivity extends MBaseActivity<SearchBookContract.Presenter> implements SearchBookContract.View {

    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_search_history)
    LinearLayout llSearchHistory;
    @BindView(R.id.tv_search_history_clean)
    TextView tvSearchHistoryClean;
    @BindView(R.id.tfl_search_history)
    TagFlowLayout tflSearchHistory;
    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;
    @BindView(R.id.fabScrollTop)
    FloatingActionButton fabScrollTop;

    MenuItem itemMy716;
    MenuItem itemDonate;
    private SearchHistoryAdapter searchHistoryAdapter;
    private ExplosionField explosionField;
    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean showHistory;
    private boolean useMy716;
    private boolean showStop;
    private boolean stopAutoScroll;

    private MoDialogHUD moDialogHUD;

    private Runnable fabHidden = new Runnable() {
        @Override
        public void run() {
            fabScrollTop.hide();
        }
    };

    public static void startByKey(MBaseActivity activity, String searchKey) {
        Intent intent = new Intent(activity, SearchBookActivity.class);
        intent.putExtra("searchKey", searchKey);
        activity.startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected SearchBookContract.Presenter initInjector() {
        useMy716 = !Objects.equals(ACache.get(this).getAsString("useMy716"), "False");
        return new SearchBookPresenterImpl(this, useMy716);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            showHistory = savedInstanceState.getBoolean("showHistory");
            showStop = savedInstanceState.getBoolean("showStop");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_search_book);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("showHistory", showHistory);
        savedInstanceState.putBoolean("showStop", showStop);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPresenter.fromIntentSearch(intent);
    }

    @Override
    protected void initData() {
        explosionField = ExplosionField.attach2Window(this);
        searchHistoryAdapter = new SearchHistoryAdapter();
        searchBookAdapter = new SearchBookAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initSearchView();
        fabScrollTop.hide();
        llSearchHistory.setOnClickListener(null);

        tflSearchHistory.setAdapter(searchHistoryAdapter);

        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        rfRvSearchBooks.getRecyclerView().setPadding(0, padding, 0, padding);
        rfRvSearchBooks.getRecyclerView().setItemAnimator(null);
        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));

        View viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            rfRvSearchBooks.startRefresh();
            mPresenter.toSearchBooks(null);
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        searchBookAdapter.setItemClickListener((animView, position, searchBookBean) -> {
            BookDetailActivity.startThis(SearchBookActivity.this, searchBookBean);
        });

        fabScrollTop.setOnClickListener(view -> {
            rfRvSearchBooks.getRecyclerView().scrollToPosition(0);
            fabScrollTop.hide();
            stopAutoScroll = false;
        });
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.action_search);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        itemMy716 = menu.findItem(R.id.action_my716);
        itemDonate = menu.findItem(R.id.action_donate);
        upMenu();

        MenuItem stop = menu.findItem(R.id.action_stop);
        Drawable drawable = getResources().getDrawable(!showStop ?
                R.drawable.ic_submit_black_24dp : R.drawable.ic_stop_black_24dp_new);
        drawable.mutate();
        drawable.setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
        stop.setIcon(drawable);
        stop.setTitle(!showStop ? R.string.submit : R.string.stop);
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_stop:
                if (item.getTitle().equals(getString(R.string.submit))) {
                    if (!TextUtils.isEmpty(searchView.getQuery())) {
                        searchView.clearFocus();
                        toSearch();
                    }
                } else {
                    mPresenter.stopSearch(true);
                }
                break;
            case R.id.action_book_source_manage:
                BookSourceActivity.startThis(this);
                break;
            case R.id.action_my716:
                useMy716 = !useMy716;
                itemMy716.setChecked(useMy716);
                mPresenter.setUseMy716(useMy716);
                ACache.get(this).put("useMy716", useMy716 ? "True" : "False");
                break;
            case R.id.action_donate:
                DonateActivity.startThis(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更新菜单
     */
    @Override
    public void upMenu() {
        if (itemMy716 != null) {
            itemMy716.setChecked(useMy716);
            if (Objects.equals(ACache.get(this).getAsString("getZfbHb"), "True")) {
                itemMy716.setVisible(true);
                itemDonate.setVisible(false);
            } else {
                itemMy716.setVisible(false);
                itemDonate.setVisible(true);
            }
        }
    }

    @Override
    public void finish() {
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)
                && !AppActivityManager.getInstance().isExist(ReadBookActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            super.finishNoAnim();
        } else {
            super.finishByAnim(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void initSearchView() {
        AppCompat.useCustomIconForSearchView(searchView, getString(R.string.searchBook));
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                toSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPresenter.querySearchHistory(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((view, b) -> {
            showHistory = b;
            if (showHistory) {
                mPresenter.stopSearch(true);
                openOrCloseHistory(showHistory);
            }
        });
    }

    @Override
    protected void bindEvent() {
        tvSearchHistoryClean.setOnClickListener(v -> {
            for (int i = 0; i < tflSearchHistory.getChildCount(); i++) {
                explosionField.explode(tflSearchHistory.getChildAt(i));
            }
            mPresenter.cleanSearchHistory();
        });

        searchHistoryAdapter.setOnItemClickListener(new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void itemClick(SearchHistoryBean searchHistoryBean) {
                searchBookAdapter.clearAll();
                searchView.clearFocus();
                searchView.setQuery(searchHistoryBean.getContent(), true);
                openOrCloseHistory(false);
            }

            @Override
            public void itemLongClick(int index) {
                explosionField.explode(tflSearchHistory.getChildAt(index));
                mPresenter.cleanSearchHistory(searchHistoryAdapter.getItemData(index));
            }
        });

        rfRvSearchBooks.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !stopAutoScroll) {
                    stopAutoScroll = true;
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    stopAutoScroll = false;
                    fabScrollTop.hide();
                    fabScrollTop.removeCallbacks(fabHidden);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (recyclerView.canScrollVertically(-1) && newState != RecyclerView.SCROLL_STATE_IDLE) {
                    stopAutoScroll = true;
                    fabScrollTop.show();
                    fabScrollTop.removeCallbacks(fabHidden);
                    fabScrollTop.postDelayed(fabHidden, 2000L);
                }
            }
        });

        searchBookAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (searchBookAdapter.getSearchBooks().isEmpty()) {
                    fabScrollTop.hide();
                    fabScrollTop.removeCallbacks(fabHidden);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (!stopAutoScroll) {
                    rfRvSearchBooks.getRecyclerView().scrollToPosition(0);
                }
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        mPresenter.fromIntentSearch(getIntent());
    }

    @Override
    public void onPause() {
        super.onPause();
        showHistory = llSearchHistory.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        openOrCloseHistory(showHistory);
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(null, false);
            searchView.clearFocus();
            mPresenter.stopSearch(true);
            openOrCloseHistory(true);
        } else {
            finish();
        }
    }

    @Override
    public void searchBook(String searchKey) {
        if (!TextUtils.isEmpty(searchKey)) {
            mPresenter.stopSearch(false);
            mSearchAutoComplete.setText(searchKey);
            searchView.clearFocus();
            toSearch();
            showHistory = false;
        } else {
            showHistory = true;
            mPresenter.querySearchHistory("");
        }
        openOrCloseHistory(showHistory);
    }

    /**
     * 开始搜索
     */
    private void toSearch() {
        String query = searchView.getQuery().toString().trim();
        if (!TextUtils.isEmpty(query)) {
            mPresenter.insertSearchHistory();
            openOrCloseHistory(false);
            rfRvSearchBooks.startRefresh();
            searchBookAdapter.clearAll();
            //执行搜索请求
            mPresenter.toSearchBooks(query);
        }
    }

    private void openOrCloseHistory(Boolean open) {
        if (open) {
            if (llSearchHistory.getVisibility() != View.VISIBLE) {
                llSearchHistory.setVisibility(View.VISIBLE);
            }
            if (showStop) {
                showStop = false;
                supportInvalidateOptionsMenu();
            }
        } else {
            if (llSearchHistory.getVisibility() == View.VISIBLE) {
                llSearchHistory.setVisibility(View.GONE);
            }
        }
        if (open && fabScrollTop.isOrWillBeShown()) {
            fabScrollTop.hide();
        }
    }

    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        //搜索历史插入或者修改成功
        mPresenter.querySearchHistory(searchView.getQuery().toString().trim());
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> datas) {
        searchHistoryAdapter.replaceAll(datas);
        if (searchHistoryAdapter.getDataSize() > 0) {
            tvSearchHistoryClean.setVisibility(View.VISIBLE);
        } else {
            tvSearchHistoryClean.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void resetSearchBook() {
        if (!showStop) {
            showStop = true;
            supportInvalidateOptionsMenu();
        }
        searchBookAdapter.clearAll();
    }

    @Override
    public void refreshFinish() {
        if (showStop) {
            showStop = false;
            supportInvalidateOptionsMenu();
        }
        rfRvSearchBooks.finishRefresh(true, false);
    }


    @Override
    public void searchBookError() {
        if (showStop) {
            showStop = false;
            supportInvalidateOptionsMenu();
        }
        rfRvSearchBooks.refreshError();
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books, mSearchAutoComplete.getText().toString().trim());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        explosionField.clear();
    }

    @Override
    public EditText getEdtContent() {
        return mSearchAutoComplete;
    }

    @Override
    public SearchBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void showBookSourceEmptyTip() {
        if (moDialogHUD == null) {
            moDialogHUD = new MoDialogHUD(this);
        }

        moDialogHUD.showTwoButton("您没有选择任何书源", "去选择"
                , v -> {
                    moDialogHUD.dismiss();
                    BookSourceActivity.startThis(SearchBookActivity.this);
                }, "取消",
                v -> moDialogHUD.dismiss());
    }
}
