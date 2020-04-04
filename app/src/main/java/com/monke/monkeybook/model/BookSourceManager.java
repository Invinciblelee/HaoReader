package com.monke.monkeybook.model;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.basemvplib.NetworkUtil;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
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

    public static List<BookSourceBean> getEnabled() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
    }

    public static List<BookSourceBean> getFindValid() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .whereOr(BookSourceBeanDao.Properties.ValidFind.isNull(), BookSourceBeanDao.Properties.ValidFind.eq(true))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
    }

    public static List<BookSourceBean> getFindEnabled() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .whereOr(BookSourceBeanDao.Properties.EnableFind.isNull(), BookSourceBeanDao.Properties.EnableFind.eq(true))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .orderAsc(BookSourceBeanDao.Properties.SerialNumber)
                .list();
    }

    public static long getEnabledCount() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .count();
    }

    public static List<BookSourceBean> getEnabledByGroup(String group) {
        if (group == null) {
            return getEnabled();
        }
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.Enable.eq(true))
                .where(BookSourceBeanDao.Properties.BookSourceGroup.like("%" + group + "%"))
                .orderRaw(BookSourceBeanDao.Properties.Weight.columnName + " DESC")
                .list();
    }

    public static List<String> getEnableGroupList() {
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

    public static List<BookSourceBean> fuzzyQuery(String query) {
        if (StringUtils.isNotBlank(query)) {
            String term = "%" + query + "%";
            return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                            BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                            BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                    .orderRaw(BookSourceManager.getSort())
                    .list();
        } else {
            return getAll();
        }
    }

    public static BookSourceBean getByUrl(String url) {
        if (url == null) return null;
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().load(url);
    }

    public static List<BookSourceBean> getAll() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                .orderRaw(getSort())
                .list();
    }

    public static long getCount() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().count();
    }

    public static List<String> getGroupList() {
        List<String> groupList = new ArrayList<>();
        String sql = "SELECT DISTINCT " + BookSourceBeanDao.Properties.BookSourceGroup.columnName + " FROM " + BookSourceBeanDao.TABLENAME;
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

    public static boolean isEmpty() {
        return DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().count() == 0;
    }


    public static String getSort() {
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

    public static void addAll(List<BookSourceBean> bookSourceBeans) {
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            add(bookSourceBean);
        }
    }

    public static void delete(BookSourceBean bookSourceBean) {
        if (bookSourceBean != null) {
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().delete(bookSourceBean);
        }
    }

    public static void deleteAll(List<BookSourceBean> bookSourceBeans) {
        if (bookSourceBeans != null) {
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().deleteInTx(bookSourceBeans);
        }
    }

    public static void add(BookSourceBean bookSourceBean) {
        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().substring(0, bookSourceBean.getBookSourceUrl().lastIndexOf("/")));
        }

        BookSourceBean temp = getByUrl(bookSourceBean.getBookSourceUrl());
        if (temp != null) {
            bookSourceBean.setSerialNumber(temp.getSerialNumber());
        }

        if (bookSourceBean.getSerialNumber() == 0) {
            long count = getCount();
            bookSourceBean.setSerialNumber((int) count + 1);
        }
        DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }

    public static void save(BookSourceBean sourceBean) {
        if (sourceBean != null) {
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplace(sourceBean);
        }
    }


    public static Observable<Boolean> importFromNet(String url) {
        try {
            url = url.trim();
            if (NetworkUtil.isIPv4Address(url)) {
                url = String.format("http://%s:65501", url);
            }

            if (StringUtils.isJsonType(url)) {
                return importFromJson(url);
            }

            if (URLUtils.isUrl(url)) {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(StringUtils.getBaseUrl(url), url);
                return SimpleModel.getResponse(analyzeUrl)
                        .subscribeOn(Schedulers.single())
                        .flatMap(rsp -> importFromJson(rsp.body()))
                        .observeOn(AndroidSchedulers.mainThread());
            }
            throw new IllegalArgumentException("url is invalid");
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    public static Observable<Boolean> importFromJson(String json) {
        return Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            List<BookSourceBean> bookSourceBeans = Assistant.fromJson(StringUtils.wrapJsonArray(json), new TypeToken<List<BookSourceBean>>() {
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
                        add(bookSourceBean);
                    } catch (Exception exception) {
                        DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl()))
                                .buildDelete().executeDeleteWithoutDetachingEntities();
                    }
                }
            }
            e.onNext(index > 0);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
