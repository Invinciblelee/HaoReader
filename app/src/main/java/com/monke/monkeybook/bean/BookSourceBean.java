package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.monke.monkeybook.help.Constant;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;

/**
 * Created by GKF on 2017/12/14.
 * 书源信息
 */
@Entity
public class BookSourceBean implements Parcelable, Cloneable {
    @Id
    private String bookSourceUrl;
    private String bookSourceName;
    private String bookSourceGroup;
    private String bookSourceType = Constant.BookType.TEXT;
    private String bookSourceRuleType = Constant.RuleType.DEFAULT;
    private String checkUrl;
    private String loginCookieKey;
    @OrderBy
    private int serialNumber;
    @OrderBy
    private int weight = 0;
    private boolean enable;
    private String ruleFindUrl;
    private String ruleSearchUrl;
    private String ruleSearchList;
    private String ruleSearchName;
    private String ruleSearchAuthor;
    private String ruleSearchKind;
    private String ruleSearchLastChapter;
    private String ruleSearchCoverUrl;
    private String ruleSearchNoteUrl;
    private String rulePersistedVariables;
    private String ruleBookName;
    private String ruleBookAuthor;
    private String ruleLastChapter;
    private String ruleChapterUrl;
    private String ruleChapterUrlNext;
    private String ruleCoverUrl;
    private String ruleIntroduce;
    private String ruleChapterList;
    private String ruleChapterName;
    private String ruleContentUrl;
    private String ruleContentUrlNext;
    private String ruleBookContent;
    private String httpUserAgent;

    @Generated(hash = 1989712022)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, String bookSourceGroup, String bookSourceType, String bookSourceRuleType, String checkUrl, String loginCookieKey, int serialNumber, int weight, boolean enable,
            String ruleFindUrl, String ruleSearchUrl, String ruleSearchList, String ruleSearchName, String ruleSearchAuthor, String ruleSearchKind, String ruleSearchLastChapter, String ruleSearchCoverUrl, String ruleSearchNoteUrl,
            String rulePersistedVariables, String ruleBookName, String ruleBookAuthor, String ruleLastChapter, String ruleChapterUrl, String ruleChapterUrlNext, String ruleCoverUrl, String ruleIntroduce, String ruleChapterList,
            String ruleChapterName, String ruleContentUrl, String ruleContentUrlNext, String ruleBookContent, String httpUserAgent) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.bookSourceGroup = bookSourceGroup;
        this.bookSourceType = bookSourceType;
        this.bookSourceRuleType = bookSourceRuleType;
        this.checkUrl = checkUrl;
        this.loginCookieKey = loginCookieKey;
        this.serialNumber = serialNumber;
        this.weight = weight;
        this.enable = enable;
        this.ruleFindUrl = ruleFindUrl;
        this.ruleSearchUrl = ruleSearchUrl;
        this.ruleSearchList = ruleSearchList;
        this.ruleSearchName = ruleSearchName;
        this.ruleSearchAuthor = ruleSearchAuthor;
        this.ruleSearchKind = ruleSearchKind;
        this.ruleSearchLastChapter = ruleSearchLastChapter;
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
        this.rulePersistedVariables = rulePersistedVariables;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleLastChapter = ruleLastChapter;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleChapterUrlNext = ruleChapterUrlNext;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleChapterList = ruleChapterList;
        this.ruleChapterName = ruleChapterName;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleContentUrlNext = ruleContentUrlNext;
        this.ruleBookContent = ruleBookContent;
        this.httpUserAgent = httpUserAgent;
    }

    public BookSourceBean() {
    }


    protected BookSourceBean(Parcel in) {
        bookSourceUrl = in.readString();
        bookSourceName = in.readString();
        bookSourceGroup = in.readString();
        bookSourceType = in.readString();
        bookSourceRuleType = in.readString();
        checkUrl = in.readString();
        loginCookieKey = in.readString();
        serialNumber = in.readInt();
        weight = in.readInt();
        enable = in.readByte() != 0;
        ruleFindUrl = in.readString();
        ruleSearchUrl = in.readString();
        ruleSearchList = in.readString();
        ruleSearchName = in.readString();
        ruleSearchAuthor = in.readString();
        ruleSearchKind = in.readString();
        ruleSearchLastChapter = in.readString();
        ruleSearchCoverUrl = in.readString();
        ruleSearchNoteUrl = in.readString();
        rulePersistedVariables = in.readString();
        ruleBookName = in.readString();
        ruleBookAuthor = in.readString();
        ruleLastChapter = in.readString();
        ruleChapterUrl = in.readString();
        ruleChapterUrlNext = in.readString();
        ruleCoverUrl = in.readString();
        ruleIntroduce = in.readString();
        ruleChapterList = in.readString();
        ruleChapterName = in.readString();
        ruleContentUrl = in.readString();
        ruleContentUrlNext = in.readString();
        ruleBookContent = in.readString();
        httpUserAgent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookSourceUrl);
        dest.writeString(bookSourceName);
        dest.writeString(bookSourceGroup);
        dest.writeString(bookSourceType);
        dest.writeString(bookSourceRuleType);
        dest.writeString(checkUrl);
        dest.writeString(loginCookieKey);
        dest.writeInt(serialNumber);
        dest.writeInt(weight);
        dest.writeByte((byte) (enable ? 1 : 0));
        dest.writeString(ruleFindUrl);
        dest.writeString(ruleSearchUrl);
        dest.writeString(ruleSearchList);
        dest.writeString(ruleSearchName);
        dest.writeString(ruleSearchAuthor);
        dest.writeString(ruleSearchKind);
        dest.writeString(ruleSearchLastChapter);
        dest.writeString(ruleSearchCoverUrl);
        dest.writeString(ruleSearchNoteUrl);
        dest.writeString(rulePersistedVariables);
        dest.writeString(ruleBookName);
        dest.writeString(ruleBookAuthor);
        dest.writeString(ruleLastChapter);
        dest.writeString(ruleChapterUrl);
        dest.writeString(ruleChapterUrlNext);
        dest.writeString(ruleCoverUrl);
        dest.writeString(ruleIntroduce);
        dest.writeString(ruleChapterList);
        dest.writeString(ruleChapterName);
        dest.writeString(ruleContentUrl);
        dest.writeString(ruleContentUrlNext);
        dest.writeString(ruleBookContent);
        dest.writeString(httpUserAgent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookSourceBean> CREATOR = new Creator<BookSourceBean>() {
        @Override
        public BookSourceBean createFromParcel(Parcel in) {
            return new BookSourceBean(in);
        }

        @Override
        public BookSourceBean[] newArray(int size) {
            return new BookSourceBean[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookSourceBean) {
            BookSourceBean bs = (BookSourceBean) obj;
            return stringEquals(bookSourceType, bs.bookSourceType)
                    && stringEquals(bookSourceRuleType, bs.bookSourceRuleType)
                    && stringEquals(bookSourceUrl, bs.bookSourceUrl)
                    && stringEquals(bookSourceName, bs.bookSourceName)
                    && stringEquals(bookSourceGroup, bs.bookSourceGroup)
                    && stringEquals(ruleFindUrl, bs.ruleFindUrl)
                    && stringEquals(ruleBookName, bs.ruleBookName)
                    && stringEquals(ruleBookAuthor, bs.ruleBookAuthor)
                    && stringEquals(ruleLastChapter, bs.ruleLastChapter)
                    && stringEquals(ruleChapterUrl, bs.ruleChapterUrl)
                    && stringEquals(ruleChapterUrlNext, ruleChapterUrlNext)
                    && stringEquals(ruleCoverUrl, bs.ruleCoverUrl)
                    && stringEquals(ruleIntroduce, bs.ruleIntroduce)
                    && stringEquals(ruleChapterList, bs.ruleChapterList)
                    && stringEquals(ruleChapterName, bs.ruleChapterName)
                    && stringEquals(ruleContentUrl, bs.ruleContentUrl)
                    && stringEquals(ruleContentUrlNext, bs.ruleContentUrlNext)
                    && stringEquals(ruleBookContent, bs.ruleBookContent)
                    && stringEquals(ruleSearchUrl, bs.ruleSearchUrl)
                    && stringEquals(ruleSearchList, bs.ruleSearchList)
                    && stringEquals(ruleSearchName, bs.ruleSearchName)
                    && stringEquals(ruleSearchAuthor, bs.ruleSearchAuthor)
                    && stringEquals(ruleSearchKind, bs.ruleSearchKind)
                    && stringEquals(ruleSearchLastChapter, bs.ruleSearchLastChapter)
                    && stringEquals(ruleSearchCoverUrl, bs.ruleSearchCoverUrl)
                    && stringEquals(ruleSearchNoteUrl, bs.ruleSearchNoteUrl)
                    && stringEquals(httpUserAgent, bs.httpUserAgent)
                    && stringEquals(checkUrl, bs.checkUrl)
                    && stringEquals(loginCookieKey, bs.loginCookieKey)
                    && stringEquals(rulePersistedVariables, bs.rulePersistedVariables);
        }
        return false;
    }

    private Boolean stringEquals(String str1, String str2) {
        return (TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)) || TextUtils.equals(str1, str2);
    }

    @Override
    public BookSourceBean clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookSourceBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getBookSourceName() {
        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceType() {
        return bookSourceType;
    }

    public void setBookSourceType(@Constant.BookType String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }

    public String getBookSourceRuleType() {
        return bookSourceRuleType;
    }

    public void setBookSourceRuleType(@Constant.RuleType String bookSourceRuleType) {
        this.bookSourceRuleType = bookSourceRuleType;
    }

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getRuleBookName() {
        return this.ruleBookName;
    }

    public void setRuleBookName(String ruleBookName) {
        this.ruleBookName = ruleBookName;
    }

    public String getRuleBookAuthor() {
        return this.ruleBookAuthor;
    }

    public void setRuleBookAuthor(String ruleBookAutoher) {
        this.ruleBookAuthor = ruleBookAutoher;
    }

    public String getRuleLastChapter() {
        return ruleLastChapter;
    }

    public void setRuleLastChapter(String ruleLastChapter) {
        this.ruleLastChapter = ruleLastChapter;
    }

    public String getRuleChapterUrl() {
        return this.ruleChapterUrl;
    }

    public void setRuleChapterUrl(String ruleChapterUrl) {
        this.ruleChapterUrl = ruleChapterUrl;
    }

    public String getRuleCoverUrl() {
        return this.ruleCoverUrl;
    }

    public void setRuleCoverUrl(String ruleCoverUrl) {
        this.ruleCoverUrl = ruleCoverUrl;
    }

    public String getRuleIntroduce() {
        return this.ruleIntroduce;
    }

    public void setRuleIntroduce(String ruleIntroduce) {
        this.ruleIntroduce = ruleIntroduce;
    }

    public String getRuleBookContent() {
        return this.ruleBookContent;
    }

    public void setRuleBookContent(String ruleBookContent) {
        this.ruleBookContent = ruleBookContent;
    }

    public String getRuleSearchUrl() {
        return this.ruleSearchUrl;
    }

    public void setRuleSearchUrl(String ruleSearchUrl) {
        this.ruleSearchUrl = ruleSearchUrl;
    }

    public String getRuleContentUrl() {
        return this.ruleContentUrl;
    }

    public void setRuleContentUrl(String ruleContentUrl) {
        this.ruleContentUrl = ruleContentUrl;
    }

    public String getRuleSearchName() {
        return this.ruleSearchName;
    }

    public void setRuleSearchName(String ruleSearchName) {
        this.ruleSearchName = ruleSearchName;
    }

    public String getRuleSearchAuthor() {
        return this.ruleSearchAuthor;
    }

    public void setRuleSearchAuthor(String ruleSearchAuthor) {
        this.ruleSearchAuthor = ruleSearchAuthor;
    }

    public String getRuleSearchKind() {
        return this.ruleSearchKind;
    }

    public void setRuleSearchKind(String ruleSearchKind) {
        this.ruleSearchKind = ruleSearchKind;
    }

    public String getRuleSearchLastChapter() {
        return this.ruleSearchLastChapter;
    }

    public void setRuleSearchLastChapter(String ruleSearchLastChapter) {
        this.ruleSearchLastChapter = ruleSearchLastChapter;
    }

    public String getRuleSearchCoverUrl() {
        return this.ruleSearchCoverUrl;
    }

    public void setRuleSearchCoverUrl(String ruleSearchCoverUrl) {
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
    }

    public String getRuleSearchNoteUrl() {
        return this.ruleSearchNoteUrl;
    }

    public void setRuleSearchNoteUrl(String ruleSearchNoteUrl) {
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
    }

    public String getRuleSearchList() {
        return this.ruleSearchList;
    }

    public void setRuleSearchList(String ruleSearchList) {
        this.ruleSearchList = ruleSearchList;
    }

    public String getRuleChapterList() {
        return this.ruleChapterList;
    }

    public void setRuleChapterList(String ruleChapterList) {
        this.ruleChapterList = ruleChapterList;
    }

    public String getRuleChapterName() {
        return this.ruleChapterName;
    }

    public void setRuleChapterName(String ruleChapterName) {
        this.ruleChapterName = ruleChapterName;
    }

    public String getHttpUserAgent() {
        return this.httpUserAgent;
    }

    public void setHttpUserAgent(String httpHeaders) {
        this.httpUserAgent = httpHeaders;
    }

    public String getRuleFindUrl() {
        return this.ruleFindUrl;
    }

    public void setRuleFindUrl(String ruleFindUrl) {
        this.ruleFindUrl = ruleFindUrl;
    }

    public String getBookSourceGroup() {
        return this.bookSourceGroup;
    }

    public void setBookSourceGroup(String bookSourceGroup) {
        this.bookSourceGroup = bookSourceGroup;
    }

    public String getCheckUrl() {
        return this.checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getLoginCookieKey() {
        return loginCookieKey;
    }

    public void setLoginCookieKey(String loginCookieKey) {
        this.loginCookieKey = loginCookieKey;
    }

    public String getRuleChapterUrlNext() {
        return this.ruleChapterUrlNext;
    }

    public void setRuleChapterUrlNext(String ruleChapterUrlNext) {
        this.ruleChapterUrlNext = ruleChapterUrlNext;
    }

    public String getRuleContentUrlNext() {
        return this.ruleContentUrlNext;
    }

    public void setRuleContentUrlNext(String ruleContentUrlNext) {
        this.ruleContentUrlNext = ruleContentUrlNext;
    }

    public String getRulePersistedVariables() {
        return rulePersistedVariables;
    }

    public void setRulePersistedVariables(String rulePersistedVariables) {
        this.rulePersistedVariables = rulePersistedVariables;
    }

    public int getWeight() {
        return weight;
    }

    // 换源时选择的源权重+500
    public void increaseWeightBySelection() {
        this.weight += 500;
    }

    public void increaseWeight(int increase) {
        this.weight += increase;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "BookSourceBean{" +
                "bookSourceUrl='" + bookSourceUrl + '\'' +
                ", bookSourceName='" + bookSourceName + '\'' +
                ", bookSourceGroup='" + bookSourceGroup + '\'' +
                ", bookSourceType='" + bookSourceType + '\'' +
                ", bookSourceRuleType='" + bookSourceRuleType + '\'' +
                ", checkUrl='" + checkUrl + '\'' +
                ", loginCookieKey='" + loginCookieKey + '\'' +
                ", serialNumber=" + serialNumber +
                ", weight=" + weight +
                ", enable=" + enable +
                ", ruleFindUrl='" + ruleFindUrl + '\'' +
                ", ruleSearchUrl='" + ruleSearchUrl + '\'' +
                ", ruleSearchList='" + ruleSearchList + '\'' +
                ", ruleSearchName='" + ruleSearchName + '\'' +
                ", ruleSearchAuthor='" + ruleSearchAuthor + '\'' +
                ", ruleSearchKind='" + ruleSearchKind + '\'' +
                ", ruleSearchLastChapter='" + ruleSearchLastChapter + '\'' +
                ", ruleSearchCoverUrl='" + ruleSearchCoverUrl + '\'' +
                ", ruleSearchNoteUrl='" + ruleSearchNoteUrl + '\'' +
                ", rulePersistedVariables='" + rulePersistedVariables + '\'' +
                ", ruleBookName='" + ruleBookName + '\'' +
                ", ruleBookAuthor='" + ruleBookAuthor + '\'' +
                ", ruleLastChapter='" + ruleLastChapter + '\'' +
                ", ruleChapterUrl='" + ruleChapterUrl + '\'' +
                ", ruleChapterUrlNext='" + ruleChapterUrlNext + '\'' +
                ", ruleCoverUrl='" + ruleCoverUrl + '\'' +
                ", ruleIntroduce='" + ruleIntroduce + '\'' +
                ", ruleChapterList='" + ruleChapterList + '\'' +
                ", ruleChapterName='" + ruleChapterName + '\'' +
                ", ruleContentUrl='" + ruleContentUrl + '\'' +
                ", ruleContentUrlNext='" + ruleContentUrlNext + '\'' +
                ", ruleBookContent='" + ruleBookContent + '\'' +
                ", httpUserAgent='" + httpUserAgent + '\'' +
                '}';
    }
}
