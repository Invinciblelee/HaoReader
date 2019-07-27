//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.util.ObjectsCompat;

import com.google.gson.annotations.Expose;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.TextProcessor;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Objects;

/**
 * 章节列表
 */
@Entity
public class ChapterBean implements Parcelable, FilterBean, Comparable<ChapterBean> {

    @Id
    private String durChapterUrl;  //当前章节对应的文章地址
    private String nextChapterUrl; //下一章
    private String durChapterName;  //当前章节名称
    private String durChapterPlayUrl; //听书章节播放地址
    private String noteUrl;
    private Integer durChapterIndex;  //当前章节数
    //章节内容在文章中的起始位置(本地)
    private Integer start;
    //章节内容在文章中的终止位置(本地)
    private Integer end;

    @Transient
    @Expose(serialize = false, deserialize = false)
    private String displayDurChapterName;

    @Generated(hash = 256775330)
    public ChapterBean(String durChapterUrl, String nextChapterUrl, String durChapterName,
                       String durChapterPlayUrl, String noteUrl, Integer durChapterIndex, Integer start,
                       Integer end) {
        this.durChapterUrl = durChapterUrl;
        this.nextChapterUrl = nextChapterUrl;
        this.durChapterName = durChapterName;
        this.durChapterPlayUrl = durChapterPlayUrl;
        this.noteUrl = noteUrl;
        this.durChapterIndex = durChapterIndex;
        this.start = start;
        this.end = end;
    }

    public ChapterBean() {
    }

    public ChapterBean(String noteUrl, String durChapterName, String durChapterUrl) {
        this.noteUrl = noteUrl;
        this.durChapterName = durChapterName;
        this.durChapterUrl = durChapterUrl;
    }

    protected ChapterBean(Parcel in) {
        if (in.readByte() == 0) {
            durChapterIndex = null;
        } else {
            durChapterIndex = in.readInt();
        }
        durChapterUrl = in.readString();
        nextChapterUrl = in.readString();
        durChapterPlayUrl = in.readString();
        durChapterName = in.readString();
        noteUrl = in.readString();
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
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (durChapterIndex == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(durChapterIndex);
        }
        dest.writeString(durChapterUrl);
        dest.writeString(nextChapterUrl);
        dest.writeString(durChapterPlayUrl);
        dest.writeString(durChapterName);
        dest.writeString(noteUrl);
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
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChapterBean> CREATOR = new Creator<ChapterBean>() {
        @Override
        public ChapterBean createFromParcel(Parcel in) {
            return new ChapterBean(in);
        }

        @Override
        public ChapterBean[] newArray(int size) {
            return new ChapterBean[size];
        }
    };

    protected ChapterBean copy() {
        ChapterBean chapterBean = new ChapterBean();
        chapterBean.durChapterIndex = durChapterIndex;
        chapterBean.durChapterUrl = durChapterUrl;
        chapterBean.durChapterPlayUrl = durChapterPlayUrl;
        chapterBean.nextChapterUrl = nextChapterUrl;
        chapterBean.durChapterName = durChapterName;
        chapterBean.noteUrl = noteUrl;
        chapterBean.start = start;
        chapterBean.end = end;
        return chapterBean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChapterBean) {
            ChapterBean chapterBean = (ChapterBean) obj;
            return Objects.equals(chapterBean.durChapterUrl, durChapterUrl);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hashCode(durChapterUrl);
    }

    public Boolean getHasCache(BookInfoBean bookInfoBean) {
        return ChapterContentHelp.isChapterCached(bookInfoBean, this);
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getDurChapterName() {
        return durChapterName;
    }

    public String getDisplayDurChapterName() {
        if (displayDurChapterName == null) {
            displayDurChapterName = TextProcessor.formatChapterName(durChapterName);
        }
        return displayDurChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public String getDurChapterUrl() {
        return this.durChapterUrl;
    }

    public String getDurChapterPlayUrl() {
        return durChapterPlayUrl;
    }

    public void setDurChapterPlayUrl(String durChapterPlayUrl) {
        this.durChapterPlayUrl = durChapterPlayUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public int getDurChapterIndex() {
        return this.durChapterIndex == null ? 0 : this.durChapterIndex;
    }

    public int getStart() {
        return this.start == null ? 0 : this.start;
    }

    public int getEnd() {
        return this.end == null ? 0 : this.end;
    }

    public void setDurChapterIndex(Integer durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getNextChapterUrl() {
        return nextChapterUrl;
    }

    public void setNextChapterUrl(String nextChapterUrl) {
        this.nextChapterUrl = nextChapterUrl;
    }

    @Override
    public String[] getFilters() {
        return new String[]{durChapterName};
    }

    @Override
    public String toString() {
        return "ChapterBean{" +
                "durChapterUrl='" + durChapterUrl + '\'' +
                ", nextChapterUrl='" + nextChapterUrl + '\'' +
                ", durChapterName='" + durChapterName + '\'' +
                ", durChapterPlayUrl='" + durChapterPlayUrl + '\'' +
                ", noteUrl='" + noteUrl + '\'' +
                ", durChapterIndex=" + durChapterIndex +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    @Override
    public int compareTo(ChapterBean o) {
        return Integer.compare(durChapterIndex, o.durChapterIndex);
    }
}
