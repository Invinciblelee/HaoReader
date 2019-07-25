//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.monke.monkeybook.help.TextProcessor;

import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_SPACE_END;
import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_SPACE_START;

/**
 * 书本缓存内容
 */
public class BookContentBean implements Parcelable {
    private String noteUrl; //对应BookInfoBean noteUrl;

    private String durChapterUrl;

    private int durChapterIndex;   //当前章节  （包括番外）

    private String durChapterName;

    private Boolean isRight = true;

    private StringBuilder durChapterContent;

    public BookContentBean() {

    }

    protected BookContentBean(Parcel in) {
        noteUrl = in.readString();
        durChapterUrl = in.readString();
        durChapterIndex = in.readInt();
        durChapterName = in.readString();
        byte tmpIsRight = in.readByte();
        isRight = tmpIsRight == 0 ? null : tmpIsRight == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteUrl);
        dest.writeString(durChapterUrl);
        dest.writeInt(durChapterIndex);
        dest.writeString(durChapterName);
        dest.writeByte((byte) (isRight == null ? 0 : isRight ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BookContentBean> CREATOR = new Creator<BookContentBean>() {
        @Override
        public BookContentBean createFromParcel(Parcel in) {
            return new BookContentBean(in);
        }

        @Override
        public BookContentBean[] newArray(int size) {
            return new BookContentBean[size];
        }
    };

    public String getDurChapterUrl() {
        return durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public String getDurChapterName() {
        return durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    public String getDurChapterContent() {
        return durChapterContent.toString();
    }

    private void setDurChapterContent(String durChapterContent) {
        if (this.durChapterContent == null) {
            this.durChapterContent = new StringBuilder();
        }
        this.durChapterContent.append(durChapterContent);
        if (durChapterContent == null || durChapterContent.length() == 0)
            this.isRight = false;
    }

    public void appendDurChapterContent(String durChapterContent) {
        if (this.durChapterContent == null) {
            setDurChapterContent(TextProcessor.formatHtml(durChapterContent));
        } else {
            Matcher matcher = PATTERN_SPACE_START.matcher(durChapterContent);
            if (matcher.find()) {
                this.durChapterContent.append("\n").append(TextProcessor.formatHtml(durChapterContent));
            } else {
                String content = this.durChapterContent.toString();
                matcher = PATTERN_SPACE_END.matcher(content);
                if (matcher.find()) {
                    this.durChapterContent.setLength(0);
                    this.durChapterContent.append(matcher.replaceFirst(""));
                }

                content = TextProcessor.formatHtml(durChapterContent);
                matcher = PATTERN_SPACE_START.matcher(content);
                if (matcher.find()) {
                    this.durChapterContent.append(matcher.replaceFirst(""));
                } else {
                    this.durChapterContent.append(content);
                }
            }
        }
    }

    public void appendRawDurChapterContent(String durChapterContent) {
        if (this.durChapterContent == null) {
            setDurChapterContent(durChapterContent);
        } else {
            this.durChapterContent.append("\n").append(durChapterContent);
        }
    }

    public Boolean getRight() {
        return isRight;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "BookContentBean{" +
                "noteUrl='" + noteUrl + '\'' +
                ", durChapterUrl='" + durChapterUrl + '\'' +
                ", durChapterIndex=" + durChapterIndex +
                ", durChapterName='" + durChapterName + '\'' +
                ", durChapterContent='" + durChapterContent + '\'' +
                ", isRight=" + isRight +
                '}';
    }
}
