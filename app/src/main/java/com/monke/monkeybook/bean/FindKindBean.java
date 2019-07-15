package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class FindKindBean implements Parcelable {
    private String group;
    private String tag;
    private String kindName;
    private String kindUrl;

    public FindKindBean() {

    }

    protected FindKindBean(Parcel in) {
        group = in.readString();
        tag = in.readString();
        kindName = in.readString();
        kindUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group);
        dest.writeString(tag);
        dest.writeString(kindName);
        dest.writeString(kindUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FindKindBean> CREATOR = new Creator<FindKindBean>() {
        @Override
        public FindKindBean createFromParcel(Parcel in) {
            return new FindKindBean(in);
        }

        @Override
        public FindKindBean[] newArray(int size) {
            return new FindKindBean[size];
        }
    };

    public String getKindName() {
        return kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    public String getKindUrl() {
        return kindUrl;
    }

    public void setKindUrl(String kindUrl) {
        this.kindUrl = kindUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @NonNull
    @Override
    public String toString() {
        return "FindKindBean{" +
                "group='" + group + '\'' +
                ", tag='" + tag + '\'' +
                ", kindName='" + kindName + '\'' +
                ", kindUrl='" + kindUrl + '\'' +
                '}';
    }
}
