//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterHelp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Objects;

/**
 * 章节列表
 */
@Entity
public class ChapterListBean implements Parcelable {

    private String noteUrl; //对应BookInfoBean noteUrl;

    @Index
    private Integer durChapterIndex;  //当前章节数
    @Id
    private String durChapterUrl;  //当前章节对应的文章地址
    private String durChapterName;  //当前章节名称
    private String tag;
    //章节内容在文章中的起始位置(本地)
    private Integer start;
    //章节内容在文章中的终止位置(本地)
    private Integer end;
    private String bookName;


    @Generated(hash = 256175124)
    public ChapterListBean(String noteUrl, Integer durChapterIndex, String durChapterUrl,
            String durChapterName, String tag, Integer start, Integer end, String bookName) {
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

    protected ChapterListBean(Parcel in) {
        noteUrl = in.readString();
        if (in.readByte() == 0) {
            durChapterIndex = null;
        } else {
            durChapterIndex = in.readInt();
        }
        durChapterUrl = in.readString();
        durChapterName = in.readString();
        tag = in.readString();
        if (in.readByte() == 0) {
            start = null;
        } else {
            start = in.readInt();
        }
        if (in.readByte() == 0) {
            end = null;
        } else {
            end = in.readInt();
        }
        bookName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        if (durChapterIndex == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(durChapterIndex);
        }
        dest.writeString(durChapterUrl);
        dest.writeString(durChapterName);
        dest.writeString(tag);
        if (start == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(start);
        }
        if (end == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(end);
        }
        dest.writeString(bookName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
        return durChapterName;
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

    public int getStart() {
        return this.start == null ? 0 : this.start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return this.end == null ? 0 : this.end;
    }

    public void setEnd(int end) {
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

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

}
