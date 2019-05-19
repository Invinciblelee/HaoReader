//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.SearchBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.SearchBookContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.adapter.SearchBookAdapter;
import com.monke.monkeybook.view.fragment.dialog.AlertDialog;
import com.monke.monkeybook.widget.theme.AppCompat;
import com.monke.monkeybook.widget.explosion_field.ExplosionField;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private String group;

    private ExplosionField explosionField;
    private SearchBookAdapter searchBookAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    private final Handler mHandler = new Handler();

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            fabStop.show();
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            fabStop.hide();
        }
    };

    private final Runnable mSearchRunnable = new Runnable() {
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

    private final View.OnClickListener historyItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SearchHistoryBean searchHistoryBean = (SearchHistoryBean) v.getTag();
            searchView.setQuery(searchHistoryBean.getContent(), true);
        }
    };

    private final View.OnLongClickListener historyItemLongClick = v -> {
        explosionField.explode(v);
        SearchHistoryBean searchHistoryBean = (SearchHistoryBean) v.getTag();
        mPresenter.cleanSearchHistory(searchHistoryBean);
        return false;
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
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            group = savedInstanceState.getString("group");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("group", group);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPresenter.fromIntentSearch(intent);
        if (AppActivityManager.getInstance().isExist(BookDetailActivity.class)) {
            AppActivityManager.getInstance().finishActivity(BookDetailActivity.class);
        }
    }

    @Override
    protected void initData() {
        explosionField = ExplosionField.attach2Window(this);
        searchBookAdapter = new SearchBookAdapter(this, false);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);
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
            hideFabButton();
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
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
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

    private void initMenu(Menu menu) {
        if (menu == null) return;
        MenuItem item = menu.findItem(R.id.action_outer_source);
        SubMenu subMenu = item.getSubMenu();
        subMenu.removeGroup(R.id.book_source_group);
        if (BookSourceManager.getEnabledCount() > 0) {
            item.setVisible(true);
            item.setEnabled(true);
            MenuItem subItem = subMenu.add(R.id.book_source_group, Menu.NONE, Menu.NONE, R.string.book_all_source);
            if (group == null) {
                subItem.setChecked(true);
            }
            List<String> groupList = BookSourceManager.getEnableGroupList();
            for (String groupName : groupList) {
                subItem = subMenu.add(R.id.book_source_group, Menu.NONE, Menu.NONE, groupName);
                if (TextUtils.equals(group, subItem.getTitle())) {
                    subItem.setChecked(true);
                }
            }
            subMenu.setGroupCheckable(R.id.book_source_group, true, true);
        } else {
            item.setVisible(false);
            item.setEnabled(false);
        }
    }

    @Override
    public void upMenu() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem my716 = menu.findItem(R.id.action_book_my716);
        if (my716 != null) {
            my716.setChecked(AppConfigHelper.get().getBoolean("useMy716", true));
        }
        MenuItem shuqi = menu.findItem(R.id.action_book_shuqi);
        if (shuqi != null) {
            shuqi.setChecked(AppConfigHelper.get().getBoolean("useShuqi", true));
        }
        initMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_source_manage:
                BookSourceActivity.startThis(this);
                break;
            case R.id.action_book_my716:
                item.setChecked(!item.isChecked());
                AppConfigHelper.get().edit().putBoolean("useMy716", item.isChecked()).apply();
                mPresenter.useMy716(item.isChecked());
                break;
            case R.id.action_book_shuqi:
                item.setChecked(!item.isChecked());
                AppConfigHelper.get().edit().putBoolean("useShuqi", item.isChecked()).apply();
                mPresenter.useShuqi(item.isChecked());
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                if (item.getGroupId() == R.id.book_source_group) {
                    item.setChecked(true);
                    if (Objects.equals(getString(R.string.book_all_source), item.getTitle().toString())) {
                        group = null;
                        mPresenter.initSearchEngineS(null);
                    } else {
                        group = item.getTitle().toString();
                        mPresenter.initSearchEngineS(item.getTitle().toString());
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (AppActivityManager.getInstance().indexOf(this) == 0
                && AppActivityManager.getInstance().size() == 1) {
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
        tvSearchHistoryClean.setOnClickListener(v -> {
            explosionField.explode(tflSearchHistory);
            mPresenter.cleanSearchHistory();
        });
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
        mHandler.removeCallbacks(mSearchRunnable);
        mHandler.post(mSearchRunnable);
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
                tagView.setBackgroundResource(R.drawable.selector_history_tag);
                tagView.setTag(searchHistoryBean);
                tagView.setText(searchHistoryBean.getContent());
                tagView.setOnClickListener(historyItemClick);
                tagView.setOnLongClickListener(historyItemLongClick);
                tflSearchHistory.addView(tagView);
            }
        }
    }

    private void showFabButton() {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(mShowRunnable);
        mHandler.postDelayed(mShowRunnable, 200L);
    }

    private void hideFabButton() {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(mShowRunnable);
        mHandler.post(mHideRunnable);
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
        showFabButton();
        searchBookAdapter.clearAll();
    }

    @Override
    public void refreshFinish() {
        hideFabButton();
        rfRvSearchBooks.finishRefresh(true, false);
    }


    @Override
    public void searchBookError() {
        hideFabButton();
        if (!NetworkUtil.isNetworkAvailable()) {
            rfRvSearchBooks.refreshError("网络不可用");
        } else {
            rfRvSearchBooks.refreshError();
        }
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books, getEdtContent());
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

        new AlertDialog.Builder(getSupportFragmentManager())
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.tip_source_not_selected)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.goto_select, (dialog, which) -> BookSourceActivity.startThis(SearchBookActivity.this))
                .show();
    }


}
