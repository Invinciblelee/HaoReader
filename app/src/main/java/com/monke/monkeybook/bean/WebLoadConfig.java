package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebView;

import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;

public class WebLoadConfig implements Parcelable {

    private String title;
    private String url;
    private String tag;
    private String userAgent;

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
        userAgent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(tag);
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

    public void intoWebView(WebView webView) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, url);
            switch (analyzeUrl.getRequestMethod()) {
                case POST:
                    webView.postUrl(analyzeUrl.getUrl(), analyzeUrl.getPostData());
                    break;
                case GET:
                    webView.loadUrl(analyzeUrl.getQueryUrl(), analyzeUrl.getHeaderMap());
                    break;
                case DEFAULT:
                    webView.loadUrl(analyzeUrl.getUrl(), analyzeUrl.getHeaderMap());

            }
        } catch (Exception e) {
            webView.loadUrl(url);
        }
    }
}
