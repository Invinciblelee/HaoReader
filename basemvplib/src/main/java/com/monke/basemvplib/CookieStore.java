package com.monke.basemvplib;

public interface CookieStore {
    void setCookie(String url, String cookie);

    String getCookie(String url);

    void removeCookie(String url);

    void clearCookies();
}
