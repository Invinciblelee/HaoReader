package com.monke.monkeybook.model;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
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

    private volatile static BookSourceManager mInstance;

    public static BookSourceManager getInstance() {
        if (mInstance == null) {
            synchronized (BookSourceManager.class) {
                if (mInstance == null) {
                    mInstance = new BookSourceManager();
                }
            }
        }
        return mInstance;
    }

    public List<BookSourceBean> getSelectedBookSource() {
        if (selectedBookSource == null) {
            selectedBookSource = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.Enable.eq(true))
                    .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                    .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                    .list();
        }
        return selectedBookSource;
    }

    public List<BookSourceBean> getEnableSourceByGroup(String group) {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .where(BookSourceBeanDao.Properties.BookSourceGroup.like("%" + group + "%"))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .list();
    }

    public List<String> getEnableGroupList() {
        List<String> groupList = new ArrayList<>();
        String sql = "SELECT DISTINCT "
                + BookSourceBeanDao.Properties.BookSourceGroup.columnName
                + " FROM " + BookSourceBeanDao.TABLENAME
                + " WHERE " + BookSourceBeanDao.Properties.Enable.name + " = 1";
        Cursor cursor = DbHelper.getInstance().getDaoSession().getDatabase().rawQuery(sql, null);
        if (!cursor.moveToFirst()) return groupList;
        do {
            String group = cursor.getString(0);
            if (TextUtils.isEmpty(group) || TextUtils.isEmpty(group.trim())) continue;
            for (String item : group.split("\\s*[,;，；]\\s*")) {
                if (TextUtils.isEmpty(item) || groupList.contains(item)) continue;
                groupList.add(item);
            }
        } while (cursor.moveToNext());
        Collections.sort(groupList);
        return groupList;
    }

    public BookSourceBean getBookSourceByUrl(String url) {
        if (url == null) return null;
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().load(url);
    }

    public void removeBookSource(BookSourceBean sourceBean) {
        if (sourceBean == null) return;
        DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().delete(sourceBean);
    }

    public List<BookSourceBean> getAllBookSource() {
        if (allBookSource == null) {
            allBookSource = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
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
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().count() == 0;
    }

    public void refreshBookSource() {
        allBookSource = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderRaw(getBookSourceSort())
                .list();

        selectedBookSource = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();

        upGroupList();
    }

    public String getBookSourceSort() {
        int sourceSort = AppConfigHelper.get().getInt("SourceSort", 0);
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

        BookSourceBean temp = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).unique();
        if (temp != null) {
            bookSourceBean.setSerialNumber(temp.getSerialNumber());
            bookSourceBean.setEnable(temp.getEnable());
        } else {
            bookSourceBean.setEnable(true);
        }

        if (bookSourceBean.getSerialNumber() == 0) {
            bookSourceBean.setSerialNumber(allBookSource.size() + 1);
        }
        DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    public BookSourceBean getBookSourceByTag(String tag) {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();
    }

    public void saveBookSource(BookSourceBean sourceBean) {
        if (sourceBean != null) {
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplace(sourceBean);
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

    public Observable<Boolean> importSourceFromWww(String url) {
        try {
            url = url.trim();
            if (NetworkUtil.isIPv4Address(url)) {
                url = String.format("http://%s:65501", url);
            }
            if (StringUtils.isJsonType(url)) {
                return importBookSourceO(url.trim())
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread());
            }
            if (URLUtils.isUrl(url)) {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(StringUtils.getBaseUrl(url), url);
                return SimpleModel.getResponse(analyzeUrl)
                        .flatMap(rsp -> importBookSourceO(rsp.body()))
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread());
            }
            throw new IllegalArgumentException("url is invalid");
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    public Observable<Boolean> importBookSourceO(String json) {
        return Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
            }.getType());
            int index = 0;
            for (BookSourceBean bookSourceBean : bookSourceBeans) {
                if (Objects.equals(bookSourceBean.getBookSourceGroup(), "删除")) {
                    DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                            .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                            .buildDelete().executeDeleteWithoutDetachingEntities();
                } else {
                    try {
                        new URL(bookSourceBean.getBookSourceUrl());
                        bookSourceBean.setSerialNumber(++index);
                        addBookSource(bookSourceBean);
                    } catch (Exception exception) {
                        DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();
                    }
                }
            }
            e.onNext(index > 0);
            e.onComplete();
        }).doOnNext(aBoolean -> {
            if (aBoolean) {
                refreshBookSource();
            }
        });
    }
}
