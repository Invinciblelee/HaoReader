//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DaoSession;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterHelp;
import com.monke.monkeybook.utils.StringUtils;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 章节列表
 */
@Entity
public class ChapterListBean implements Parcelable {

    private String noteUrl; //对应BookInfoBean noteUrl;

    private Integer durChapterIndex;  //当前章节数
    @Id
    private String durChapterUrl;  //当前章节对应的文章地址
    private String durChapterName;  //当前章节名称
    private String tag;
    //章节内容在文章中的起始位置(本地)
    private Long start;
    //章节内容在文章中的终止位置(本地)
    private Long end;
    private String bookName;


    protected ChapterListBean(Parcel in) {
        noteUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterUrl = in.readString();
        durChapterName = in.readString();
        tag = in.readString();
        start = in.readLong();
        end = in.readLong();
        bookName = in.readString();
    }

    @Generated(hash = 634102195)
    public ChapterListBean(String noteUrl, Integer durChapterIndex, String durChapterUrl, String durChapterName, String tag,
            Long start, Long end, String bookName) {
        this.noteUrl = noteUrl;
        this.durChapterIndex = durChapterIndex;
        this.durChapterUrl = durChapterUrl;
        this.durChapterName = durChapterName;
        this.tag = tag;
        this.start = start;
        this.end = end;
        this.bookName = bookName;
    }

    @Generated(hash = 1096893365)
    public ChapterListBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterUrl);
        dest.writeString(durChapterName);
        dest.writeString(tag);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeString(bookName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Transient
    public static final Creator<ChapterListBean> CREATOR = new Creator<ChapterListBean>() {
        @Override
        public ChapterListBean createFromParcel(Parcel in) {
            return new ChapterListBean(in);
        }

        @Override
        public ChapterListBean[] newArray(int size) {
            return new ChapterListBean[size];
        }
    };

    protected ChapterListBean copy() {
        ChapterListBean chapterListBean = new ChapterListBean();
        chapterListBean.noteUrl = noteUrl;
        chapterListBean.durChapterIndex = durChapterIndex;
        chapterListBean.durChapterUrl = durChapterUrl;
        chapterListBean.durChapterName = durChapterName;
        chapterListBean.tag = tag;
        chapterListBean.start = start;
        chapterListBean.end = end;
        chapterListBean.bookName = bookName;
        return chapterListBean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChapterListBean) {
            ChapterListBean chapterListBean = (ChapterListBean) obj;
            return Objects.equals(chapterListBean.durChapterUrl, durChapterUrl);
        } else {
            return false;
        }
    }

    public Boolean getHasCache(BookInfoBean bookInfoBean) {
        return BookshelfHelp.isChapterCached(bookInfoBean, this);
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = ChapterHelp.getFormatChapterName(durChapterName);
    }

    public String getDurChapterUrl() {
        return this.durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public int getDurChapterIndex() {
        return this.durChapterIndex == null ? 0 : this.durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public Long getStart() {
        return this.start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return this.end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public void setDurChapterIndex(Integer durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
