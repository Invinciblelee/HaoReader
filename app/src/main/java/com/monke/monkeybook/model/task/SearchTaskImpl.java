package com.monke.monkeybook.model.task;

import android.text.TextUtils;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchEngine;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.content.Default716;
import com.monke.monkeybook.model.impl.ISearchTask;
import com.monke.monkeybook.utils.ListUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchTaskImpl implements ISearchTask {

    private CompositeDisposable disposables;
    private OnSearchingListener listener;

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
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }

        disposables = null;
    }

    private void toSearch(String query, Scheduler scheduler) {
        final SearchEngine searchEngine = listener.nextSearchEngine();
        if (searchEngine != null) {
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
                WebBookModelImpl.getInstance()
                        .searchOtherBook(query, searchEngine.getPage(), searchEngine.getTag())
                        .subscribeOn(scheduler)
                        .flatMap(this::dispatchResult)
                        .doAfterNext(bool -> incrementSourceWeight(searchEngine.getTag(), searchEngine.getElapsedTime()))
                        .doOnError(throwable -> decrementSourceWeight(searchEngine.getTag()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                if (!isDisposed()) {
                                    disposables.add(d);
                                }
                            }

                            @Override
                            public void onNext(Boolean hasMore) {
                                whenNext(searchEngine, hasMore, query, scheduler);
                                successCount += 1;
                            }

                            @Override
                            public void onError(Throwable e) {
                                whenError(searchEngine, query, scheduler);
                            }
                        });
            }
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

    private Observable<Boolean> dispatchResult(final List<SearchBookBean> searchBookBeans) {
        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean hasMore = true;
            if (!isDisposed() && searchBookBeans != null && !searchBookBeans.isEmpty()) {
                listener.onSearchResult(ListUtils.removeDuplicate(searchBookBeans, (o1, o2) -> o1.getName().compareTo(o2.getName())));
                saveData(searchBookBeans);

                if(TextUtils.equals(searchBookBeans.get(0).getTag(), Default716.TAG)){
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
            emitter.onNext(hasMore);
        }).onErrorReturnItem(false);
    }

    private boolean isDisposed() {
        return disposables == null || disposables.isDisposed();
    }

    private static void incrementSourceWeight(String tag, long elapsedTime) {
        Schedulers.single().createWorker().schedule(() -> {
            BookSourceBean bookSourceBean = BookSourceManager.getInstance().getBookSourceByTag(tag);
            if (bookSourceBean != null && elapsedTime < 10000) {
                bookSourceBean.increaseWeight((int) (10000 / (1000 + elapsedTime)));
                BookSourceManager.getInstance().saveBookSource(bookSourceBean);
            }
        });
    }

    private static void decrementSourceWeight(String tag) {
        Schedulers.single().createWorker().schedule(() -> {
            BookSourceBean sourceBean = BookSourceManager.getInstance().getBookSourceByTag(tag);
            if (sourceBean != null) {
                sourceBean.increaseWeight(-100);
                BookSourceManager.getInstance().saveBookSource(sourceBean);
            }
        });
    }

    private static void saveData(List<SearchBookBean> searchBookBeans) {
        Schedulers.single().createWorker().schedule(() ->
                DbHelper.getInstance().getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(searchBookBeans));
    }

}
