//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.view.adapter.FindKindAdapter;
import com.monke.monkeybook.widget.theme.AppCompat;
import com.monke.monkeybook.widget.refreshview.scroller.TopLinearSmoothScroller;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FindBookActivity extends MBaseActivity<FindBookContract.Presenter> implements FindBookContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView expandableList;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.progressBar)
    ContentLoadingProgressBar progressBar;

    SearchView searchView;
    private boolean hasSearchFocus;

    private FindKindAdapter adapter;

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_expandable_list_vew);
        ButterKnife.bind(this);

        initExpandableList();
    }

    @Override
    protected void initData() {
        mPresenter.initData();
    }

    private void initExpandableList() {
        LinearLayoutManager manager = new LinearLayoutManager(this) {
            @Override
            public void smoothScrollToPosition(RecyclerView view, RecyclerView.State state, int position) {
                TopLinearSmoothScroller scroller = new TopLinearSmoothScroller(view.getContext());
                scroller.setTargetPosition(position);
                startSmoothScroll(scroller);
            }
        };
        expandableList.setLayoutManager(manager);
        expandableList.setHasFixedSize(true);
        expandableList.setItemViewCacheSize(20);
        expandableList.setDrawingCacheEnabled(true);
        expandableList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        adapter = new FindKindAdapter(this, autoExpandGroup());
        expandableList.setAdapter(adapter);
        tvEmpty.setText(R.string.find_empty);
        tvEmpty.setVisibility(View.GONE);

        //  设置子选项点击监听事件
        adapter.setOnChildItemClickListener(kindBean -> {
            Intent intent = new Intent(this, ChoiceBookActivity.class);
            intent.putExtra("url", kindBean.getKindUrl());
            intent.putExtra("title", kindBean.getKindName());
            intent.putExtra("tag", kindBean.getTag());
            startActivity(intent);
        });

        adapter.setOnGroupItemClickListener(groupBean -> {
            BookSourceBean sourceBean = BookSourceManager.getByUrl(groupBean.getTag());
            if (sourceBean != null) {
                SourceEditActivity.startThis(FindBookActivity.this, sourceBean);
            }
        });

    }

    private boolean autoExpandGroup() {
        return getPreferences().getBoolean(getString(R.string.pk_find_expand_group), false);
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.find_on_www);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        searchView.setFocusableInTouchMode(true);
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.search));
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            hasSearchFocus = hasFocus;
            if (!hasFocus) {
                searchView.onActionViewCollapsed();
            }
        });
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasSearchFocus) {
            searchView.clearFocus();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void updateUI(List<FindKindGroupBean> group) {
        if (group.size() > 0) {
            tvEmpty.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }
        adapter.resetDataS(group);
    }

    @Override
    public void showProgress() {
        progressBar.show();
    }

    @Override
    public void hideProgress() {
        progressBar.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SourceEditActivity.EDIT_SOURCE && resultCode == RESULT_OK) {
            initData();
        }
    }

}