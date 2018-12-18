package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookSourceBean;


public class AudioBookChapterUrl {
    private String audioType;
    private String javaScript;

    private boolean isAJAX;

    public AudioBookChapterUrl(BookSourceBean bookSourceBean) {
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

    public boolean checkChapterUrl(String chapterUrl) {
        return chapterUrl != null && chapterUrl.endsWith(audioType);
    }

    public boolean isAJAX() {
        return isAJAX;
    }

    public String getJavaScript() {
        return javaScript;
    }
}
