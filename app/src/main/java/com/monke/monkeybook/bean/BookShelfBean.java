//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.monke.monkeybook.help.TextProcessor;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 书架item Bean
 */

@Entity
public class BookShelfBean implements Parcelable, VariableStore {
    @Transient
    public static final String LOCAL_TAG = "loc_book";

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
    private Integer group = -1;
    private String durChapterName;
    private String lastChapterName;
    private Integer chapterListSize = 0;
    private Boolean updateOff = false; //禁止更新
    private String variableString;

    @Transient
    private final VariableStoreImpl variableStore = new VariableStoreImpl();
    @Transient
    private BookInfoBean bookInfoBean = new BookInfoBean();
    @Transient
    private List<ChapterBean> chapterList = new ArrayList<>();    //章节列表
    @Transient
    private List<BookmarkBean> bookmarkList = new ArrayList<>();    //书签列表
    @Transient
    private boolean flag;

    public BookShelfBean() {
    }


    @Generated(hash = 228430457)
    public BookShelfBean(String noteUrl, Integer durChapter, Integer durChapterPage, Long finalDate, Boolean hasUpdate, Integer newChapters,
                         String tag, Integer serialNumber, Long finalRefreshData, Integer group, String durChapterName, String lastChapterName,
                         Integer chapterListSize, Boolean updateOff, String variableString) {
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
        this.variableString = variableString;
    }


    protected BookShelfBean(Parcel in) {
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
        variableString = in.readString();
        bookInfoBean = in.readParcelable(BookInfoBean.class.getClassLoader());
        chapterList = in.createTypedArrayList(ChapterBean.CREATOR);
        bookmarkList = in.createTypedArrayList(BookmarkBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
        dest.writeString(variableString);
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
        bookShelfBean.setVariableString(variableString);
        bookShelfBean.bookInfoBean = bookInfoBean.copy();
        if (chapterList != null) {
            for (ChapterBean aChapterList : chapterList) {
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
        return this.durChapter == null ? 0 : this.durChapter;
    }

    public ChapterBean getChapter(int index) {
        if (realChapterListEmpty() || index < 0) {
            return new ChapterBean();
        } else if (index < getChapterList().size()) {
            return getChapterList().get(index);
        } else {
            return getChapterList().get(getChapterList().size() - 1);
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

    public boolean isLocalBook() {
        return LOCAL_TAG.equals(tag);
    }

    public BookInfoBean getBookInfoBean() {
        if (bookInfoBean == null) {
            bookInfoBean = new BookInfoBean();
        }
        return bookInfoBean;
    }

    public int getUnreadChapterNum() {
        int num = getChapterListSize() - getDurChapter() - 1;
        return num < 0 ? 0 : num;
    }

    public void setBookInfoBean(BookInfoBean bookInfoBean) {
        this.bookInfoBean = bookInfoBean;
    }

    public boolean getHasUpdate() {
        return hasUpdate == null ? false : hasUpdate;
    }

    public int getNewChapters() {
        return newChapters == null ? 0 : newChapters;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public long getFinalRefreshData() {
        return this.finalRefreshData;
    }

    public boolean isFlag() {
        return flag;
    }

    public BookShelfBean withFlag(boolean flag) {
        this.flag = flag;
        return this;
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
        return TextProcessor.formatChapterName(this.durChapterName);
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public String getLastChapterName() {
        return TextProcessor.formatChapterName(this.lastChapterName);
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }

    public void upDurChapterName() {
        this.durChapter = Math.max(0, this.durChapter);
        if (!realChapterListEmpty()) {
            this.durChapter = Math.min(this.durChapter, getChapterList().size() - 1);
            this.durChapterName = getChapterList().get(this.durChapter).getDurChapterName();
        } else {
            this.durChapterName = "";
        }
    }

    public void upLastChapterName() {
        if (!realChapterListEmpty()) {
            this.lastChapterName = getChapterList().get(getChapterList().size() - 1).getDurChapterName();
        } else {
            this.lastChapterName = "";
        }
    }

    public int getChapterListSize() {
        return chapterListSize == null ? 0 : chapterListSize;
    }

    public void setChapterList(List<ChapterBean> chapterList) {
        setChapterList(chapterList, true);
    }

    public void setChapterList(List<ChapterBean> chapterList, boolean updateSize) {
        this.chapterList = chapterList;
        if (!realChapterListEmpty()) {
            Collections.sort(this.chapterList);
        }
        if (updateSize) {
            this.chapterListSize = getChapterList().size();
        }
    }

    public List<ChapterBean> getChapterList() {
        if (chapterList == null) {
            chapterList = new ArrayList<>();
        }
        return chapterList;
    }

    public void setBookmarkList(List<BookmarkBean> markList) {
        this.bookmarkList = markList;
    }

    public List<BookmarkBean> getBookmarkList() {
        if (bookmarkList == null) {
            bookmarkList = new ArrayList<>();
        }
        return bookmarkList;
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

    @Override
    public String getVariableString() {
        return this.variableString;
    }

    @Override
    public void setVariableString(String variableString) {
        this.variableString = variableString;
        variableStore.setVariableString(variableString);
    }

    @Override
    public Map<String, String> getVariableMap() {
        return variableStore.getVariableMap();
    }

    @Override
    public void putVariableMap(Map<String, String> variableMap) {
        variableStore.putVariableMap(variableMap);
        String variableString = variableStore.getVariableString();
        if (variableString != null) {
            this.variableString = variableString;
        }
    }

    @Override
    public void putVariable(String key, String value) {
        variableStore.putVariable(key, value);
        String variableString = variableStore.getVariableString();
        if (variableString != null) {
            this.variableString = variableString;
        }
    }

    @Override
    public String getVariable(String key) {
        return variableStore.getVariable(key);
    }
}