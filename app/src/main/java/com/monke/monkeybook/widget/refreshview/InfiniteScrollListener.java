package com.monke.monkeybook.widget.refreshview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (isSlideToBottom(recyclerView) && dy > dx && dy > 0) {
            onLoadMore();
        }
        
    }

    public abstract void onLoadMore();


    private static boolean isSlideToBottom(RecyclerView recyclerView) {
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                >= recyclerView.computeVerticalScrollRange();
    }

}