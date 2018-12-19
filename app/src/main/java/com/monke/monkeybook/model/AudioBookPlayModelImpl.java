package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.AudioSniffer;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;

public class AudioBookPlayModelImpl implements IAudioBookPlayModel {

    private String mTag;
    private BookSourceBean mBookSource;
    private AudioSniffer mSniffer;

    public AudioBookPlayModelImpl(String tag) {
        ensureBookSource(tag);
    }

    @Override
    public void getChapterList(BookShelfBean bookShelfBean) {
    }

    @Override
    public void ensureChapterUrl(ChapterListBean chapter) {
        ensureBookSource(chapter.getTag());
        if(mBookSource == null){
            return;
        }
    }

    @Override
    public void closeSniffer() {
        mSniffer.destroy();
    }

    private void ensureBookSource(String tag){
        if(TextUtils.isEmpty(tag)){
            return;
        }

        if(mBookSource == null || !TextUtils.equals(mTag, tag) ){
            mTag = tag;
            mBookSource = BookSourceManager.getInstance().getBookSourceByTag(mTag);
        }
    }

    private class AudioBookChapterRule {
        private String audioType;
        private String javaScript;

        private boolean isAJAX;

        private AudioBookChapterRule(BookSourceBean bookSourceBean) {
            String ruleBookContent = bookSourceBean.getRuleBookContent();
            if (ruleBookContent != null) {
                if (ruleBookContent.startsWith("^")) {
                    this.audioType = ruleBookContent.substring(1);
                } else if (ruleBookContent.startsWith("$")) {
                    isAJAX = true;

                    String[] rules = ruleBookContent.split("@js:");
                    if (rules.length > 1) {
                        this.audioType = rules[0].substring(1);
                        this.javaScript = rules[1];
                    } else {
                        this.audioType = ruleBookContent.substring(1);
                    }
                }
            }
        }

        private boolean checkChapterUrl(String chapterUrl) {
            return chapterUrl != null && chapterUrl.endsWith(audioType);
        }

        private boolean isAJAX() {
            return isAJAX;
        }

        private String getJavaScript() {
            return javaScript;
        }
    }
}
