package com.monke.monkeybook.model.analyzeRule;

import android.os.Bundle;

import com.monke.monkeybook.bean.BookSourceBean;

public class AnalyzeConfig {
    private String tag;
    private String name;
    private String baseURL;
    private BookSourceBean bookSource;
    private Bundle extras;

    public AnalyzeConfig newConfig() {
        AnalyzeConfig config = new AnalyzeConfig();
        config.tag = tag;
        config.name = name;
        config.baseURL = baseURL;
        config.bookSource = bookSource;
        return config;
    }

    public AnalyzeConfig tag(String tag) {
        this.tag = tag;
        return this;
    }

    public AnalyzeConfig name(String name) {
        this.name = name;
        return this;
    }

    public AnalyzeConfig baseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    public AnalyzeConfig bookSource(BookSourceBean bookSource) {
        this.bookSource = bookSource;
        return this;
    }

    public AnalyzeConfig extras(Bundle extras){
        this.extras = extras;
        return this;
    }

    public final Bundle getExtras(){
        return extras;
    }

    public final String getTag() {
        return tag;
    }

    public final String getName() {
        return name;
    }

    public final String getBaseURL() {
        return baseURL;
    }

    public final BookSourceBean getBookSource() {
        return bookSource;
    }
}