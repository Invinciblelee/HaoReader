//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SearchBookBean implements Parcelable {
    @Id
    private String noteUrl;
    private String coverUrl;//封面URL
    private String name;
    private String author;
    private String tag;
    private String kind;//分类
    private String origin;//来源
    private String desc;
    private String lastChapter;
    private String introduce; //简介
    private String chapterUrl;//目录URL
    @Transient
    private long words;
    @Transient
    private String state;
    @Transient
    private Boolean isCurrentSource = false;
    @Transient
    private int originNum = 1;

    @Transient
    private List<String> origins;

    public SearchBookBean() {

    }


    @Generated(hash = 472283759)
    public SearchBookBean(String noteUrl, String coverUrl, String name, String author, String tag,
                          String kind, String origin, String desc, String lastChapter, String introduce,
                          String chapterUrl) {
        this.noteUrl = noteUrl;
        this.coverUrl = coverUrl;
        this.name = name;
        this.author = author;
        this.tag = tag;
        this.kind = kind;
        this.origin = origin;
        this.desc = desc;
        this.lastChapter = lastChapter;
        this.introduce = introduce;
        this.chapterUrl = chapterUrl;
    }


    protected SearchBookBean(Parcel in) {
        noteUrl = in.readString();
        coverUrl = in.readString();
        name = in.readString();
        author = in.readString();
        tag = in.readString();
        kind = in.readString();
        origin = in.readString();
        desc = in.readString();
        lastChapter = in.readString();
        introduce = in.readString();
        chapterUrl = in.readString();
        words = in.readLong();
        state = in.readString();
        byte tmpIsCurrentSource = in.readByte();
        isCurrentSource = tmpIsCurrentSource == 0 ? null : tmpIsCurrentSource == 1;
        originNum = in.readInt();
        origins = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeString(coverUrl);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(tag);
        dest.writeString(kind);
        dest.writeString(origin);
        dest.writeString(desc);
        dest.writeString(lastChapter);
        dest.writeString(introduce);
        dest.writeString(chapterUrl);
        dest.writeLong(words);
        dest.writeString(state);
        dest.writeByte((byte) (isCurrentSource == null ? 0 : isCurrentSource ? 1 : 2));
        dest.writeInt(originNum);
        dest.writeStringList(origins);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchBookBean> CREATOR = new Creator<SearchBookBean>() {
        @Override
        public SearchBookBean createFromParcel(Parcel in) {
            return new SearchBookBean(in);
        }

        @Override
        public SearchBookBean[] newArray(int size) {
            return new SearchBookBean[size];
        }
    };

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return TextUtils.isEmpty(author) ? "未知" : author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getWords() {
        return words;
    }

    public void setWords(long words) {
        this.words = words;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLastChapter() {
        return lastChapter == null ? "" : lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOrigin() {
        return TextUtils.isEmpty(origin) ? origin : origin.contains("⪢") ? origin : ("⪢" + origin);
    }

    public void setOrigin(String origin) {
        this.origin = origin;
        addOrigin(origin);
    }

    public void addOrigin(String origin) {
        if (this.origins == null) {
            this.origins = new ArrayList<>();
        }

        if (!this.origins.contains(origin)) {
            this.origins.add(origin);
        }
        originNum = this.origins.size();
    }

    public List<String> getOrigins() {
        return this.origins == null ? new ArrayList<String>() : this.origins;
    }

    public Boolean getCurrentSource() {
        return isCurrentSource;
    }

    public void setCurrentSource(Boolean currentSource) {
        isCurrentSource = currentSource;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getIsCurrentSource() {
        return this.isCurrentSource;
    }

    public void setIsCurrentSource(Boolean isCurrentSource) {
        this.isCurrentSource = isCurrentSource;
    }

    public int getOriginNum() {
        return originNum;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getChapterUrl() {
        return this.chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }
}