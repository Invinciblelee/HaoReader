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
    private int startThisId;
    private int threadsNum;
    private int searchPageCount;
    private SearchListener searchListener;
    private boolean useMy716;
    private boolean searchEngineChanged = false;

    private ExecutorService executor;
    private Scheduler scheduler;

    private final List<SearchEngine> searchEngineS = new ArrayList<>();
    private final List<ISearchTask> searchTasks = new ArrayList<>();

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
        searchEngineS.clear();
        if (useMy716 && Objects.equals(ACache.get(MApplication.getInstance()).getAsString("getZfbHb"), "True")) {
            searchEngineS.add(new SearchEngine(My716.TAG));
        }

        List<BookSourceBean> bookSourceBeans = BookSourceManager.getInstance().getSelectedBookSource();
        if (bookSourceBeans != null) {
            for (BookSourceBean bookSourceBean : bookSourceBeans) {
                searchEngineS.add(new SearchEngine(bookSourceBean.getBookSourceUrl()));
            }
        }
        searchEngineChanged = false;
    }

    public void startSearch(int id, String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }

        startThisId = id;

        if (!searchTasks.isEmpty()) {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
                searchTask.setId(id);
            }
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
            search(id, query);
        }
    }

    private void search(int id, String query) {
        if (searchTasks.isEmpty()) {
            for (int i = 0, size = Math.min(searchEngineS.size(), threadsNum); i < size; i++) {
                ISearchTask searchTask = new SearchTaskImpl(id, this);
                searchTask.startSearch(query, scheduler);
                searchTasks.add(searchTask);
            }
        } else {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.startSearch(query, scheduler);
            }
        }
    }

    public void stopSearch() {
        if (isTaskRunning()) {
            searchListener.searchBookFinish();
        }
        if (!searchTasks.isEmpty()) {
            for (ISearchTask searchTask : searchTasks) {
                searchTask.stopSearch();
                searchTask.setId(0);
            }
        }
    }

    public void shutdownSearch() {
        if (!searchTasks.isEmpty()) {
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

    @Override
    public boolean checkSameTask(int id) {
        return startThisId == id;
    }

    @Override
    public SearchEngine getNextSearchEngine() {
        SearchEngine searchEngine = null;
        synchronized (this) {
            for (SearchEngine engine : searchEngineS) {
                if (engine.getHasMore() && !engine.isRunning() && engine.getPage() < searchPageCount) {
                    searchEngine = engine;
                    break;
                }
            }
        }
        return searchEngine;
    }

    @Override
    public void onSearchResult(List<SearchBookBean> searchBooks) {
        searchListener.loadMoreSearchBook(searchBooks);
    }

    @Override
    public void onSearchError() {
        for (ISearchTask searchTask : searchTasks) {
            if (!searchTask.isComplete()) {
                return;
            }
        }
        searchListener.searchBookError();
    }

    @Override
    public void onSearchComplete() {
        if (isTaskRunning()) {
            return;
        }

        searchListener.searchBookFinish();
    }

    private boolean isTaskRunning() {
        if (searchTasks.isEmpty()) {
            return false;
        }

        for (ISearchTask searchTask : searchTasks) {
            if (!searchTask.isComplete()) {
                return true;
            }
        }
        return false;
    }

    public interface SearchListener {
        void searchSourceEmpty();

        void resetSearchBook();

        void searchBookFinish();

        void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList);

        void searchBookError();
    }

}
