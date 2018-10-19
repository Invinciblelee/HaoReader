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

/**
 * 书本信息
 */
@Entity
public class BookInfoBean implements Parcelable {

    private String name; //小说名
    private String tag;
    @Id
    private String noteUrl;  //如果是来源网站   则小说根地址 /如果是本地  则是小说本地MD5
    private String chapterUrl;  //章节目录地址
    private Long finalRefreshData = System.currentTimeMillis();  //章节最后更新时间
    private String coverUrl; //小说封面
    private String author;//作者
    private String introduce; //简介
    private String origin; //来源
    private String charset;//编码

    @Transient
    private List<ChapterListBean> chapterList = new ArrayList<>();    //章节列表
    @Transient
    private List<BookmarkBean> bookmarkList = new ArrayList<>();    //书签列表

    public BookInfoBean() {

    }


    @Generated(hash = 928796558)
    public BookInfoBean(String name, String tag, String noteUrl, String chapterUrl,
                        Long finalRefreshData, String coverUrl, String author, String introduce, String origin,
                        String charset) {
        this.name = name;
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.chapterUrl = chapterUrl;
        this.finalRefreshData = finalRefreshData;
        this.coverUrl = coverUrl;
        this.author = author;
        this.introduce = introduce;
        this.origin = origin;
        this.charset = charset;
    }


    protected BookInfoBean(Parcel in) {
        name = in.readString();
        tag = in.readString();
        noteUrl = in.readString();
        chapterUrl = in.readString();
        if (in.readByte() == 0) {
            finalRefreshData = null;
        } else {
            finalRefreshData = in.readLong();
        }
        coverUrl = in.readString();
        author = in.readString();
        introduce = in.readString();
        origin = in.readString();
        charset = in.readString();
        chapterList = in.createTypedArrayList(ChapterListBean.CREATOR);
        bookmarkList = in.createTypedArrayList(BookmarkBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(tag);
        dest.writeString(noteUrl);
        dest.writeString(chapterUrl);
        if (finalRefreshData == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(finalRefreshData);
        }
        dest.writeString(coverUrl);
        dest.writeString(author);
        dest.writeString(introduce);
        dest.writeString(origin);
        dest.writeString(charset);
        dest.writeTypedList(chapterList);
        dest.writeTypedList(bookmarkList);
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
        return name;
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

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public List<ChapterListBean> getChapterList() {
        if (chapterList == null) {
            return new ArrayList<>();
        } else {
            return chapterList;
        }
    }

    void setChapterList(List<ChapterListBean> chapterlist) {
        this.chapterList = chapterlist;
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

    public List<BookmarkBean> getBookmarkList() {
        if (bookmarkList == null) {
            return new ArrayList<>();
        }
        return bookmarkList;
    }

    void setBookmarkList(List<BookmarkBean> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    BookInfoBean copy() {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.name = name;
        bookInfoBean.tag = tag;
        bookInfoBean.noteUrl = noteUrl;
        bookInfoBean.chapterUrl = chapterUrl;
        bookInfoBean.coverUrl = coverUrl;
        bookInfoBean.author = author;
        bookInfoBean.introduce = introduce;
        bookInfoBean.origin = origin;
        bookInfoBean.charset = charset;
        if (chapterList != null) {
            List<ChapterListBean> newListC = new ArrayList<>();
            for (ChapterListBean aChapterList : chapterList) {
                newListC.add(aChapterList.copy());
            }
            bookInfoBean.setChapterList(newListC);
        }
        if (bookmarkList != null) {
            List<BookmarkBean> newListM = new ArrayList<>();
            for (BookmarkBean aBookmarkList : bookmarkList) {
                newListM.add(aBookmarkList.copy());
            }
            bookInfoBean.setBookmarkList(newListM);
        }
        return bookInfoBean;
    }
}