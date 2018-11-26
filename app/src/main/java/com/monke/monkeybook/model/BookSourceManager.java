package com.monke.monkeybook.model;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2017/12/15.
 * 所有书源
 */

public class BookSourceManager extends BaseModelImpl {
    private List<BookSourceBean> selectedBookSource;
    private List<BookSourceBean> allBookSource;
    private List<String> groupList = new ArrayList<>();

    private BookSourceManager() {

    }

    private static class InstanceHolder {
        private static final BookSourceManager SINGLETON = new BookSourceManager();
    }

    public static BookSourceManager getInstance() {
        return InstanceHolder.SINGLETON;
    }

    public List<BookSourceBean> getSelectedBookSource() {
        if (selectedBookSource == null) {
            selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.Enable.eq(true))
                    .orderRaw(getBookSourceSort())
                    .list();
        }
        return selectedBookSource;
    }

    public List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .orderRaw(getBookSourceSort())
                    .list();
            upGroupList();
        }
        return allBookSource;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public boolean isEmpty() {
        return DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().count() == 0;
    }

    public void refreshBookSource() {
        allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderRaw(getBookSourceSort())
                .list();

        selectedBookSource = new ArrayList<>();
        if (allBookSource != null) {
            for (BookSourceBean bookSourceBean : allBookSource) {
                if (bookSourceBean.getEnable()) {
                    selectedBookSource.add(bookSourceBean);
                }
            }
        }

        upGroupList();
    }

    public String getBookSourceSort() {
        int sourceSort = AppConfigHelper.get(MApplication.getInstance()).getInt("SourceSort", 0);
        switch (sourceSort) {
            case 1:
                return BookSourceBeanDao.Properties.Weight.columnName + " DESC";
            case 2:
                return BookSourceBeanDao.Properties.BookSourceName.columnName + " ASC";
            default:
                return BookSourceBeanDao.Properties.SerialNumber.columnName + " ASC";
        }
    }

    public void addBookSource(List<BookSourceBean> bookSourceBeans) {
        refreshBookSource();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            addBookSource(bookSourceBean);
        }
        refreshBookSource();
    }

    public void addBookSource(BookSourceBean bookSourceBean) {
        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().substring(0, bookSourceBean.getBookSourceUrl().lastIndexOf("/")));
        }

        BookSourceBean temp = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).unique();
        if (temp != null) {
            bookSourceBean.setSerialNumber(temp.getSerialNumber());
            bookSourceBean.setEnable(temp.getEnable());
        }else {
            bookSourceBean.setEnable(true);
        }

        if (bookSourceBean.getSerialNumber() == 0) {
            bookSourceBean.setSerialNumber(allBookSource.size() + 1);
        }
        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    public BookSourceBean getBookSourceByTag(String tag) {
        if (tag == null)
            return null;
        return DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();
    }

    public void saveBookSource(BookSourceBean sourceBean) {
        if (sourceBean != null) {
            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(sourceBean);
        }
    }

    private synchronized void upGroupList() {
        groupList.clear();
        for (BookSourceBean bookSourceBean : allBookSource) {
            if (!TextUtils.isEmpty(bookSourceBean.getBookSourceGroup()) && !groupList.contains(bookSourceBean.getBookSourceGroup())) {
                groupList.add(bookSourceBean.getBookSourceGroup());
            }
        }
        Collections.sort(groupList);
    }

    public Observable<Boolean> importSourceFromWww(URL url) {
        return createService(String.format("%s://%s", url.getProtocol(), url.getHost()), "utf-8", IHttpGetApi.class)
                .getWebContent(url.getPath(), AnalyzeHeaders.getMap(null))
                .flatMap(rsp -> importBookSourceO(rsp.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> importBookSourceO(String json) {
        return Observable.create(e -> {
            try {
                List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
                }.getType());
                int index = 0;
                for (BookSourceBean bookSourceBean : bookSourceBeans) {
                    if (Objects.equals(bookSourceBean.getBookSourceGroup(), "删除")) {
                        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();
                    } else {
                        try {
                            new URL(bookSourceBean.getBookSourceUrl());
                            bookSourceBean.setSerialNumber(++index);
                            addBookSource(bookSourceBean);
                        } catch (Exception exception) {
                            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                    .buildDelete().executeDeleteWithoutDetachingEntities();
                        }
                    }
                }
                refreshBookSource();
                e.onNext(index > 0);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }

    public void importDefaultSource(Context context, OnImportSourceListener listener) {
        try {
            URL url = new URL(context.getString(R.string.default_source_url));
            BookSourceManager.getInstance().importSourceFromWww(url)
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            listener.onSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            listener.onError();
                        }
                    });
        } catch (Exception e) {
            listener.onError();
        }
    }


    public interface OnImportSourceListener {
        void onSuccess();

        void onError();
    }
}
