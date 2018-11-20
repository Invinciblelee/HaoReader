package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.model.source.My716;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchTaskImpl implements ISearchTask {

    private int id;
    private CompositeDisposable disposables;

    private boolean isComplete;
    private int successCount;

    private OnSearchingListener listener;

    public SearchTaskImpl(int id, OnSearchingListener listener) {
        this.id = id;
        this.listener = listener;

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
        if (TextUtils.isEmpty(query) || !listener.checkSameTask(getId())) {
            return;
        }

        reset();

        toSearch(query, scheduler, null);
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
    public boolean isComplete() {
        return isComplete;
    }

    private synchronized void toSearch(String query, Scheduler scheduler, SearchEngine engine) {
        final SearchEngine searchEngine;
        if (engine == null) {
            searchEngine = listener.getNextSearchEngine();
        } else {
            searchEngine = engine;
        }
        if (searchEngine != null) {
            long start = System.currentTimeMillis();
            searchEngine.searchBegin();
            WebBookModelImpl.getInstance()
                    .searchOtherBook(query, searchEngine.getPage(), searchEngine.getTag())
                    .subscribeOn(scheduler)
                    .flatMap(this::dispatchResult)
                    .doOnComplete(() -> incrementSourceWeight(searchEngine.getTag(), start))
                    .doOnError(throwable -> decrementSourceWeight(searchEngine.getTag()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onNext(Boolean bool) {
                            whenNext(searchEngine, bool, query, scheduler);
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
        if (isComplete) {
            return;
        }

        searchEngine.searchEnd(hasMore);
        SearchEngine nextSearchEngine = listener.getNextSearchEngine();
        if (nextSearchEngine == null) {
            stopSearch();
            listener.onSearchComplete();
        } else {
            toSearch(query, scheduler, nextSearchEngine);
        }
    }

    private void whenError(SearchEngine searchEngine, String query, Scheduler scheduler) {
        if (isComplete) {
            return;
        }

        searchEngine.searchEnd(false);
        SearchEngine nextSearchEngine = listener.getNextSearchEngine();
        if (nextSearchEngine == null) {
            stopSearch();
            if (successCount == 0) {
                listener.onSearchError();
            } else {
                listener.onSearchComplete();
            }
        } else {
            toSearch(query, scheduler, nextSearchEngine);
        }
    }

    private Observable<Boolean> dispatchResult(final List<SearchBookBean> searchBookBeans) {
        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean hasMore = true;
            if (!isComplete && listener.checkSameTask(getId())) {
                if (searchBookBeans != null && !searchBookBeans.isEmpty()) {
                    listener.onSearchResult(searchBookBeans);

                    if(TextUtils.equals(searchBookBeans.get(0).getTag(), My716.TAG)){
                        hasMore = false;
                    }

                    saveData(searchBookBeans);
                } else {
                    hasMore = false;
                }
            }
            emitter.onNext(hasMore);
        }).onErrorReturnItem(false);
    }

    private void incrementSourceWeight(String tag, long startTime) {
        Schedulers.single().createWorker().schedule(() -> {
            int searchTime = (int) (System.currentTimeMillis() - startTime);
            BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(tag);
            if (bookSourceBean != null && searchTime < 10000) {
                bookSourceBean.increaseWeight(10000 / (1000 + searchTime));
                BookshelfHelp.saveBookSource(bookSourceBean);
            }
        });
    }

    private void decrementSourceWeight(String tag) {
        Schedulers.single().createWorker().schedule(() -> {
            BookSourceBean sourceBean = BookshelfHelp.getBookSourceByTag(tag);
            if (sourceBean != null) {
                sourceBean.increaseWeight(-100);
                BookshelfHelp.saveBookSource(sourceBean);
            }
        });
    }

    private void reset() {
        isComplete = false;
        successCount = 0;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
    }

    private static void saveData(List<SearchBookBean> searchBookBeans) {
        Schedulers.single().createWorker().schedule(() -> {
            if (searchBookBeans != null) {
                DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(searchBookBeans);
            }
        });

    }
}
