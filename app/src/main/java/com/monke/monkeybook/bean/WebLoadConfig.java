package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WebLoadConfig implements Parcelable {

    private String title;
    private String url;
    private String userAgent;
    private String loginTag;

    public WebLoadConfig(String title, String url, String userAgent, String loginTag) {
        this.title = title;
        this.url = url;
        this.userAgent = userAgent;
        this.loginTag = loginTag;
    }

    public WebLoadConfig(String title, String url, String userAgent) {
        this.title = title;
        this.url = url;
        this.userAgent = userAgent;
    }

    protected WebLoadConfig(Parcel in) {
        title = in.readString();
        url = in.readString();
        userAgent = in.readString();
        loginTag = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(userAgent);
        dest.writeString(loginTag);
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLoginTag() {
        return loginTag;
    }

    public void setLoginTag(String loginTag) {
        this.loginTag = loginTag;
    }
}
