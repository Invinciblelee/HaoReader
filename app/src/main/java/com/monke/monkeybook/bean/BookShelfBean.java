//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Parcelable {
    @Transient
    public static final String LOCAL_TAG = "loc_book";
    @Transient
    private boolean isLoading;
    @Transient
    private boolean isQueuing = true;

    @Id
    private String noteUrl; //对应BookInfoBean noteUrl;
    private Integer durChapter = 0;   //当前章节 （包括番外）
    private Integer durChapterPage = 0;  // 当前章节位置   用页码
    private Long finalDate = System.currentTimeMillis();  //最后阅读时间
    private Boolean hasUpdate = false;  //是否有更新
    private Integer newChapters = 0;  //更新章节数
    private String tag;
    private Integer serialNumber = 0; //手动排序
    private Long finalRefreshData = System.currentTimeMillis();  //章节最后更新时间
    private Integer group = 0;
    private String durChapterName;
    private String lastChapterName;
    private Integer chapterListSize = 0;
    private Boolean updateOff = false; //禁止更新

    @Transient
    private BookInfoBean bookInfoBean = new BookInfoBean();
    @Transient
    private List<ChapterListBean> chapterList = new ArrayList<>();    //章节列表
    @Transient
    private List<BookmarkBean> bookmarkList = new ArrayList<>();    //书签列表

    public BookShelfBean() {
    }


    @Generated(hash = 1946753255)
    public BookShelfBean(String noteUrl, Integer durChapter, Integer durChapterPage, Long finalDate, Boolean hasUpdate, Integer newChapters,
            String tag, Integer serialNumber, Long finalRefreshData, Integer group, String durChapterName, String lastChapterName,
            Integer chapterListSize, Boolean updateOff) {
        this.noteUrl = noteUrl;
        this.durChapter = durChapter;
        this.durChapterPage = durChapterPage;
        this.finalDate = finalDate;
        this.hasUpdate = hasUpdate;
        this.newChapters = newChapters;
        this.tag = tag;
        this.serialNumber = serialNumber;
        this.finalRefreshData = finalRefreshData;
        this.group = group;
        this.durChapterName = durChapterName;
        this.lastChapterName = lastChapterName;
        this.chapterListSize = chapterListSize;
        this.updateOff = updateOff;
    }


    protected BookShelfBean(Parcel in) {
        isLoading = in.readByte() != 0;
        isQueuing = in.readByte() != 0;
        noteUrl = in.readString();
        if (in.readByte() == 0) {
            durChapter = null;
        } else {
            durChapter = in.readInt();
        }
        if (in.readByte() == 0) {
            durChapterPage = null;
        } else {
            durChapterPage = in.readInt();
        }
        if (in.readByte() == 0) {
            finalDate = null;
        } else {
            finalDate = in.readLong();
        }
        byte tmpHasUpdate = in.readByte();
        hasUpdate = tmpHasUpdate == 0 ? null : tmpHasUpdate == 1;
        if (in.readByte() == 0) {
            newChapters = null;
        } else {
            newChapters = in.readInt();
        }
        tag = in.readString();
        if (in.readByte() == 0) {
            serialNumber = null;
        } else {
            serialNumber = in.readInt();
        }
        if (in.readByte() == 0) {
            finalRefreshData = null;
        } else {
            finalRefreshData = in.readLong();
        }
        if (in.readByte() == 0) {
            group = null;
        } else {
            group = in.readInt();
        }
        durChapterName = in.readString();
        lastChapterName = in.readString();
        if (in.readByte() == 0) {
            chapterListSize = null;
        } else {
            chapterListSize = in.readInt();
        }
        byte tmpUpdateOff = in.readByte();
        updateOff = tmpUpdateOff == 0 ? null : tmpUpdateOff == 1;
        bookInfoBean = in.readParcelable(BookInfoBean.class.getClassLoader());
        chapterList = in.createTypedArrayList(ChapterListBean.CREATOR);
        bookmarkList = in.createTypedArrayList(BookmarkBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isLoading ? 1 : 0));
        dest.writeByte((byte) (isQueuing ? 1 : 0));
        dest.writeString(noteUrl);
        if (durChapter == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(durChapter);
        }
        if (durChapterPage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(durChapterPage);
        }
        if (finalDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(finalDate);
        }
        dest.writeByte((byte) (hasUpdate == null ? 0 : hasUpdate ? 1 : 2));
        if (newChapters == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(newChapters);
        }
        dest.writeString(tag);
        if (serialNumber == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(serialNumber);
        }
        if (finalRefreshData == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(finalRefreshData);
        }
        if (group == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(group);
        }
        dest.writeString(durChapterName);
        dest.writeString(lastChapterName);
        if (chapterListSize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chapterListSize);
        }
        dest.writeByte((byte) (updateOff == null ? 0 : updateOff ? 1 : 2));
        dest.writeParcelable(bookInfoBean, flags);
        dest.writeTypedList(chapterList);
        dest.writeTypedList(bookmarkList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookShelfBean> CREATOR = new Creator<BookShelfBean>() {
        @Override
        public BookShelfBean createFromParcel(Parcel in) {
            return new BookShelfBean(in);
        }

        @Override
        public BookShelfBean[] newArray(int size) {
            return new BookShelfBean[size];
        }
    };

    public BookShelfBean copy() {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.noteUrl = noteUrl;
        bookShelfBean.durChapter = durChapter;
        bookShelfBean.durChapterPage = durChapterPage;
        bookShelfBean.finalDate = finalDate;
        bookShelfBean.hasUpdate = hasUpdate;
        bookShelfBean.newChapters = newChapters;
        bookShelfBean.tag = tag;
        bookShelfBean.serialNumber = serialNumber;
        bookShelfBean.finalRefreshData = finalRefreshData;
        bookShelfBean.group = group;
        bookShelfBean.durChapterName = durChapterName;
        bookShelfBean.lastChapterName = lastChapterName;
        bookShelfBean.chapterListSize = chapterListSize;
        bookShelfBean.updateOff = updateOff;
        bookShelfBean.bookInfoBean = bookInfoBean.copy();
        if (chapterList != null) {
            for (ChapterListBean aChapterList : chapterList) {
                bookShelfBean.chapterList.add(aChapterList.copy());
            }
        }
        if (bookmarkList != null) {
            for (BookmarkBean aBookmarkList : bookmarkList) {
                bookShelfBean.bookmarkList.add(aBookmarkList.copy());
            }
        }
        return bookShelfBean;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public int getDurChapter() {
        return (durChapter == null || durChapter < 0) ? 0 : durChapter;
    }

    public ChapterListBean getChapter(int index) {
        if (realChapterListEmpty() || index < 0) {
            ChapterListBean chapterListBean = new ChapterListBean();
            chapterListBean.setDurChapterName("暂无");
            chapterListBean.setDurChapterUrl("暂无");
            return chapterListBean;
        } else if (index < getChapterList().size()) {
            return getChapterList().get(index);
        } else {
            durChapter = getChapterList().size() - 1;
            return getChapterList().get(durChapter);
        }
    }

    public BookmarkBean getBookmark(int index) {
        if (realBookmarkListEmpty() || index < 0) {
            return null;
        } else if (index < getBookmarkList().size()) {
            return getBookmarkList().get(index);
        } else {
            return getBookmarkList().get(getChapterList().size() - 1);
        }
    }

    public int getDurChapterPage() {
        return (durChapterPage == null || durChapterPage < 0) ? 0 : durChapterPage;
    }

    public long getFinalDate() {
        return finalDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BookInfoBean getBookInfoBean() {
        return bookInfoBean;
    }

    public void setBookInfoBean(BookInfoBean bookInfoBean) {
        this.bookInfoBean = bookInfoBean;
    }

    public boolean getHasUpdate() {
        return hasUpdate == null ? false : hasUpdate;
    }

    public int getNewChapters() {
        return newChapters;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public long getFinalRefreshData() {
        return this.finalRefreshData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isQueuing() {
        return isQueuing;
    }

    public void setQueuing(boolean queuing) {
        this.isQueuing = queuing;
    }

    public int getGroup() {
        return this.group == null ? 0 : this.group;
    }

    public void setDurChapter(int durChapter) {
        this.durChapter = durChapter;
    }

    public void setDurChapterPage(int durChapterPage) {
        this.durChapterPage = durChapterPage;
    }

    public void setFinalDate(Long finalDate) {
        this.finalDate = finalDate;
    }

    public void setHasUpdate(Boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public void setNewChapters(Integer newChapters) {
        this.newChapters = newChapters;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setFinalRefreshData(Long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public void upDurChapterName() {
        if (realChapterListEmpty()) {
            return;
        }
        if (getChapterListSize() > 0) {
            if (durChapter < getChapterListSize()) {
                durChapterName = getChapterList().get(durChapter).getDurChapterName();
            } else {
                durChapterName = getChapterList().get(getChapterListSize() - 1).getDurChapterName();
            }
        } else {
            durChapterName = "";
        }
    }

    public String getLastChapterName() {
        return this.lastChapterName;
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }

    public void upLastChapterName() {
        if (realChapterListEmpty()) {
            return;
        }

        if (getChapterListSize() > 0) {
            lastChapterName = getChapterList().get(getChapterListSize() - 1).getDurChapterName();
        } else {
            lastChapterName = "";
        }
    }

    public int getChapterListSize() {
        return chapterListSize == null ? 0 : chapterListSize;
    }

    public void setChapterList(List<ChapterListBean> chapterList) {
        this.chapterList = chapterList;
    }

    public void upChapterListSize() {
        chapterListSize = getChapterList().size();
    }

    public List<ChapterListBean> getChapterList() {
        if (chapterList != null) {
            return chapterList;
        }
        return new ArrayList<>();
    }

    public void setBookmarkList(List<BookmarkBean> markList) {
        this.bookmarkList = markList;
    }

    public List<BookmarkBean> getBookmarkList() {
        if (bookmarkList != null) {
            return bookmarkList;
        }
        return new ArrayList<>();
    }

    public int getBookmarkListSize() {
        return getBookmarkList().size();
    }

    public boolean realChapterListEmpty() {
        return getChapterList().isEmpty();
    }

    public boolean realBookmarkListEmpty() {
        return getBookmarkList().isEmpty();
    }

    public void setChapterListSize(Integer chapterListSize) {
        this.chapterListSize = chapterListSize;
    }

    public boolean getUpdateOff() {
        return updateOff == null ? false : updateOff;
    }

    public void setUpdateOff(Boolean updateOff) {
        this.updateOff = updateOff;
    }


    public void setDurChapter(Integer durChapter) {
        this.durChapter = durChapter;
    }


    public void setDurChapterPage(Integer durChapterPage) {
        this.durChapterPage = durChapterPage;
    }
}