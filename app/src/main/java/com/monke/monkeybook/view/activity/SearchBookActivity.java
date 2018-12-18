//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
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
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.presenter.SearchBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.SearchBookContract;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;
import com.monke.monkeybook.widget.AppCompat;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tyrantgit.explosionfield.ExplosionField;

public class SearchBookActivity extends MBaseActivity<SearchBookContract.Presenter> implements SearchBookContract.View {

    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_search_history)
    View llSearchHistory;
    @BindView(R.id.tv_search_history_clean)
    TextView tvSearchHistoryClean;
    @BindView(R.id.tfl_search_history)
    FlexboxLayout tflSearchHistory;
    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;
    @BindView(R.id.fabStop)
    FloatingActionButton fabStop;

    private ExplosionField explosionField;
    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    private MoDialogHUD moDialogHUD;

    private final View.OnClickListener historyItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SearchHistoryBean searchHistoryBean = (SearchHistoryBean) v.getTag();
            searchView.setQuery(searchHistoryBean.getContent(), true);
        }
    };

    private final View.OnLongClickListener historyItemLongClick = v -> {
        SearchHistoryBean searchHistoryBean = (SearchHistoryBean) v.getTag();
        explosionField.explode(v);
        mPresenter.cleanSearchHistory(searchHistoryBean);
        return false;
    };

    private final Handler handler = new Handler();

    private final Runnable searchTask = new Runnable() {
        @Override
        public void run() {
            String query = searchView.getQuery().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                openOrCloseHistory(false);
                searchView.clearFocus();
                mPresenter.insertSearchHistory();
                rfRvSearchBooks.startRefresh();
                //执行搜索请求
                mPresenter.toSearchBooks(query);
            }
        }
    };

    public static void startByKey(MBaseActivity activity, String searchKey) {
        Intent intent = new Intent(activity, SearchBookActivity.class);
        intent.putExtra("searchKey", searchKey);
        activity.startActivityByAnim(intent, R.anim.anim_alpha_in, R.anim.anim_alpha_out);
    }

    @Override
    protected SearchBookContract.Presenter initInjector() {
        return new SearchBookPresenterImpl(this);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_search_book);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPresenter.fromIntentSearch(intent);
    }

    @Override
    protected void initData() {
        explosionField = ExplosionField.attach2Window(this);
        searchBookAdapter = new SearchBookAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initSearchView();

        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        rfRvSearchBooks.getRecyclerView().setPadding(0, padding, 0, padding);
        rfRvSearchBooks.getRecyclerView().setItemAnimator(null);
        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));
        rfRvSearchBooks.setEnabled(false);

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

        fabStop.setOnClickListener(view -> {
            mPresenter.stopSearch();
            fabStop.hide();
        });

        searchBookAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (searchBookAdapter.getICount() > 0
                        && rfRvSearchBooks.getNoDataView().isShown()) {
                    rfRvSearchBooks.getNoDataView().setVisibility(View.GONE);
                }
            }
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

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_source_manage:
                BookSourceActivity.startThis(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)
                && !AppActivityManager.getInstance().isExist(ReadBookActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivityByAnim(intent, R.anim.anim_alpha_in, R.anim.anim_alpha_out);
        }
        super.finishByAnim(R.anim.anim_alpha_in, R.anim.anim_alpha_out);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchView() {
        AppCompat.useCustomIconForSearchView(searchView, getString(R.string.searchBook));
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                toSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPresenter.querySearchHistory(newText);
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && TextUtils.isEmpty(searchView.getQuery())) {
                finish();
            }

            if (hasFocus) {
                mPresenter.stopSearch();
                openOrCloseHistory(true);
            }
        });
    }

    @Override
    protected void bindEvent() {
        tvSearchHistoryClean.setOnClickListener(v -> mPresenter.cleanSearchHistory());
    }

    @Override
    protected void firstRequest() {
        getWindow().getDecorView().post(() -> {
            searchView.onActionViewExpanded();
            mPresenter.fromIntentSearch(getIntent());
        });
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(null, false);
            searchView.clearFocus();
            mPresenter.stopSearch();
            openOrCloseHistory(true);
        } else {
            finish();
        }
    }

    @Override
    public void searchBook(String searchKey) {
        if (!TextUtils.isEmpty(searchKey)) {
            searchView.setQuery(searchKey, true);
        } else {
            mPresenter.querySearchHistory("");
            openOrCloseHistory(true);
        }
    }

    /**
     * 开始搜索
     */
    private void toSearch() {
        handler.removeCallbacks(searchTask);
        handler.post(searchTask);
    }

    private void openOrCloseHistory(boolean open) {
        if (open) {
            if (!llSearchHistory.isShown()) {
                llSearchHistory.setVisibility(View.VISIBLE);
            }
        } else {
            if (llSearchHistory.isShown()) {
                llSearchHistory.setVisibility(View.GONE);
            }
            searchBookAdapter.clearAll();
            rfRvSearchBooks.getRecyclerView().scrollTo(0, 0);
        }
    }

    private void addNewHistories(List<SearchHistoryBean> historyBeans) {
        tflSearchHistory.removeAllViews();
        if (historyBeans != null) {
            TextView tagView;
            for (SearchHistoryBean searchHistoryBean : historyBeans) {
                tagView = (TextView) getLayoutInflater().inflate(R.layout.item_search_history, tflSearchHistory, false);
                tagView.setTag(searchHistoryBean);
                tagView.setText(searchHistoryBean.getContent());
                tagView.setOnClickListener(historyItemClick);
                tagView.setOnLongClickListener(historyItemLongClick);
                tflSearchHistory.addView(tagView);
            }
        }
    }

    @Override
    public void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean) {
        //搜索历史插入或者修改成功
        mPresenter.querySearchHistory(searchView.getQuery().toString().trim());
    }

    @Override
    public void querySearchHistorySuccess(List<SearchHistoryBean> datas) {
        addNewHistories(datas);

        if (tflSearchHistory.getChildCount() > 0) {
            tvSearchHistoryClean.setVisibility(View.VISIBLE);
        } else {
            tvSearchHistoryClean.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void resetSearchBook() {
        fabStop.postDelayed(() -> {
            if (fabStop.isOrWillBeHidden()) {
                fabStop.show();
            }
        }, 200L);
        searchBookAdapter.clearAll();
    }

    @Override
    public void refreshFinish() {
        if (fabStop.isOrWillBeShown()) {
            fabStop.hide();
        }
        rfRvSearchBooks.finishRefresh(true, false);
    }


    @Override
    public void searchBookError() {
        if (fabStop.isOrWillBeShown()) {
            fabStop.hide();
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
    public String getEdtContent() {
        return mSearchAutoComplete.getText().toString().trim();
    }

    @Override
    public void showBookSourceEmptyTip() {
        refreshFinish();

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
