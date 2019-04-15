package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.webkit.CookieManager;

import com.monke.monkeybook.help.CookieHelper;

public class WebLoadConfig implements Parcelable {

    private String title;
    private String url;
    private String tag;
    private String userAgent;
    private String cookieKey;

    public WebLoadConfig(String title, String url, String tag, String userAgent) {
        this.title = title;
        this.url = url;
        this.tag = tag;
        this.userAgent = userAgent;
    }

    public WebLoadConfig(String url, String userAgent) {
        this.url = url;
        this.userAgent = userAgent;
    }


    protected WebLoadConfig(Parcel in) {
        title = in.readString();
        url = in.readString();
        tag = in.readString();
        cookieKey = in.readString();
        userAgent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(tag);
        dest.writeString(cookieKey);
        dest.writeString(userAgent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WebLoadConfig> CREATOR = new Creator<WebLoadConfig>() {
        @Override
        public WebLoadConfig createFromParcel(Parcel in) {
            return new WebLoadConfig(in);
        }

        @Override
        public WebLoadConfig[] newArray(int size) {
            return new WebLoadConfig[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCookieKey(String cookieKey) {
        this.cookieKey = cookieKey;
    }

    public String getCookieKey() {
        return cookieKey;
    }

    public String getTag() {
        return tag == null ? url : tag;
    }

    public void setTag(String loginTag) {
        this.tag = loginTag;
    }


    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setCookie(String url) {
        if (!TextUtils.isEmpty(tag)) {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url);
            if (TextUtils.isEmpty(cookieKey)) {
                CookieHelper.get().setCookie(tag, cookie);
            } else if (!TextUtils.isEmpty(cookie)) {
                try {
                    final String type;
                    if (cookieKey.contains("&&")) {
                        type = "&&";
                    } else {
                        type = "\\|\\|";
                    }
                    final String[] keys = cookieKey.split(type);
                    final String[] arr = cookie.split(";");

                    int tryCount = 0;
                    for (String key : keys) {
                        for (String string : arr) {
                            String[] pair = string.split("=");
                            if (pair.length > 1 && key.equals(pair[0].trim()) && !TextUtils.isEmpty(pair[1].trim())) {
                                tryCount += 1;
                            }
                        }
                    }

                    if ((type.equals("&&") && tryCount == keys.length) || (type.equals("\\|\\|") && tryCount == 1)) {
                        CookieHelper.get().setCookie(tag, cookie);
                    }
                } catch (Exception e) {
                    CookieHelper.get().setCookie(tag, cookie);
                }
            }
        }
    }
}
