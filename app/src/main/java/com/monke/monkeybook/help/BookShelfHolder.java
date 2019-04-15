package com.monke.monkeybook.help;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.utils.ObjectsCompat;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BookShelfHolder {

    private BookShelfBean mBook;


    private static volatile BookShelfHolder mInstance;

    private final Map<String, OnDataChangedListener> mListeners = new HashMap<>();


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

    public void clear() {
        mBook = null;
        mListeners.clear();
    }

    public void observe(Object object, OnDataChangedListener listener) {
        synchronized (mListeners) {
            if (object != null && listener != null) {
                mListeners.put(object.getClass().getSimpleName(), listener);

                if (mBook != null) {
                    listener.onChanged(mBook);
                }
            }
        }
    }

    public void unsubscribe(Object object) {
        synchronized (mListeners) {
            if (object != null) {
                if (!mListeners.isEmpty()) {
                    mListeners.remove(object.getClass().getSimpleName());
                }
            }
        }
    }


    private void dispatchChange(BookShelfBean bookShelfBean) {
        if (mListeners.isEmpty()) {
            return;
        }
        synchronized (mListeners) {
            Observable.fromIterable(mListeners.values())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .filter(ObjectsCompat::nonNull)
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
    }

    public interface OnDataChangedListener {


        void onChanged(BookShelfBean bookShelfBean);
    }

}
