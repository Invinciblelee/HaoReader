//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

public class DownloadChapterBean implements Parcelable {
    private String noteUrl;

    private int durChapterIndex;  //当前章节数

    private String durChapterUrl;  //当前章节对应的文章地址

    private String durChapterName;  //当前章节名称

    private String tag;

    private String bookName;

    private String coverUrl; //小说封面

    protected DownloadChapterBean(Parcel in) {
        noteUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterUrl = in.readString();
        durChapterName = in.readString();
        tag = in.readString();
        bookName = in.readString();
        coverUrl = in.readString();
    }

    public DownloadChapterBean(String noteUrl, int durChapterIndex, String durChapterUrl,
            String durChapterName, String tag, String bookName, String coverUrl) {
        this.noteUrl = noteUrl;
        this.durChapterIndex = durChapterIndex;
        this.durChapterUrl = durChapterUrl;
        this.durChapterName = durChapterName;
        this.tag = tag;
        this.bookName = bookName;
        this.coverUrl = coverUrl;
    }

    public DownloadChapterBean() {
    }

    @Transient
    public static final Creator<DownloadChapterBean> CREATOR = new Creator<DownloadChapterBean>() {
        @Override
        public DownloadChapterBean createFromParcel(Parcel in) {
            return new DownloadChapterBean(in);
        }

        @Override
        public DownloadChapterBean[] newArray(int size) {
            return new DownloadChapterBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterUrl);
        dest.writeString(durChapterName);
        dest.writeString(tag);
        dest.writeString(bookName);
        dest.writeString(coverUrl);
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getDurChapterUrl() {
        return durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public String getDurChapterName() {
        return durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof DownloadChapterBean){
            return TextUtils.equals(((DownloadChapterBean) obj).getDurChapterUrl(), this.durChapterUrl);
        }
        return super.equals(obj);
    }
}
