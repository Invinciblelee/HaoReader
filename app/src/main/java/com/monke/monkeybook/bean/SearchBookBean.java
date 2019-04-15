//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.monke.monkeybook.help.ChapterHelp;
import com.monke.monkeybook.model.content.Default716;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.monke.monkeybook.help.Constant.STRING_MAP;

@Entity
public class SearchBookBean implements Parcelable, Comparable<SearchBookBean>, VariableStore {
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
    private String bookType;
    private String variableString;
    private Long addTime;
    @Transient
    private int weight;
    @Transient
    private int lastChapterNum;
    @Transient
    private boolean isCurrentSource = false;
    @Transient
    private int originNum = 1;
    @Transient
    private List<String> tags;
    @Transient
    private Map<String, String> variableMap;

    public SearchBookBean() {

    }


    @Generated(hash = 995325559)
    public SearchBookBean(String noteUrl, String coverUrl, String name, String author, String tag,
                          String kind, String origin, String desc, String lastChapter, String introduce,
                          String bookType, String variableString, Long addTime) {
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
        this.bookType = bookType;
        this.variableString = variableString;
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
        bookType = in.readString();
        variableString = in.readString();
        if (in.readByte() == 0) {
            addTime = null;
        } else {
            addTime = in.readLong();
        }
        weight = in.readInt();
        lastChapterNum = in.readInt();
        isCurrentSource = in.readByte() != 0;
        originNum = in.readInt();
        tags = in.createStringArrayList();
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
        dest.writeString(bookType);
        dest.writeString(variableString);
        if (addTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(addTime);
        }
        dest.writeInt(weight);
        dest.writeInt(lastChapterNum);
        dest.writeByte((byte) (isCurrentSource ? 1 : 0));
        dest.writeInt(originNum);
        dest.writeStringList(tags);
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

    public String getRealNoteUrl() {
        if (TextUtils.equals(tag, Default716.TAG)
                && !TextUtils.isEmpty(noteUrl)) {
            return noteUrl.substring(5);
        }
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
        return author == null ? "" : author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = ChapterHelp.getFormatChapterName(lastChapter);
    }

    public int getLastChapterNum() {
        if (this.lastChapterNum == 0) {
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
        addTag(tag);
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }

        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
        originNum = this.tags.size();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean isCurrentSource() {
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

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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
        if (this.isCurrentSource()) {
            return -1;
        } else if (o.isCurrentSource()) {
            return 1;
        } else if ((result = Integer.compare(o.getLastChapterNum(), this.getLastChapterNum())) != 0) {
            return result;
        } else if ((result = Long.compare(this.getAddTime(), o.getAddTime())) != 0) {
            return result;
        }
        return Integer.compare(o.getWeight(), this.getWeight());
    }

    @Override
    public String getVariableString() {
        return this.variableString;
    }

    @Override
    public void setVariableString(String variableString) {
        this.variableString = variableString;
    }

    public Map<String, String> getVariableMap() {
        return variableMap;
    }

    @Override
    public void putVariableMap(Map<String, String> variableMap) {
        if (variableMap != null && !variableMap.isEmpty()) {
            final Gson gson = new Gson();
            if (this.variableMap == null) {
                try {
                    this.variableMap = gson.fromJson(variableString, STRING_MAP);
                } catch (Exception ignore) {
                }
            }
            if (this.variableMap == null) {
                this.variableMap = new HashMap<>();
            }
            this.variableMap.putAll(variableMap);
            this.variableString = gson.toJson(this.variableMap);
        }
    }

    @Override
    public String getVariable(String key) {
        if (this.variableMap == null) {
            try {
                this.variableMap = new Gson().fromJson(variableString, STRING_MAP);
            } catch (Exception ignore) {
            }
        }
        return (this.variableMap != null && !this.variableMap.isEmpty()) ? this.variableMap.get(key) : null;
    }
}