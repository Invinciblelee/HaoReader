package com.monke.monkeybook.model;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.model.content.Default716;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.model.task.SearchTaskImpl;
import com.monke.monkeybook.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private String searchBookType;
    private SearchListener searchListener;
    private boolean searchEngineChanged = false;
    private boolean useMy716 = true;
    private boolean hasData = false;

    private ExecutorService executor;

    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final List<ISearchTask> searchTasks = new ArrayList<>();

    private final SearchHandler searchHandler;

    private SearchIterator searchIterator;

    private static class SearchHandler extends Handler {

        private static final int MSG_SEARCH = 1;
        private static final int MSG_QUERY = 2;

        private SearchBookModel model;
        private Scheduler scheduler;

        private SearchHandler(SearchBookModel model, Scheduler scheduler) {
            this.model = model;
            this.scheduler = scheduler;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SEARCH) {
                model.search((String) msg.obj);
            } else if (msg.what == MSG_QUERY) {
                new SearchTaskImpl(model).startSearch((String) msg.obj, scheduler);
            }
        }
    }

    public SearchBookModel(Context context) {
        AppConfigHelper configHelper = AppConfigHelper.get();
        threadsNum = Math.max(1, configHelper.getInt(context.getString(R.string.pk_threads_num), 6));
        searchPageCount = configHelper.getInt(context.getString(R.string.pk_search_page_count), 1);
        executor = Executors.newFixedThreadPool(threadsNum);
        searchHandler = new SearchHandler(this, Schedulers.from(executor));
    }

    /**
     * 搜索引擎初始化
     */
    private void initSearchEngineS() {
        if (!searchEngineS.isEmpty()) {
            searchEngineS.clear();
        }

        if (useMy716) {
            searchEngineS.add(new SearchEngine(Default716.TAG));
        }

        List<BookSourceBean> bookSourceBeans = BookSourceManager.getInstance().getSelectedBookSource();
        if (bookSourceBeans != null && !bookSourceBeans.isEmpty()) {
            for (BookSourceBean bookSourceBean : bookSourceBeans) {
                if (searchBookType != null && !TextUtils.equals(bookSourceBean.getBookSourceType(), searchBookType)) {
                    continue;
                }
                searchEngineS.add(new SearchEngine(bookSourceBean.getBookSourceUrl()));
            }
        }
        searchEngineChanged = false;
    }

    public void startSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }

        clearSearch();

        if (searchEngineChanged || searchEngineS.isEmpty()) {
            initSearchEngineS();
        } else {
            for (SearchEngine searchEngine : searchEngineS) {
                searchEngine.searchReset();
            }
        }

        if (!NetworkUtil.isNetworkAvailable()) {
            searchListener.searchBookError();
            return;
        }

        if (searchEngineS.isEmpty()) {
            searchListener.searchSourceEmpty();
        } else {
            searchListener.resetSearchBook();

            searchHandler.removeMessages(SearchHandler.MSG_SEARCH);
            Message msg = searchHandler.obtainMessage(SearchHandler.MSG_SEARCH, query);
            searchHandler.sendMessageDelayed(msg, 400L);
        }
    }

    private void search(String query) {
        searchIterator = new SearchIterator(searchEngineS, searchPageCount);

        searchHandler.removeMessages(SearchHandler.MSG_QUERY);
        for (int i = 0, size = Math.min(searchEngineS.size(), threadsNum); i < size; i++) {
            searchHandler.obtainMessage(SearchHandler.MSG_QUERY, query).sendToTarget();
        }
    }

    private boolean clearSearch() {
        hasData = false;
        if (isLoading()) {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
            }
            searchTasks.clear();
            return true;
        }
        return false;
    }

    public void stopSearch() {
        if (clearSearch()) {
            searchListener.searchBookFinish();
        }
    }

    public void shutdownSearch() {
        clearSearch();
        executor.shutdown();
    }

    public SearchBookModel onlyOnePage() {
        searchPageCount = 1;
        return this;
    }

    public SearchBookModel setSearchBookType(@BookType String searchBookType) {
        this.searchBookType = searchBookType;
        return this;
    }

    public SearchBookModel listener(SearchListener listener) {
        this.searchListener = listener;
        return this;
    }

    public SearchBookModel useMy716(boolean useMy716) {
        this.useMy716 = useMy716;
        return this;
    }

    public SearchBookModel setup() {
        initSearchEngineS();
        return this;
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
    public void onSearchStart(ISearchTask searchTask) {
        if (!searchTasks.contains(searchTask)) {
            searchTasks.add(searchTask);
        }
    }

    @Override
    public void onSearchResult(List<SearchBookBean> searchBooks) {
        searchListener.loadMoreSearchBook(searchBooks);
        hasData = true;
    }

    @Override
    public void onSearchError(ISearchTask searchTask) {
        searchTasks.remove(searchTask);
        if (searchTasks.size() == 0 && !hasData) {
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
            if (cursor < limit) {
                cursor++;
            }
        }
    }
}
