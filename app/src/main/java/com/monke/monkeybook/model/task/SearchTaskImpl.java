package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.impl.ISearchTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SearchTaskImpl implements ISearchTask {

    private int id;
    private CompositeDisposable disposables;
    private int successCount;

    private boolean isComplete;

    private OnSearchingListener listener;

    private List<SearchEngine> searchEngines;


    public SearchTaskImpl(int id, List<SearchEngine> searchEngines, OnSearchingListener listener) {
        this.id = id;
        this.listener = listener;
        this.searchEngines = searchEngines;

        disposables = new CompositeDisposable();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void startSearch(String query, Scheduler scheduler) {
        if (searchEngines == null || TextUtils.isEmpty(query) || !listener.checkSameTask(getId())) {
            return;
        }

        isComplete = false;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        toSearch(query, scheduler);
    }

    @Override
    public void stopSearch() {
        if (!isComplete) {
            isComplete = true;
        }

        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public SearchEngine getNextSearchEngine() {
        if (!isComplete && searchEngines != null) {
            for (SearchEngine engine : searchEngines) {
                if (listener.checkSearchEngine(engine)) {
                    return engine;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasSuccess() {
        return successCount > 0;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    private synchronized void toSearch(String query, Scheduler scheduler) {
        SearchEngine searchEngine = getNextSearchEngine();
        if (listener.checkSearchEngine(searchEngine)) {
            WebBookModelImpl.getInstance()
                    .searchOtherBook(query, searchEngine.getPage(), searchEngine.getTag())
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onNext(List<SearchBookBean> searchBookBeans) {
                            successCount += 1;
                            boolean hasMore = true;
                            if (!isComplete && listener.checkSameTask(getId())) {
                                searchBookBeans = removeDuplicate(searchBookBeans);
                                if (searchBookBeans.size() > 0) {
                                    if (!listener.checkExists(searchBookBeans.get(0))) {
                                        listener.onSearchResult(searchBookBeans);
                                    }
                                } else {
                                    hasMore = false;
                                }
                                whenNext(searchEngine, hasMore, query, scheduler);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            whenError(searchEngine, query, scheduler);
                        }
                    });
        } else {
            stopSearch();
            listener.onSearchComplete();
        }
    }

    private void whenNext(SearchEngine searchEngine, boolean hasMore, String query, Scheduler scheduler) {
        if (isComplete) {
            return;
        }
        searchEngine.pageAdd();
        searchEngine.setHasMore(hasMore);
        toSearch(query, scheduler);
    }

    private void whenError(SearchEngine searchEngine, String query, Scheduler scheduler) {
        if (isComplete) {
            return;
        }
        searchEngine.setEnabled(false);
        if (!hasSuccess() && listener.getShowingItemCount() == 0 && getNextSearchEngine() == null) {
            stopSearch();
            listener.onSearchError();
        } else {
            searchEngine.pageAdd();
            toSearch(query, scheduler);
        }
    }

    private static List<SearchBookBean> removeDuplicate(List<SearchBookBean> orderList) {
        Set<SearchBookBean> set = new TreeSet<>((a, b) -> {
            // 字符串则按照asicc码升序排列
            return a.getName().compareTo(b.getName());
        });

        set.addAll(orderList);
        return new ArrayList<>(set);
    }
}
