package com.monke.monkeybook.model;

import android.content.Context;
import android.text.TextUtils;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.model.source.My716;
import com.monke.monkeybook.model.task.SearchTaskImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/16.
 * 搜索
 */

public class SearchBookModel implements ISearchTask.OnSearchingListener {
    private int threadsNum;
    private int searchPageCount;
    private SearchListener searchListener;
    private boolean useMy716;
    private boolean searchEngineChanged = false;

    private ExecutorService executor;
    private Scheduler scheduler;

    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final List<ISearchTask> searchTasks = new ArrayList<>();

    private SearchIterator searchIterator;

    public SearchBookModel(Context context, boolean useMy716, SearchListener searchListener) {
        this.useMy716 = useMy716;
        this.searchListener = searchListener;
        AppConfigHelper helper = AppConfigHelper.get(context);
        threadsNum = helper.getInt(context.getString(R.string.pk_threads_num), 6);
        searchPageCount = helper.getInt(context.getString(R.string.pk_search_page_count), 1);
        executor = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executor);
        initSearchEngineS();
    }

    /**
     * 搜索引擎初始化
     */
    private void initSearchEngineS() {
        if (!searchEngineS.isEmpty()) {
            searchEngineS.clear();
        }

        if (useMy716 && Objects.equals(ACache.get(MApplication.getInstance()).getAsString("getZfbHb"), "True")) {
            searchEngineS.add(new SearchEngine(My716.TAG));
        }

        List<BookSourceBean> bookSourceBeans = BookSourceManager.getInstance().getSelectedBookSource();
        if (bookSourceBeans != null) {
            SearchEngine searchEngine;
            for (BookSourceBean bookSourceBean : bookSourceBeans) {
                searchEngine = new SearchEngine(bookSourceBean.getBookSourceUrl());
                searchEngineS.add(searchEngine);
            }
        }
        searchEngineChanged = false;
    }

    public void startSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }

        if (searchEngineChanged || searchEngineS.isEmpty()) {
            initSearchEngineS();
        } else {
            for (SearchEngine searchEngine : searchEngineS) {
                searchEngine.searchReset();
            }
        }

        if (searchEngineS.isEmpty()) {
            searchListener.searchSourceEmpty();
        } else {
            searchListener.resetSearchBook();
            search(query);
        }
    }

    private void search(String query) {
        searchTasks.clear();
        searchIterator = new SearchIterator(searchEngineS, searchPageCount);

        for (int i = 0, size = Math.min(searchEngineS.size(), threadsNum); i < size; i++) {
            new SearchTaskImpl(this).startSearch(query, scheduler);
        }
    }

    public void stopSearch() {
        if (isLoading()) {
            searchListener.searchBookFinish();

            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
            }
            searchTasks.clear();
        }
    }

    public void shutdownSearch() {
        if (isLoading()) {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
            }
            searchTasks.clear();
        }
        executor.shutdown();
    }

    public void setUseMy716(boolean useMy716) {
        this.useMy716 = useMy716;
        notifySearchEngineChanged();
    }

    public void onlyOnePage() {
        searchPageCount = 1;
    }

    public void notifySearchEngineChanged() {
        searchEngineChanged = true;
    }

    public boolean isLoading() {
        return !searchTasks.isEmpty();
    }

    @Override
    public SearchEngine nextSearchEngine() {
        return searchIterator.next();
    }

    @Override
    public boolean hasNextSearchEngine() {
        return searchIterator.hasNext();
    }

    @Override
    public void moveToNextSearchEngine() {
        searchIterator.moveToNext();
    }

    @Override
    public void onSearchResult(List<SearchBookBean> searchBooks) {
        searchListener.loadMoreSearchBook(searchBooks);
    }

    @Override
    public void onSearchStart(ISearchTask searchTask) {
        if (!searchTasks.contains(searchTask)) {
            searchTasks.add(searchTask);
        }
    }

    @Override
    public void onSearchError(ISearchTask searchTask) {
        searchTasks.remove(searchTask);
        if (searchTasks.size() == 0) {
            searchListener.searchBookError();
        }
    }

    @Override
    public void onSearchComplete(ISearchTask searchTask) {
        searchTasks.remove(searchTask);
        if (searchTasks.size() == 0) {
            searchListener.searchBookFinish();
        }
    }

    public interface SearchListener {
        void searchSourceEmpty();

        void resetSearchBook();

        void searchBookFinish();

        void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList);

        void searchBookError();
    }

    private class SearchIterator implements Iterator<SearchEngine> {

        final List<SearchEngine> searchEngines;
        final int limit;
        int cycleIndex;
        int cursor;

        SearchIterator(List<SearchEngine> searchEngines, int cycleIndex) {
            this.searchEngines = searchEngines;
            this.limit = searchEngines == null ? 0 : searchEngines.size();
            this.cycleIndex = cycleIndex;
            this.cursor = 0;
        }

        @Override
        public boolean hasNext() {
            if (limit == 0) {
                return false;
            }

            if (cursor < limit) {
                return true;
            } else if (cycleIndex > 1) {
                cycleIndex--;
                cursor = 0;
                return true;
            }
            return false;
        }

        @Override
        public SearchEngine next() {
            int i = cursor;
            if (i >= limit)
                return null;
            cursor = i + 1;
            return searchEngines.get(i);
        }

        void moveToNext() {
            if (cursor < limit - 1) {
                cursor++;
            }
        }
    }
}
