package com.monke.monkeybook.presenter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchHistoryBeanDao;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.presenter.contract.SearchBookContract;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchBookPresenterImpl extends BasePresenterImpl<SearchBookContract.View> implements SearchBookContract.Presenter, SearchBookModel.SearchListener {
    private static final int BOOK = 2;

    private String searchKey;
    private SearchBookModel searchBookModel;

    public SearchBookPresenterImpl(Context context) {
        //搜索引擎初始化
        searchBookModel = new SearchBookModel(context)
                .listener(this)
                .useMy716(AppConfigHelper.get().getBoolean("useMy716", true))
                .useShuqi(AppConfigHelper.get().getBoolean("useShuqi", true))
                .setup();
    }

    @Override
    public void fromIntentSearch(Intent intent) {
        String keyWord = null;
        if (intent != null) {
            keyWord = intent.getStringExtra("searchKey");
            if (keyWord == null && intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                ClipData.Item item = intent.getClipData().getItemAt(0);
                keyWord = StringUtils.valueOf(item.getText()).trim();

                if (!TextUtils.isEmpty(keyWord)) {
                    int start = keyWord.indexOf("《");
                    int end = keyWord.indexOf("》");
                    if (start >= 0 && end > 1) {
                        keyWord = keyWord.substring(start + 1, end);
                    } else if (keyWord.length() > 12) {
                        keyWord = keyWord.substring(0, 12);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && keyWord == null && Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                String type = intent.getType();
                if ("text/plain".equals(type)) {
                    keyWord = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                }
            }
        }

        mView.searchBook(keyWord);
    }

    @Override
    public void insertSearchHistory() {
        final int type = SearchBookPresenterImpl.BOOK;
        final String content = mView.getEdtContent();
        Observable.create((ObservableOnSubscribe<SearchHistoryBean>) e -> {
            List<SearchHistoryBean> data = DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.eq(content))
                    .limit(1)
                    .build().list();
            SearchHistoryBean searchHistoryBean;
            if (null != data && data.size() > 0) {
                searchHistoryBean = data.get(0);
                searchHistoryBean.setDate(System.currentTimeMillis());
                DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao().update(searchHistoryBean);
            } else {
                searchHistoryBean = new SearchHistoryBean(type, content, System.currentTimeMillis());
                DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao().insert(searchHistoryBean);
            }
            e.onNext(searchHistoryBean);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<SearchHistoryBean>() {
                    @Override
                    public void onNext(SearchHistoryBean value) {
                        mView.insertSearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void cleanSearchHistory() {
        final String content = mView.getEdtContent();
        Observable.create((ObservableOnSubscribe<Integer>) e -> {
            int a = DbHelper.getInstance().getDb().delete(SearchHistoryBeanDao.TABLENAME,
                    SearchHistoryBeanDao.Properties.Type.columnName + "=? and " + SearchHistoryBeanDao.Properties.Content.columnName + " like ?",
                    new String[]{String.valueOf(SearchBookPresenterImpl.BOOK), "%" + content + "%"});
            e.onNext(a);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>() {
                    @Override
                    public void onNext(Integer value) {
                        if (value > 0) {
                            mView.querySearchHistorySuccess(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void cleanSearchHistory(SearchHistoryBean searchHistoryBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao().delete(searchHistoryBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            querySearchHistory(mView.getEdtContent());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void querySearchHistory(String query) {
        Observable.create((ObservableOnSubscribe<List<SearchHistoryBean>>) e -> {
            List<SearchHistoryBean> data = DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Type.eq(SearchBookPresenterImpl.BOOK), SearchHistoryBeanDao.Properties.Content.like("%" + query + "%"))
                    .orderDesc(SearchHistoryBeanDao.Properties.Date)
                    .limit(100)
                    .build().list();
            e.onNext(data);
            e.onComplete();
        })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchHistoryBean>>() {
                    @Override
                    public void onNext(List<SearchHistoryBean> value) {
                        if (null != value)
                            mView.querySearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void toSearchBooks(String key) {
        if (key != null) {
            searchKey = key;
        }
        if (!NetworkUtil.isNetworkAvailable()) {
            mView.searchBookError();
            return;
        }

        if (key == null) {
            searchBookModel.startSearch(searchKey);
        } else {
            searchBookModel.startSearch(key);
        }
    }
    @Override
    public void initSearchEngineS(String group) {
        if (TextUtils.isEmpty(group)) {
            searchBookModel.initSearchEngineS1(BookSourceManager.getInstance().getSelectedBookSource(), group);
        } else {
            searchBookModel.initSearchEngineS1(BookSourceManager.getInstance().getEnableSourceByGroup(group), group);
        }
    }
    @Override
    public void stopSearch() {
        searchBookModel.stopSearch();
    }

    @Override
    public void useMy716(Boolean bool) {
        searchBookModel.useMy716(bool);
        searchBookModel.notifySearchEngineChanged();
    }

    @Override
    public void useShuqi(Boolean bool) {
        searchBookModel.useShuqi(bool);
        searchBookModel.notifySearchEngineChanged();
    }
    @Override
    public void searchSourceEmpty() {
        mView.showBookSourceEmptyTip();
    }

    @Override
    public void searchBookReset() {
        mView.resetSearchBook();
    }

    @Override
    public void searchBookFinish() {
        mView.refreshFinish();
    }

    @Override
    public void loadMoreSearchBook(List<SearchBookBean> searchBookBeanList) {
        mView.loadMoreSearchBook(searchBookBeanList);
    }

    @Override
    public void searchBookError() {
        mView.searchBookError();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        searchBookModel.shutdownSearch();
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SEARCH_BOOK)})
    public void searchBook(String searchKey) {
        mView.searchBook(searchKey);
    }


    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.SOURCE_LIST_CHANGE)})
    public void sourceListChange(Boolean change) {
        searchBookModel.notifySearchEngineChanged();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }
}
