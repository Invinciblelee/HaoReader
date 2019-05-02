//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.model.annotation.BookType;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 书本信息
 */
@Entity
public class BookInfoBean implements Parcelable {

    private String name; //小说名
    private String tag;
    @Id
    private String noteUrl;  //如果是来源网站   则小说根地址 /如果是本地  则是小说本地MD5
    private String chapterListUrl;  //章节目录地址
    private Long finalRefreshData = System.currentTimeMillis();  //章节最后更新时间
    private String coverUrl; //小说封面
    private String customCoverPath;//自定义小说封面
    private String author;//作者
    private String introduce; //简介
    private String origin; //来源
    private String charset;//编码
    private String bookType;//类型 TEXT AUDIO COMIC DOWNLOAD

    public BookInfoBean() {

    }


    @Generated(hash = 1261366546)
    public BookInfoBean(String name, String tag, String noteUrl, String chapterListUrl, Long finalRefreshData,
                        String coverUrl, String customCoverPath, String author, String introduce, String origin,
                        String charset, String bookType) {
        this.name = name;
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.chapterListUrl = chapterListUrl;
        this.finalRefreshData = finalRefreshData;
        this.coverUrl = coverUrl;
        this.customCoverPath = customCoverPath;
        this.author = author;
        this.introduce = introduce;
        this.origin = origin;
        this.charset = charset;
        this.bookType = bookType;
    }


    protected BookInfoBean(Parcel in) {
        name = in.readString();
        tag = in.readString();
        noteUrl = in.readString();
        chapterListUrl = in.readString();
        if (in.readByte() == 0) {
            finalRefreshData = null;
        } else {
            finalRefreshData = in.readLong();
        }
        coverUrl = in.readString();
        customCoverPath = in.readString();
        author = in.readString();
        introduce = in.readString();
        origin = in.readString();
        charset = in.readString();
        bookType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(tag);
        dest.writeString(noteUrl);
        dest.writeString(chapterListUrl);
        if (finalRefreshData == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(finalRefreshData);
        }
        dest.writeString(coverUrl);
        dest.writeString(customCoverPath);
        dest.writeString(author);
        dest.writeString(introduce);
        dest.writeString(origin);
        dest.writeString(charset);
        dest.writeString(bookType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookInfoBean> CREATOR = new Creator<BookInfoBean>() {
        @Override
        public BookInfoBean createFromParcel(Parcel in) {
            return new BookInfoBean(in);
        }

        @Override
        public BookInfoBean[] newArray(int size) {
            return new BookInfoBean[size];
        }
    };

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getChapterListUrl() {
        return chapterListUrl;
    }

    public void setChapterListUrl(String chapterListUrl) {
        this.chapterListUrl = chapterListUrl;
    }

    public long getFinalRefreshData() {
        return finalRefreshData;
    }

    public void setFinalRefreshData(Long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCustomCoverPath() {
        return customCoverPath;
    }

    public void setCustomCoverPath(String customCoverPath) {
        this.customCoverPath = customCoverPath;
    }

    public String getRealCoverUrl(){
        if(!TextUtils.isEmpty(customCoverPath)){
            return customCoverPath;
        }
        return coverUrl;
    }

    public String getAuthor() {
        return author == null ? "" : author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getBookType() {
        return bookType == null ? BookType.TEXT : bookType;
    }

    public void setBookType(@BookType String bookType) {
        this.bookType = bookType;
    }

    BookInfoBean copy() {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.name = name;
        bookInfoBean.tag = tag;
        bookInfoBean.noteUrl = noteUrl;
        bookInfoBean.finalRefreshData = finalRefreshData;
        bookInfoBean.chapterListUrl = chapterListUrl;
        bookInfoBean.coverUrl = coverUrl;
        bookInfoBean.customCoverPath = customCoverPath;
        bookInfoBean.author = author;
        bookInfoBean.introduce = introduce;
        bookInfoBean.origin = origin;
        bookInfoBean.charset = charset;
        bookInfoBean.bookType = bookType;
        return bookInfoBean;
    }
}