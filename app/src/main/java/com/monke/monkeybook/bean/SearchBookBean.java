//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.monke.monkeybook.help.ChapterHelp;
import com.monke.monkeybook.model.source.My716;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SearchBookBean implements Parcelable, Comparable<SearchBookBean> {
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
    private Long addTime;
    @Transient
    private int weight;
    @Transient
    private int searchTime;
    @Transient
    private int lastChapterNum;
    @Transient
    private long words;
    @Transient
    private String state;
    @Transient
    private boolean isCurrentSource = false;
    @Transient
    private int originNum = 1;
    @Transient
    private List<String> originUrls;

    public SearchBookBean() {

    }


    @Generated(hash = 1805065778)
    public SearchBookBean(String noteUrl, String coverUrl, String name, String author, String tag,
                          String kind, String origin, String desc, String lastChapter, String introduce,
                          String chapterUrl, Long addTime) {
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
        this.addTime = addTime;
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
        if (in.readByte() == 0) {
            addTime = null;
        } else {
            addTime = in.readLong();
        }
        weight = in.readInt();
        searchTime = in.readInt();
        words = in.readLong();
        state = in.readString();
        isCurrentSource = in.readByte() != 0;
        originNum = in.readInt();
        originUrls = in.createStringArrayList();
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
        if (addTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(addTime);
        }
        dest.writeInt(weight);
        dest.writeInt(searchTime);
        dest.writeLong(words);
        dest.writeString(state);
        dest.writeByte((byte) (isCurrentSource ? 1 : 0));
        dest.writeInt(originNum);
        dest.writeStringList(originUrls);
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
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        if (TextUtils.isEmpty(this.author)) {
            this.author = "未知";
        }
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
        this.lastChapterNum = ChapterHelp.guessChapterNum(lastChapter);
    }

    public int getLastChapterNum() {
        if(this.lastChapterNum == 0) {
            this.lastChapterNum = ChapterHelp.guessChapterNum(lastChapter);
        }
        return lastChapterNum;
    }

    public void setLastChapterNum(int lastChapterNum) {
        this.lastChapterNum = lastChapterNum;
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
        addOriginUrl(tag);
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void addOriginUrl(String origin) {
        if (this.originUrls == null) {
            this.originUrls = new ArrayList<>();
        }

        if (!this.originUrls.contains(origin)) {
            this.originUrls.add(origin);
        }
        originNum = this.originUrls.size();
    }

    public List<String> getOriginUrls() {
        return this.originUrls == null ? new ArrayList<String>() : this.originUrls;
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
        this.addTime = System.currentTimeMillis();
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(int searchTime) {
        this.searchTime = searchTime;
    }

    public Long getAddTime() {
        return this.addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    @Override
    public int compareTo(SearchBookBean o) {
        int result;
        if (this.getIsCurrentSource()) {
            return -1;
        } else if (o.getIsCurrentSource()) {
            return 1;
        } else if(TextUtils.equals(this.getTag(), My716.TAG)){
            return -1;
        }else if(TextUtils.equals(o.getTag(), My716.TAG)){
            return 1;
        }else if ((result = Integer.compare(o.getLastChapterNum(), this.getLastChapterNum())) != 0) {
            return result;
        } else if ((result = Long.compare(this.getAddTime(), o.getAddTime())) != 0) {
            return result;
        }
        return Integer.compare(o.getWeight(), this.getWeight());
    }

}