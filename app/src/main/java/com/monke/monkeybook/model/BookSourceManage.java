package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

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

public class BookSourceManage extends BaseModelImpl {
    private static List<BookSourceBean> selectedBookSource;
    private static List<BookSourceBean> allBookSource;
    public static List<String> groupList = new ArrayList<>();

    public static BookSourceManage getInstance() {
        return new BookSourceManage();
    }

    public static List<BookSourceBean> getSelectedBookSource() {
        if (selectedBookSource == null) {
            selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.Enable.eq(true))
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
        }
        return selectedBookSource;
    }

    public static List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
            upGroupList();
        }
        return allBookSource;
    }

    public static void refreshBookSource() {
        allBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
        selectedBookSource = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
        upGroupList();
    }

    public static void addBookSource(List<BookSourceBean> bookSourceBeans) {
        refreshBookSource();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            addBookSource(bookSourceBean);
        }
        refreshBookSource();
    }

    public static void addBookSource(BookSourceBean bookSourceBean) {
        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().substring(0, bookSourceBean.getBookSourceUrl().lastIndexOf("/")));
        }
        if (!bookSourceBean.getBookSourceName().startsWith("⪢")) {
            bookSourceBean.setBookSourceName("⪢" + bookSourceBean.getBookSourceName());
        }

        BookSourceBean temp = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).unique();
        if (temp != null) {
            bookSourceBean.setSerialNumber(temp.getSerialNumber());
            bookSourceBean.setEnable(temp.getEnable());
        }
        if (bookSourceBean.getSerialNumber() == 0) {
            bookSourceBean.setSerialNumber(allBookSource.size() + 1);
        }
        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    private synchronized static void upGroupList() {
        groupList.clear();
        for (BookSourceBean bookSourceBean : allBookSource) {
            if (!TextUtils.isEmpty(bookSourceBean.getBookSourceGroup()) && !groupList.contains(bookSourceBean.getBookSourceGroup())) {
                groupList.add(bookSourceBean.getBookSourceGroup());
            }
        }
        Collections.sort(groupList);
    }

    public static Observable<Boolean> importSourceFromWww(URL url) {
        return getRetrofitString(String.format("%s://%s", url.getProtocol(), url.getHost()), "utf-8")
                .create(IHttpGetApi.class)
                .getWebContent(url.getPath(), AnalyzeHeaders.getMap(null))
                .flatMap(rsp -> importBookSourceO(rsp.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Boolean> importBookSourceO(String json) {
        return Observable.create(e -> {
            try {
                List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
                }.getType());
                int index = 1;
                for (BookSourceBean bookSourceBean : bookSourceBeans) {
                    if (Objects.equals(bookSourceBean.getBookSourceGroup(), "删除")) {
                        DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();
                    } else {
                        try {
                            new URL(bookSourceBean.getBookSourceUrl());
                            bookSourceBean.setSerialNumber(index);
                            addBookSource(bookSourceBean);
                            index++;
                        } catch (Exception exception) {
                            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                    .buildDelete().executeDeleteWithoutDetachingEntities();
                        }
                    }
                }
                refreshBookSource();
                e.onNext(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(false);
            }
            e.onComplete();
        });
    }

}
