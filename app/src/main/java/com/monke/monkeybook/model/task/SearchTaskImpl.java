package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.impl.ISearchTask;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SearchTaskImpl implements ISearchTask {

    private CompositeDisposable disposables;

    private final OnSearchingListener listener;

    private AtomicInteger loadingCount = new AtomicInteger();

    private int successCount;

    public SearchTaskImpl(OnSearchingListener listener) {
        this.listener = listener;

        disposables = new CompositeDisposable();
    }

    @Override
    public void startSearch(String query, Scheduler scheduler) {
        if (TextUtils.isEmpty(query) || isDisposed()) {
            return;
        }

        successCount = 0;

        listener.onSearchStart(this);

        toSearch(query, scheduler);
    }

    @Override
    public void stopSearch() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }

        disposables = null;
    }

    private void toSearch(String query, Scheduler scheduler) {
        final SearchEngine searchEngine = listener.nextSearchEngine();
        if (searchEngine == null) {
            if (listener.hasNextSearchEngine()) {
                toSearch(query, scheduler);
            } else if (loadingCount.get() == 0) {
                stopSearch();
                listener.onSearchComplete(this);
            }
            return;
        }

        if (!searchEngine.getHasMore()) {
            if (listener.hasNextSearchEngine()) {
                toSearch(query, scheduler);
            } else if (loadingCount.get() == 0) {
                stopSearch();
                listener.onSearchComplete(this);
            }
        } else {
            searchEngine.searchBegin();
            WebBookModel.getInstance()
                    .searchBook(searchEngine.getTag(), query, searchEngine.getPage())
                    .timeout(30L, TimeUnit.SECONDS)
                    .subscribeOn(scheduler)
                    .doOnNext(result -> {
                        saveData(result);
                        incrementSourceWeight(searchEngine.getTag(), searchEngine.getElapsedTime());
                    })
                    .doOnError(throwable -> decrementSourceWeight(searchEngine.getTag()))
                    .flatMap(searchBookBeans -> {
                        boolean hasMore = true;
                        if (!isDisposed() && !searchBookBeans.isEmpty()) {
                            listener.onSearchResult(searchBookBeans);
                        } else {
                            hasMore = false;
                        }
                        return Observable.just(hasMore);
                    })
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            if (!isDisposed()) {
                                disposables.add(d);
                                loadingCount.incrementAndGet();
                            }
                        }

                        @Override
                        public void onNext(Boolean result) {
                            whenNext(searchEngine, query, scheduler, result);
                            successCount += 1;
                        }

                        @Override
                        public void onError(Throwable e) {
                            whenError(searchEngine, query, scheduler);
                        }
                    });
        }
    }

    private void whenNext(SearchEngine searchEngine, String query, Scheduler scheduler, boolean hasMore) {
        if (isDisposed()) {
            return;
        }

        searchEngine.searchEnd(hasMore);
        if (loadingCount.decrementAndGet() == 0 && !listener.hasNextSearchEngine()) {
            stopSearch();
            listener.onSearchComplete(this);
        } else {
            toSearch(query, scheduler);
        }
    }

    private void whenError(SearchEngine searchEngine, String query, Scheduler scheduler) {
        if (isDisposed()) {
            return;
        }

        searchEngine.searchEnd(false);
        if (loadingCount.decrementAndGet() == 0 && !listener.hasNextSearchEngine()) {
            stopSearch();
            if (successCount == 0) {
                listener.onSearchError(this);
            } else {
                listener.onSearchComplete(this);
            }
        } else {
            toSearch(query, scheduler);
        }
    }

    private boolean isDisposed() {
        return disposables == null || disposables.isDisposed();
    }

    private void incrementSourceWeight(String tag, long elapsedTime) {
        BookSourceBean bookSourceBean = BookSourceManager.getByUrl(tag);
        if (bookSourceBean != null && elapsedTime < 10000) {
            bookSourceBean.increaseWeight((int) (10000 / (1000 + elapsedTime)));
            BookSourceManager.save(bookSourceBean);
        }
    }

    private void decrementSourceWeight(String tag) {
        BookSourceBean sourceBean = BookSourceManager.getByUrl(tag);
        if (sourceBean != null) {
            sourceBean.increaseWeight(-100);
            BookSourceManager.save(sourceBean);
        }
    }

    private void saveData(List<SearchBookBean> searchBookBeans) {
        DbHelper.getInstance().getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(searchBookBeans);
    }

}
