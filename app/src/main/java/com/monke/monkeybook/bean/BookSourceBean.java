package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.model.annotation.RuleType;
import com.monke.monkeybook.utils.StringUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Arrays;

import static com.monke.monkeybook.help.Constant.BOOK_TYPES;
import static com.monke.monkeybook.help.Constant.RULE_TYPES;

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
    private String bookSourceType = BookType.TEXT;
    private String bookSourceRuleType = RuleType.DEFAULT;
    private String checkUrl;
    @OrderBy
    private int serialNumber;
    @OrderBy
    private int weight = 0;
    private Boolean enable = true;
    private Boolean enableFind = true;
    private String ruleFindUrl;
    //搜索
    private String ruleSearchUrl;
    private String ruleSearchList;
    private String ruleSearchName;
    private String ruleSearchAuthor;
    private String ruleSearchKind;
    private String ruleSearchLastChapter;
    private String ruleSearchIntroduce;
    private String ruleSearchCoverUrl;
    private String ruleSearchNoteUrl;
    //详情
    private String ruleBookName;
    private String ruleBookAuthor;
    private String ruleBookLastChapter;
    private String ruleCoverUrl;
    private String ruleIntroduce;
    private String ruleChapterUrl;
    private String ruleChapterUrlNext;
    //目录
    private String ruleChapterList;
    private String ruleChapterName;
    private String ruleContentUrl;
    //正文
    private String ruleContentUrlNext;
    private String ruleBookContent;
    //其他
    private String rulePersistedVariables;
    private String httpUserAgent;

    @Transient
    private String ajaxJavaScript;

    @Generated(hash = 1372865890)
    public BookSourceBean(String bookSourceUrl, String bookSourceName, String bookSourceGroup, String bookSourceType, String bookSourceRuleType, String checkUrl, int serialNumber, int weight, Boolean enable, Boolean enableFind, String ruleFindUrl, String ruleSearchUrl, String ruleSearchList,
            String ruleSearchName, String ruleSearchAuthor, String ruleSearchKind, String ruleSearchLastChapter, String ruleSearchIntroduce, String ruleSearchCoverUrl, String ruleSearchNoteUrl, String ruleBookName, String ruleBookAuthor, String ruleBookLastChapter, String ruleCoverUrl,
            String ruleIntroduce, String ruleChapterUrl, String ruleChapterUrlNext, String ruleChapterList, String ruleChapterName, String ruleContentUrl, String ruleContentUrlNext, String ruleBookContent, String rulePersistedVariables, String httpUserAgent) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.bookSourceGroup = bookSourceGroup;
        this.bookSourceType = bookSourceType;
        this.bookSourceRuleType = bookSourceRuleType;
        this.checkUrl = checkUrl;
        this.serialNumber = serialNumber;
        this.weight = weight;
        this.enable = enable;
        this.enableFind = enableFind;
        this.ruleFindUrl = ruleFindUrl;
        this.ruleSearchUrl = ruleSearchUrl;
        this.ruleSearchList = ruleSearchList;
        this.ruleSearchName = ruleSearchName;
        this.ruleSearchAuthor = ruleSearchAuthor;
        this.ruleSearchKind = ruleSearchKind;
        this.ruleSearchLastChapter = ruleSearchLastChapter;
        this.ruleSearchIntroduce = ruleSearchIntroduce;
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleBookLastChapter = ruleBookLastChapter;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleChapterUrlNext = ruleChapterUrlNext;
        this.ruleChapterList = ruleChapterList;
        this.ruleChapterName = ruleChapterName;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleContentUrlNext = ruleContentUrlNext;
        this.ruleBookContent = ruleBookContent;
        this.rulePersistedVariables = rulePersistedVariables;
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
        serialNumber = in.readInt();
        weight = in.readInt();
        byte tmpEnable = in.readByte();
        enable = tmpEnable == 0 ? null : tmpEnable == 1;
        byte tmpShowFind = in.readByte();
        enableFind = tmpShowFind == 0 ? null : tmpShowFind == 1;
        ruleFindUrl = in.readString();
        ruleSearchUrl = in.readString();
        ruleSearchList = in.readString();
        ruleSearchName = in.readString();
        ruleSearchAuthor = in.readString();
        ruleSearchKind = in.readString();
        ruleSearchLastChapter = in.readString();
        ruleSearchIntroduce = in.readString();
        ruleSearchCoverUrl = in.readString();
        ruleSearchNoteUrl = in.readString();
        ruleBookName = in.readString();
        ruleBookAuthor = in.readString();
        ruleBookLastChapter = in.readString();
        ruleCoverUrl = in.readString();
        ruleIntroduce = in.readString();
        ruleChapterUrl = in.readString();
        ruleChapterUrlNext = in.readString();
        ruleChapterList = in.readString();
        ruleChapterName = in.readString();
        ruleContentUrl = in.readString();
        ruleContentUrlNext = in.readString();
        ruleBookContent = in.readString();
        rulePersistedVariables = in.readString();
        httpUserAgent = in.readString();
        ajaxJavaScript = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookSourceUrl);
        dest.writeString(bookSourceName);
        dest.writeString(bookSourceGroup);
        dest.writeString(bookSourceType);
        dest.writeString(bookSourceRuleType);
        dest.writeString(checkUrl);
        dest.writeInt(serialNumber);
        dest.writeInt(weight);
        dest.writeByte((byte) (enable == null ? 0 : enable ? 1 : 2));
        dest.writeByte((byte) (enableFind == null ? 0 : enableFind ? 1 : 2));
        dest.writeString(ruleFindUrl);
        dest.writeString(ruleSearchUrl);
        dest.writeString(ruleSearchList);
        dest.writeString(ruleSearchName);
        dest.writeString(ruleSearchAuthor);
        dest.writeString(ruleSearchKind);
        dest.writeString(ruleSearchLastChapter);
        dest.writeString(ruleSearchIntroduce);
        dest.writeString(ruleSearchCoverUrl);
        dest.writeString(ruleSearchNoteUrl);
        dest.writeString(ruleBookName);
        dest.writeString(ruleBookAuthor);
        dest.writeString(ruleBookLastChapter);
        dest.writeString(ruleCoverUrl);
        dest.writeString(ruleIntroduce);
        dest.writeString(ruleChapterUrl);
        dest.writeString(ruleChapterUrlNext);
        dest.writeString(ruleChapterList);
        dest.writeString(ruleChapterName);
        dest.writeString(ruleContentUrl);
        dest.writeString(ruleContentUrlNext);
        dest.writeString(ruleBookContent);
        dest.writeString(rulePersistedVariables);
        dest.writeString(httpUserAgent);
        dest.writeString(ajaxJavaScript);
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
                    && stringEquals(ruleBookLastChapter, bs.ruleBookLastChapter)
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
                    && stringEquals(ruleSearchIntroduce, bs.ruleSearchIntroduce)
                    && stringEquals(ruleSearchCoverUrl, bs.ruleSearchCoverUrl)
                    && stringEquals(ruleSearchNoteUrl, bs.ruleSearchNoteUrl)
                    && stringEquals(httpUserAgent, bs.httpUserAgent)
                    && stringEquals(checkUrl, bs.checkUrl)
                    && stringEquals(rulePersistedVariables, bs.rulePersistedVariables)
                    && enable == bs.enable
                    && enableFind == bs.enableFind;

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
        } catch (Exception ignore) {
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

    public void setBookSourceType(@BookType String bookSourceType) {
        if (!Arrays.asList(BOOK_TYPES).contains(StringUtils.nonNull(bookSourceType))) {
            bookSourceType = BookType.TEXT;
        }
        this.bookSourceType = bookSourceType;
    }

    public String getBookSourceRuleType() {
        return bookSourceRuleType;
    }

    public void setBookSourceRuleType(@RuleType String bookSourceRuleType) {
        if (!Arrays.asList(RULE_TYPES).contains(StringUtils.nonNull(bookSourceRuleType))) {
            bookSourceRuleType = RuleType.DEFAULT;
        }
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

    public Boolean getEnable() {
        return this.enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getEnableFind() {
        return this.enableFind;
    }

    public void setEnableFind(Boolean enableFind) {
        this.enableFind = enableFind;
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

    public String getRuleBookLastChapter() {
        return ruleBookLastChapter;
    }

    public void setRuleBookLastChapter(String ruleBookLastChapter) {
        this.ruleBookLastChapter = ruleBookLastChapter;
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

    public String getRealRuleBookContent() {
        if (sniffRuleBookContent()) {
            String[] rules = ruleBookContent.split(AnalyzeGlobal.REGEX_OPERATOR);
            if (rules.length > 1) {
                return rules[0].substring(2);
            }
            return ruleBookContent.substring(2);
        }
        if (ajaxRuleBookContent()) {
            return ruleBookContent.substring(1);
        }
        return ruleBookContent;
    }

    public String getAjaxJavaScript() {
        if (ajaxJavaScript == null) {
            String[] rules = ruleBookContent.split(AnalyzeGlobal.REGEX_OPERATOR);
            if (rules.length > 1) {
                ajaxJavaScript = rules[1];
            }
        }
        return ajaxJavaScript;
    }

    public boolean ajaxRuleBookContent() {
        if (TextUtils.equals(bookSourceRuleType, RuleType.JSON)
                || StringUtils.startWithIgnoreCase(ruleBookContent, AnalyzeGlobal.RULE_JSON)
                || StringUtils.startWithIgnoreCase(ruleBookContent, AnalyzeGlobal.RULE_JSON_TRAIT)) {
            return false;
        }
        return !TextUtils.isEmpty(ruleBookContent) && ruleBookContent.startsWith(AnalyzeGlobal.RULE_AJAX);
    }

    public boolean sniffRuleBookContent() {
        if (TextUtils.equals(bookSourceRuleType, RuleType.JSON)
                || StringUtils.startWithIgnoreCase(ruleBookContent, AnalyzeGlobal.RULE_JSON)
                || StringUtils.startWithIgnoreCase(ruleBookContent, AnalyzeGlobal.RULE_JSON_TRAIT)) {
            return false;
        }
        return !TextUtils.isEmpty(ruleBookContent) && ruleBookContent.startsWith(AnalyzeGlobal.RULE_SNIFF);
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

    public boolean ajaxSearchList() {
        if (TextUtils.equals(bookSourceRuleType, RuleType.JSON)
                || StringUtils.startWithIgnoreCase(ruleSearchUrl, AnalyzeGlobal.RULE_JSON)
                || StringUtils.startWithIgnoreCase(ruleSearchUrl, AnalyzeGlobal.RULE_JSON_TRAIT)) {
            return false;
        }
        return !TextUtils.isEmpty(ruleSearchUrl) && ruleSearchUrl.startsWith(AnalyzeGlobal.RULE_AJAX);
    }

    public String getRealRuleSearchUrl() {
        if (ajaxSearchList()) {
            return ruleSearchUrl.substring(1);
        }
        return ruleSearchUrl;
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

    public String getRuleSearchIntroduce() {
        return ruleSearchIntroduce;
    }

    public void setRuleSearchIntroduce(String ruleSearchIntroduce) {
        this.ruleSearchIntroduce = ruleSearchIntroduce;
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

    public String getRealRuleSearchList() {
        final String searchListRule;
        if (searchListInWhole() || searchListInRegex()) {
            searchListRule = ruleSearchList.substring(7);
        } else {
            searchListRule = ruleSearchList;
        }
        return searchListReverse() ? searchListRule.substring(1) : searchListRule;
    }

    public boolean searchListReverse() {
        return !TextUtils.isEmpty(ruleSearchList) && ruleSearchList.startsWith(AnalyzeGlobal.RULE_REVERSE);
    }

    public boolean searchListInWhole() {
        if (TextUtils.isEmpty(ruleSearchList)) {
            return false;
        }
        return StringUtils.startWithIgnoreCase(ruleSearchList, AnalyzeGlobal.RULE_IN_WHOLE);
    }

    public boolean searchListInRegex() {
        if (TextUtils.isEmpty(ruleSearchList)) {
            return false;
        }
        return StringUtils.startWithIgnoreCase(ruleSearchList, AnalyzeGlobal.RULE_IN_REGEX);
    }

    public void setRuleSearchList(String ruleSearchList) {
        this.ruleSearchList = ruleSearchList;
    }

    public String getRuleChapterList() {
        return this.ruleChapterList;
    }

    public String getRealRuleChapterList() {
        final String chapterListRule;
        if (chapterListInWhole() || chapterListInRegex()) {
            chapterListRule = ruleChapterList.substring(7);
        } else {
            chapterListRule = ruleChapterList;
        }
        return chapterListReverse() ? chapterListRule.substring(1) : chapterListRule;
    }

    public boolean chapterListReverse() {
        if (TextUtils.isEmpty(ruleChapterList)) {
            return false;
        }
        if (chapterListInWhole() || chapterListInRegex()) {
            return ruleChapterList.startsWith(AnalyzeGlobal.RULE_REVERSE, 7);
        }
        return ruleChapterList.startsWith(AnalyzeGlobal.RULE_REVERSE);
    }

    public boolean chapterListInWhole() {
        if (TextUtils.isEmpty(ruleChapterList)) {
            return false;
        }
        return StringUtils.startWithIgnoreCase(ruleChapterList, AnalyzeGlobal.RULE_IN_WHOLE);
    }

    public boolean chapterListInRegex() {
        if (TextUtils.isEmpty(ruleChapterList)) {
            return false;
        }
        return StringUtils.startWithIgnoreCase(ruleChapterList, AnalyzeGlobal.RULE_IN_REGEX);
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
                ", serialNumber=" + serialNumber +
                ", weight=" + weight +
                ", enable=" + enable +
                ", enableFind=" + enableFind +
                ", ruleFindUrl='" + ruleFindUrl + '\'' +
                ", ruleSearchUrl='" + ruleSearchUrl + '\'' +
                ", ruleSearchList='" + ruleSearchList + '\'' +
                ", ruleSearchName='" + ruleSearchName + '\'' +
                ", ruleSearchAuthor='" + ruleSearchAuthor + '\'' +
                ", ruleSearchKind='" + ruleSearchKind + '\'' +
                ", ruleSearchLastChapter='" + ruleSearchLastChapter + '\'' +
                ", ruleSearchIntroduce='" + ruleSearchIntroduce + '\'' +
                ", ruleSearchCoverUrl='" + ruleSearchCoverUrl + '\'' +
                ", ruleSearchNoteUrl='" + ruleSearchNoteUrl + '\'' +
                ", ruleBookName='" + ruleBookName + '\'' +
                ", ruleBookAuthor='" + ruleBookAuthor + '\'' +
                ", ruleBookLastChapter='" + ruleBookLastChapter + '\'' +
                ", ruleCoverUrl='" + ruleCoverUrl + '\'' +
                ", ruleIntroduce='" + ruleIntroduce + '\'' +
                ", ruleChapterUrl='" + ruleChapterUrl + '\'' +
                ", ruleChapterUrlNext='" + ruleChapterUrlNext + '\'' +
                ", ruleChapterList='" + ruleChapterList + '\'' +
                ", ruleChapterName='" + ruleChapterName + '\'' +
                ", ruleContentUrl='" + ruleContentUrl + '\'' +
                ", ruleContentUrlNext='" + ruleContentUrlNext + '\'' +
                ", ruleBookContent='" + ruleBookContent + '\'' +
                ", rulePersistedVariables='" + rulePersistedVariables + '\'' +
                ", httpUserAgent='" + httpUserAgent + '\'' +
                ", ajaxJavaScript='" + ajaxJavaScript + '\'' +
                '}';
    }
}
