package com.monke.monkeybook.help;

import com.monke.basemvplib.CookieStore;
import com.monke.monkeybook.bean.CookieBean;
import com.monke.monkeybook.dao.CookieBeanDao;
import com.monke.monkeybook.dao.DbHelper;

public class CookieHelper implements CookieStore {
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
        }catch (Exception ignore){
        }
    }

    @Override
    public String getCookie(String url) {
        try {
            CookieBean cookieBean = DbHelper.getInstance().getDaoSession().getCookieBeanDao().load(url);
            return cookieBean == null ? "" : cookieBean.getCookie();
        }catch (Exception ignore){
        }
        return "";
    }

    @Override
    public void removeCookie(String url) {
        try {
            DbHelper.getInstance().getDaoSession().queryBuilder(CookieBean.class)
                    .where(CookieBeanDao.Properties.Url.eq(url))
                    .buildDelete();
        }catch (Exception ignore){
        }
    }

    @Override
    public void clearCookies() {
        try {
            DbHelper.getInstance().getDaoSession().delete(CookieBean.class);
        }catch (Exception ignore){

        }
    }
}
