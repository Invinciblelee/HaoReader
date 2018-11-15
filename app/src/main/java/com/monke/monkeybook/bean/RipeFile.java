package com.monke.monkeybook.bean;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class RipeFile {

    private String name;
    private String path;
    private long size;
    private long date;
    private String suffix;

    public RipeFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof RipeFile){
            return TextUtils.equals(((RipeFile) obj).path, this.path);
        }
        return super.equals(obj);
    }
}
