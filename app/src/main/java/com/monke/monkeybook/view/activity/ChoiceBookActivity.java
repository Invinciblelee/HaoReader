//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.ChoiceBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.adapter.ChoiceBookAdapter;
import com.monke.monkeybook.widget.AppCompat;
import com.monke.monkeybook.widget.refreshview.OnLoadMoreListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChoiceBookActivity extends MBaseActivity<ChoiceBookContract.Presenter> implements ChoiceBookContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;

    private ChoiceBookAdapter searchBookAdapter;

    @Override
    protected ChoiceBookContract.Presenter initInjector() {
        return new ChoiceBookPresenterImpl(getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_choice);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void initData() {
        searchBookAdapter = new ChoiceBookAdapter(this);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));
        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        rfRvSearchBooks.getRecyclerView().setClipToPadding(false);
        rfRvSearchBooks.getRecyclerView().setPadding(0, padding, 0, padding);

        View viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            searchBookAdapter.replaceAll(null);
            rfRvSearchBooks.startRefresh();
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorToolBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mPresenter.getTitle());
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEvent() {
        searchBookAdapter.setItemClickListener(new ChoiceBookAdapter.OnItemClickListener() {
            @Override
            public void clickToSearch(View clickView, int position, SearchBookBean searchBookBean) {
                SearchBookActivity.startByKey(ChoiceBookActivity.this, searchBookBean.getName());
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                BookDetailActivity.startThis(ChoiceBookActivity.this, searchBookBean);
            }
        });

        rfRvSearchBooks.setOnRefreshListener(() -> {
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
        });

        rfRvSearchBooks.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                mPresenter.toSearchBooks(null);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toSearchBooks(null);
            }
        });
    }

    @Override
    public void refreshSearchBook(List<SearchBookBean> books) {
        searchBookAdapter.replaceAll(books);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        if (books.isEmpty()) {
            loadMoreFinish(true);
            return;
        }
        searchBookAdapter.addAll(books);
        loadMoreFinish(false);
    }

    @Override
    public void searchBookError() {
        if (mPresenter.getPage() > 1) {
            rfRvSearchBooks.loadMoreError();
        } else {
            //刷新失败
            if (!NetworkUtil.isNetworkAvailable()) {
                rfRvSearchBooks.refreshError("网络不可用");
            } else {
                rfRvSearchBooks.refreshError();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void addBookShelfFailed(String massage) {
        toast(massage);
    }

    @Override
    public ChoiceBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    protected void firstRequest() {
        rfRvSearchBooks.startRefresh();
    }
}