package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.content.Default716;
import com.monke.monkeybook.model.content.DefaultShuqi;
import com.monke.monkeybook.model.impl.ISearchTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SearchTaskImpl implements ISearchTask {

    private CompositeDisposable disposables;
    private final OnSearchingListener listener;

    private int index;

    private int successCount;

    public SearchTaskImpl(OnSearchingListener listener) {
        this.listener = listener;

        disposables = new CompositeDisposable();
    }

    @Override
    public void startSearch(int index, String query, Scheduler scheduler) {
        if (TextUtils.isEmpty(query) || isDisposed()) {
            return;
        }

        this.index = index;

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
            } else {
                stopSearch();
                listener.onSearchComplete(this);
            }
            return;
        }

        if (!searchEngine.getHasMore()) {
            listener.moveToNextSearchEngine();
            if (listener.hasNextSearchEngine()) {
                toSearch(query, scheduler);
            } else {
                stopSearch();
                listener.onSearchComplete(this);
            }
        } else {
            searchEngine.searchBegin();
            WebBookModel.getInstance()
                    .searchBook(searchEngine.getTag(), query, searchEngine.getPage())
                    .subscribeOn(scheduler)
                    .doOnNext(result -> {
                        saveData(result);
                        incrementSourceWeight(searchEngine.getTag(), searchEngine.getElapsedTime());
                    })
                    .doOnError(throwable -> decrementSourceWeight(searchEngine.getTag()))
                    .delay(index * 50L, TimeUnit.MILLISECONDS)
                    .flatMap(searchBookBeans -> Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                        boolean hasMore = true;
                        if (!isDisposed() && !searchBookBeans.isEmpty()) {
                            listener.onSearchResult(searchBookBeans);

                            if (TextUtils.equals(searchBookBeans.get(0).getTag(), Default716.TAG)) {
                                hasMore = false;
                            }
                            if (TextUtils.equals(searchBookBeans.get(0).getTag(), DefaultShuqi.TAG)) {
                                hasMore = false;
                            }
                        } else {
                            hasMore = false;
                        }
                        emitter.onNext(hasMore);
                        emitter.onComplete();
                    }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            if (!isDisposed()) {
                                disposables.add(d);
                            }
                        }

                        @Override
                        public void onNext(Boolean result) {
                            whenNext(searchEngine, result, query, scheduler);
                            successCount += 1;
                        }

                        @Override
                        public void onError(Throwable e) {
                            whenError(searchEngine, query, scheduler);
                        }
                    });
        }
    }

    private void whenNext(SearchEngine searchEngine, boolean hasMore, String query, Scheduler scheduler) {
        if (isDisposed()) {
            return;
        }

        searchEngine.searchEnd(hasMore);
        if (!listener.hasNextSearchEngine()) {
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
        if (!listener.hasNextSearchEngine()) {
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
