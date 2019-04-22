package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.monke.basemvplib.CookieStore;
import com.monke.monkeybook.bean.CookieBean;
import com.monke.monkeybook.dao.CookieBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CookieHelper implements CookieStore {

    private static final String TAG = CookieHelper.class.getSimpleName();

    private volatile static CookieHelper mInstance;

    private CookieHelper() {
    }

    public static CookieHelper get() {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new CookieHelper();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void setCookie(String url, String cookie) {
        try {
            CookieBean cookieBean = new CookieBean(url, cookie);
            DbHelper.getInstance().getDaoSession().getCookieBeanDao().insertOrReplace(cookieBean);
            Logger.d(TAG, "setCookie: " + url + " --> " + cookie);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void replaceCookie(String url, String cookie) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(cookie)) {
            return;
        }
        String oldCookie = getCookie(url);
        Logger.d(TAG, "replaceCookie from: " + url + " --> " + cookie);
        if (TextUtils.isEmpty(oldCookie)) {
            setCookie(url, cookie);
        } else {
            final Map<String, String> cookieMap = cookieToMap(oldCookie);
            cookieMap.putAll(CookieHelper.cookieToMap(cookie));
            String newCookie = mapToCookie(cookieMap);
            Logger.d(TAG, "replaceCookie to: " + url + " --> " + newCookie);
            setCookie(url, newCookie);
        }
    }

    @Override
    public String getCookie(String url) {
        try {
            CookieBean cookieBean = DbHelper.getInstance().getDaoSession().getCookieBeanDao().load(url);
            return cookieBean == null ? "" : cookieBean.getCookie();
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public void removeCookie(String url) {
        try {
            DbHelper.getInstance().getDaoSession().queryBuilder(CookieBean.class)
                    .where(CookieBeanDao.Properties.Url.eq(url))
                    .buildDelete();
        } catch (Exception ignore) {
        }
    }

    @Override
    public void clearCookies() {
        try {
            DbHelper.getInstance().getDaoSession().delete(CookieBean.class);
        } catch (Exception ignore) {
        }
    }

    private static Map<String, String> cookieToMap(String cookie) {
        final Map<String, String> cookieMap = new HashMap<>();
        if (StringUtils.isTrimEmpty(cookie)) {
            return cookieMap;
        }
        String[] pairArray = cookie.split(";");
        for (String pair : pairArray) {
            String[] pairs = pair.split("=");
            if (pairs.length == 1) {
                continue;
            }
            String key = pairs[0].trim();
            String value = pairs[1];
            if (!StringUtils.isTrimEmpty(value) || value.trim().equals("null")) {
                cookieMap.put(key, value.trim());
            }
        }
        return cookieMap;
    }

    private static String mapToCookie(Map<String, String> cookieMap) {
        if (cookieMap == null || cookieMap.isEmpty()) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        for (String key : cookieMap.keySet()) {
            String value = cookieMap.get(key);
            if (!StringUtils.isTrimEmpty(value)) {
                builder.append(key)
                        .append("=")
                        .append(value)
                        .append(";");
            }
        }
        return builder.deleteCharAt(builder.lastIndexOf(";")).toString();
    }
}
