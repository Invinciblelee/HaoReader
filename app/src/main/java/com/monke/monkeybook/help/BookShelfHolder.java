package com.monke.monkeybook.help;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BookShelfHolder {

    private BookShelfBean mBook;


    private static volatile BookShelfHolder mInstance;

    private Map<String, OnDataChangedListener> mListeners;


    public static BookShelfHolder get() {
        if (mInstance == null) {
            synchronized (BookShelfHolder.class) {
                if (mInstance == null) {
                    mInstance = new BookShelfHolder();
                }
            }
        }
        return mInstance;
    }

    public void post(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null) {
            mBook = bookShelfBean;
            dispatchChange(mBook);
        }
    }

    public BookShelfBean getBook() {
        return mBook;
    }

    public void observe(Object object, OnDataChangedListener listener) {
        if (mListeners == null) {
            mListeners = new HashMap<>();
        }

        if (object != null && listener != null) {
            mListeners.put(object.getClass().getSimpleName(), listener);

            if (mBook != null) {
                listener.onChanged(mBook);
            }
        }

    }

    public void unsubscribe(Object object) {
        if (object != null) {
            if (mListeners != null && !mListeners.isEmpty()) {
                mListeners.remove(object.getClass().getSimpleName());
            }
        }
    }


    private void dispatchChange(BookShelfBean bookShelfBean) {
        if (mListeners == null || mListeners.isEmpty()) {
            return;
        }
        Observable.fromIterable(mListeners.values())
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(listener -> listener != null)
                .subscribe(new SimpleObserver<OnDataChangedListener>() {
                    @Override
                    public void onNext(OnDataChangedListener listener) {
                        listener.onChanged(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public interface OnDataChangedListener {


        void onChanged(BookShelfBean bookShelfBean);
    }

}
