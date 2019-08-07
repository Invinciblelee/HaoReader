//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.monke.monkeybook.help.TextProcessor;
import com.monke.monkeybook.utils.ObjectsCompat;
import com.monke.monkeybook.utils.URLUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private String lastChapter;
    private String introduce; //简介
    private String bookType;
    private String variableString;
    @Transient
    private int weight;
    @Transient
    private int lastChapterNum;
    @Transient
    private String displayLastChapter;
    @Transient
    private boolean isCurrentSource = false;
    @Transient
    private int originNum = 1;
    @Transient
    private List<String> tags;
    @Transient
    private VariableStoreImpl variableStore;

    public SearchBookBean() {
    }


    @Generated(hash = 65370007)
    public SearchBookBean(String noteUrl, String coverUrl, String name, String author, String tag, String kind, String origin,
            String lastChapter, String introduce, String bookType, String variableString) {
        this.noteUrl = noteUrl;
        this.coverUrl = coverUrl;
        this.name = name;
        this.author = author;
        this.tag = tag;
        this.kind = kind;
        this.origin = origin;
        this.lastChapter = lastChapter;
        this.introduce = introduce;
        this.bookType = bookType;
        this.variableString = variableString;
    }


    protected SearchBookBean(Parcel in) {
        noteUrl = in.readString();
        coverUrl = in.readString();
        name = in.readString();
        author = in.readString();
        tag = in.readString();
        kind = in.readString();
        origin = in.readString();
        lastChapter = in.readString();
        introduce = in.readString();
        bookType = in.readString();
        variableString = in.readString();
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
        dest.writeString(lastChapter);
        dest.writeString(introduce);
        dest.writeString(bookType);
        dest.writeString(variableString);
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

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getCoverUrl() {
        return URLUtils.resolve(tag, coverUrl);
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
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public String getDisplayLastChapter() {
        if(displayLastChapter == null){
            displayLastChapter = TextProcessor.formatChapterName(lastChapter);
        }
        return displayLastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public int getLastChapterNum() {
        if (this.lastChapterNum == 0) {
            this.lastChapterNum = TextProcessor.guessChapterNum(lastChapter);
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

    public Boolean isCurrentSource() {
        return this.isCurrentSource;
    }

    public void setIsCurrentSource(Boolean isCurrentSource) {
        this.isCurrentSource = isCurrentSource;
    }

    public int getOriginNum() {
        return originNum;
    }

    public String getIntroduce() {
        return TextProcessor.formatHtml(introduce);
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

    public boolean isSimilarTo(BookInfoBean book, boolean ignoreType) {
        if (book == null) {
            return false;
        }
        return (ignoreType || TextUtils.equals(bookType, book.getBookType()))
                && TextUtils.equals(name, book.getName())
                && TextUtils.equals(author, book.getAuthor());
    }

    public boolean isSimilarTo(SearchBookBean book) {
        if (book == null) {
            return false;
        }
        return TextUtils.equals(bookType, book.bookType)
                && TextUtils.equals(name, book.name)
                && TextUtils.equals(author, book.author);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SearchBookBean) {
            SearchBookBean compare = (SearchBookBean) obj;
            return TextUtils.equals(compare.bookType, bookType)
                    && TextUtils.equals(compare.tag, tag)
                    && TextUtils.equals(compare.name, name)
                    && TextUtils.equals(compare.author, author);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hashCode(bookType) + ObjectsCompat.hashCode(tag) + ObjectsCompat.hashCode(name) + ObjectsCompat.hashCode(author);
    }

    @Override
    public int compareTo(SearchBookBean o) {
        return Integer.compare(o.getLastChapterNum(), this.getLastChapterNum());
    }

    @Override
    public String getVariableString() {
        return this.variableString;
    }

    @Override
    public void setVariableString(String variableString) {
        this.variableString = variableString;
    }

    @Override
    public Map<String, String> getVariableMap() {
        if (variableStore == null) {
            variableStore = new VariableStoreImpl(this.variableString);
        }
        return variableStore.getVariableMap();
    }

    @Override
    public Map<String, String> putVariableMap(Map<String, String> variableMap) {
        if (variableStore == null) {
            variableStore = new VariableStoreImpl(variableString);
        }
        Map<String, String> map = variableStore.putVariableMap(variableMap);
        setVariableString(variableStore.getVariableString());
        return map;
    }

    @Override
    public String putVariable(String key, String value) {
        if (variableStore == null) {
            variableStore = new VariableStoreImpl(variableString);
        }
        String val = variableStore.putVariable(key, value);
        setVariableString(variableStore.getVariableString());
        return val;
    }

    @Override
    public String getVariable(String key) {
        if (variableStore == null) {
            variableStore = new VariableStoreImpl(variableString);
        }
        return variableStore.getVariable(key);
    }

    @NonNull
    @Override
    public String toString() {
        return "SearchBookBean{" +
                "noteUrl='" + noteUrl + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", tag='" + tag + '\'' +
                ", kind='" + kind + '\'' +
                ", origin='" + origin + '\'' +
                ", lastChapter='" + lastChapter + '\'' +
                ", introduce='" + introduce + '\'' +
                ", bookType='" + bookType + '\'' +
                ", variableString='" + variableString + '\'' +
                ", weight=" + weight +
                ", lastChapterNum=" + lastChapterNum +
                ", isCurrentSource=" + isCurrentSource +
                ", originNum=" + originNum +
                ", tags=" + tags +
                '}';
    }
}