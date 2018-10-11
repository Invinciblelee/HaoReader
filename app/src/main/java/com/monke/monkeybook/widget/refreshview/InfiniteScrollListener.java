package com.monke.monkeybook.widget.refreshview;

import android.support.v7.widget.RecyclerView;

public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).canLoadMore()
                && !((RefreshRecyclerViewAdapter) recyclerView.getAdapter()).getLoadMoreError()
                && isSlideToBottom(recyclerView)
                && dy > dx && dy > 0) {
            onLoadMore();
        }
    }

    public abstract void onLoadMore();


    private static boolean isSlideToBottom(RecyclerView recyclerView) {
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                >= recyclerView.computeVerticalScrollRange();
    }

}