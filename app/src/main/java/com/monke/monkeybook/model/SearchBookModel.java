package com.monke.monkeybook.model;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.model.content.Default716;
import com.monke.monkeybook.model.content.DefaultShuqi;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.model.task.SearchTaskImpl;
import com.monke.basemvplib.NetworkUtil;

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
    private String group;
    private SearchListener searchListener;
    private boolean searchEngineChanged = false;
    private boolean useMy716;
    private boolean useShuqi;
    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final List<ISearchTask> searchTasks = new ArrayList<>();

    private ExecutorService executor;
    private final SearchHandler searchHandler;

    private SearchIterator searchIterator;

    private static class SearchHandler extends Handler {

        private static final int MSG_SEARCH = 1;
        private static final int MSG_QUERY = 2;
        private static final int MSG_EMPTY = 3;
        private static final int MSG_ERROR = 4;
        private static final int MSG_FINISH = 6;
        private static final int MSG_RESET = 7;

        private SearchBookModel model;

        private SearchHandler(SearchBookModel model) {
            this.model = model;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SEARCH) {
                model.search((String) msg.obj);
            } else if (msg.what == MSG_QUERY) {
                new SearchTaskImpl(model).startSearch((String) msg.obj, model.getScheduler());
            } else if (msg.what == MSG_EMPTY && model.searchListener != null) {
                model.searchListener.searchSourceEmpty();
            } else if (msg.what == MSG_ERROR && model.searchListener != null) {
                model.searchListener.searchBookError();
            } else if (msg.what == MSG_FINISH && model.searchListener != null) {
                model.searchListener.searchBookFinish();
            } else if (msg.what == MSG_RESET && model.searchListener != null) {
                model.searchListener.searchBookReset();
            }
        }
    }

    public SearchBookModel(Context context) {
        AppConfigHelper configHelper = AppConfigHelper.get();
        threadsNum = Math.max(1, configHelper.getInt(context.getString(R.string.pk_threads_num), 6));
        threadsNum = Math.min(30, threadsNum);
        searchPageCount = configHelper.getInt(context.getString(R.string.pk_search_page_count), 1);
        searchHandler = new SearchHandler(this);
    }

    private Scheduler getScheduler() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(threadsNum);
        }
        return Schedulers.from(executor);
    }

    /**
     * 搜索引擎初始化
     */
    public void initSearchEngineS() {
        if (!searchEngineS.isEmpty()) {
            searchEngineS.clear();
        }

        if (useMy716) {
            searchEngineS.add(new SearchEngine(Default716.TAG));
        }
        if (useShuqi) {
            searchEngineS.add(new SearchEngine(DefaultShuqi.TAG));
        }
        final List<BookSourceBean> bookSourceBeans = BookSourceManager.getEnabledByGroup(group);
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
            //searchHandler.obtainMessage(SearchHandler.MSG_EMPTY).sendToTarget();
        } else {
            for (SearchEngine searchEngine : searchEngineS) {
                searchEngine.searchReset();
            }
        }

        if (!NetworkUtil.isNetworkAvailable()) {
            searchHandler.obtainMessage(SearchHandler.MSG_ERROR).sendToTarget();
            return;
        }

        if (searchEngineS.isEmpty()) {
            searchHandler.obtainMessage(SearchHandler.MSG_EMPTY).sendToTarget();
        } else {
            searchHandler.obtainMessage(SearchHandler.MSG_RESET).sendToTarget();

            searchHandler.removeMessages(SearchHandler.MSG_SEARCH);
            Message msg = searchHandler.obtainMessage(SearchHandler.MSG_SEARCH, query);
            searchHandler.sendMessageDelayed(msg, 200L);
        }
    }

    private void search(String query) {
        searchIterator = new SearchIterator(searchEngineS, searchPageCount);

        searchHandler.removeMessages(SearchHandler.MSG_QUERY);
        for (int i = 0, size = Math.min(searchEngineS.size(), threadsNum); i < size; i++) {
            Message msg = searchHandler.obtainMessage(SearchHandler.MSG_QUERY, query);
            searchHandler.sendMessageDelayed(msg, i * 50L);
        }
    }

    private boolean clearSearch() {
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
            searchHandler.obtainMessage(SearchHandler.MSG_FINISH).sendToTarget();
        }
    }

    public void shutdownSearch() {
        clearSearch();
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
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

    public SearchBookModel useShuqi(boolean useShuqi) {
        this.useShuqi = useShuqi;
        return this;
    }

    public SearchBookModel group(String group) {
        this.group = group;
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
    public void onSearchStart(ISearchTask searchTask) {
        if (!searchTasks.contains(searchTask)) {
            searchTasks.add(searchTask);
        }
    }

    @Override
    public void onSearchResult(List<SearchBookBean> searchBooks) {
        searchListener.loadMoreSearchBook(searchBooks);
    }

    @Override
    public void onSearchError(ISearchTask searchTask) {
        searchTasks.remove(searchTask);
        if (searchTasks.size() == 0) {
            searchHandler.obtainMessage(SearchHandler.MSG_ERROR).sendToTarget();
        }
    }

    @Override
    public void onSearchComplete(ISearchTask searchTask) {
        searchTasks.remove(searchTask);
        if (searchTasks.size() == 0) {
            searchHandler.obtainMessage(SearchHandler.MSG_FINISH).sendToTarget();
        }
    }

    public interface SearchListener {
        void searchSourceEmpty();

        void searchBookReset();

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
        public synchronized boolean hasNext() {
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
        public synchronized SearchEngine next() {
            int i = cursor;
            if (i >= limit)
                return null;
            cursor = i + 1;
            return searchEngines.get(i);
        }
    }
}
