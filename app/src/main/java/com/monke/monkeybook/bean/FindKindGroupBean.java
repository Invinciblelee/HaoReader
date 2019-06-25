package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.monke.monkeybook.utils.ObjectsCompat;

import java.util.List;

public class FindKindGroupBean implements Parcelable {
    private String groupName;
    private String tag;
    private int childrenCount;
    private List<FindKindBean> children;
    private boolean isExpand;

    private List<SearchBookBean> books;

    public FindKindGroupBean() {
    }

    protected FindKindGroupBean(Parcel in) {
        groupName = in.readString();
        tag = in.readString();
        childrenCount = in.readInt();
        children = in.createTypedArrayList(FindKindBean.CREATOR);
        isExpand = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(tag);
        dest.writeInt(childrenCount);
        dest.writeTypedList(children);
        dest.writeByte((byte) (isExpand ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FindKindGroupBean> CREATOR = new Creator<FindKindGroupBean>() {
        @Override
        public FindKindGroupBean createFromParcel(Parcel in) {
            return new FindKindGroupBean(in);
        }

        @Override
        public FindKindGroupBean[] newArray(int size) {
            return new FindKindGroupBean[size];
        }
    };

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<FindKindBean> getChildren() {
        return children;
    }

    public void setChildren(List<FindKindBean> children) {
        this.children = children;
    }

    public List<SearchBookBean> getBooks() {
        return books;
    }

    public void setBooks(List<SearchBookBean> books) {
        this.books = books;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof FindKindGroupBean) {
            return ObjectsCompat.equals(((FindKindGroupBean) obj).tag, tag);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hashCode(tag);
    }
}
