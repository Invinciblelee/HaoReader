package com.monke.monkeybook.bean;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class SearchEngine {
    private String tag;
    private boolean hasMore;
    private int page;
    private boolean isRunning;

    public SearchEngine(String tag) {
        this.tag = tag;
        hasMore = true;
        isRunning = false;
        page = 0;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public int getPage() {
        return page;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void searchBegin() {
        this.page += 1;
        this.isRunning = true;
    }

    public void searchEnd(boolean hasMore) {
        this.hasMore = hasMore;
        this.isRunning = false;
    }

    public void searchReset() {
        this.hasMore = true;
        this.isRunning = false;
        this.page = 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SearchEngine) {
            return TextUtils.equals(((SearchEngine) obj).tag, this.tag);
        }
        return super.equals(obj);
    }
}