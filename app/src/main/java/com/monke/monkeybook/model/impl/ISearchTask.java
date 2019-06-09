package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;

import java.util.List;

import io.reactivex.Scheduler;

public interface ISearchTask {

    void startSearch(String query, Scheduler scheduler);

    void stopSearch();

    interface OnSearchingListener {
        SearchEngine nextSearchEngine();

        boolean hasNextSearchEngine();

        void onSearchStart(ISearchTask searchTask);

        void onSearchResult(List<SearchBookBean> searchBooks);

        void onSearchError(ISearchTask searchTask);

        void onSearchComplete(ISearchTask searchTask);
    }

}
