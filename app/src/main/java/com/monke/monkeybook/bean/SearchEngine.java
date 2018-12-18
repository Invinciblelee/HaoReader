package com.monke.monkeybook.bean;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class SearchEngine {
    private String tag;
    private boolean hasMore;
    private int page;
    private long start;

    public SearchEngine(String tag) {
        this.tag = tag;
        hasMore = true;
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

    public long getStart() {
        return start;
    }

    public int getPage() {
        return page;
    }

    public void searchBegin() {
        this.page += 1;
        this.start = System.currentTimeMillis();
    }

    public void searchEnd(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public void searchReset() {
        this.hasMore = true;
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