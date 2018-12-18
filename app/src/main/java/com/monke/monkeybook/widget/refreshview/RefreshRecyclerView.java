package com.monke.monkeybook.widget.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RefreshRecyclerView extends FrameLayout {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.ll_header)
    LinearLayout llHeader;

    private View noDataView;
    private View refreshErrorView;

    public LinearLayout getHeader() {
        return llHeader;
    }

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.view_refresh_recycler_view, this, false);
        ButterKnife.bind(this, view);
        recyclerView.setHasFixedSize(true);

        bindEvent();

        addView(view);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener refreshListener) {
        refreshLayout.setOnRefreshListener(refreshListener);
    }

    private OnLoadMoreListener loadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindEvent() {
        recyclerView.addOnScrollListener(new InfiniteScrollListener() {
            @Override
            public void onLoadMore() {
                if (null != loadMoreListener) {
                    ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).setIsRequesting(2, false);
                    loadMoreListener.startLoadMore();
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshLayout.setEnabled(enabled);
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public View getNoDataView() {
        return noDataView;
    }

    public View getRefreshErrorView() {
        return refreshErrorView;
    }

    public void refreshError() {
        refreshLayout.stopRefreshing();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof RefreshRecyclerViewAdapter) {
            ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(0, true);
        }

        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }

        if (refreshErrorView != null) {
            if(adapter != null){
                int itemCount;
                if (adapter instanceof RefreshRecyclerViewAdapter) {
                    itemCount = ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getICount();
                } else {
                    itemCount = adapter.getItemCount();
                }
                refreshErrorView.setVisibility(itemCount > 0 ? GONE : VISIBLE);
            }else {
                refreshErrorView.setVisibility(VISIBLE);
            }
        }
    }

    public void startRefresh(boolean callEvent) {
        refreshLayout.startRefreshing(callEvent);

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof RefreshRecyclerViewAdapter) {
            ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(1, true);
        }

        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(GONE);
        }
    }

    public void startRefresh() {
        startRefresh(true);
    }

    public void finishRefresh(Boolean needNoti) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            int itemCount;
            if (adapter instanceof RefreshRecyclerViewAdapter) {
                itemCount = ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getICount();
            } else {
                itemCount = adapter.getItemCount();
            }
            finishRefresh(itemCount == 0, needNoti);
        }
    }

    public void finishRefresh(Boolean isAll, Boolean needNoti) {
        refreshLayout.stopRefreshing();

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof RefreshRecyclerViewAdapter) {
            if (isAll) {
                ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(0, false);
                ((RefreshRecyclerViewAdapter) adapter).setIsAll(true, needNoti);
            } else {
                ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(0, needNoti);
            }
        }

        if (isAll) {
            if (noDataView != null) {
                if (adapter == null) {
                    noDataView.setVisibility(GONE);
                } else {
                    int itemCount;
                    if (adapter instanceof RefreshRecyclerViewAdapter) {
                        itemCount = ((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getICount();
                    } else {
                        itemCount = adapter.getItemCount();
                    }
                    noDataView.setVisibility(itemCount > 0 ? GONE : VISIBLE);
                }
            }
            if (refreshErrorView != null) {
                refreshErrorView.setVisibility(GONE);
            }
        }
    }

    public void finishLoadMore(Boolean isAll, Boolean needNoti) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof RefreshRecyclerViewAdapter) {
            if (isAll) {
                ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(0, false);
                ((RefreshRecyclerViewAdapter) adapter).setIsAll(true, needNoti);
            } else {
                ((RefreshRecyclerViewAdapter) adapter).setIsRequesting(0, needNoti);
            }
        }

        if (noDataView != null) {
            noDataView.setVisibility(GONE);
        }
        if (refreshErrorView != null) {
            refreshErrorView.setVisibility(GONE);
        }
    }

    public void setRefreshRecyclerViewAdapter(RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setRefreshRecyclerViewAdapter(View headerView, RefreshRecyclerViewAdapter refreshRecyclerViewAdapter, RecyclerView.LayoutManager layoutManager) {
        refreshRecyclerViewAdapter.setClickTryAgainListener(() -> {
            if (loadMoreListener != null)
                loadMoreListener.loadMoreErrorTryAgain();
        });
        llContent.addView(headerView, 0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(refreshRecyclerViewAdapter);
    }

    public void setItemTouchHelperCallback(ItemTouchHelper.Callback callback) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void loadMoreError() {
        refreshLayout.stopRefreshing();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof RefreshRecyclerViewAdapter) {
            ((RefreshRecyclerViewAdapter) adapter).setLoadMoreError(true, true);
        }
    }

    public void setNoDataAndrRefreshErrorView(View noData, View refreshError) {
        if (noData != null) {
            noDataView = noData;
            noDataView.setVisibility(GONE);
            addView(noDataView, getChildCount() - 1);

        }
        if (refreshError != null) {
            refreshErrorView = refreshError;
            addView(refreshErrorView, 2);
            refreshErrorView.setVisibility(GONE);
        }
    }

}