package com.monke.monkeybook.model.analyzeRule;

import android.os.Bundle;
import android.os.Parcelable;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.VariableStore;

import java.io.Serializable;
import java.util.Map;

public class AnalyzeConfig {
    private String tag;
    private String name;
    private String baseURL;
    private Map<String, String> headerMap;
    private BookSourceBean bookSource;
    private VariableStore variableStore;
    private Bundle extras;

    public AnalyzeConfig newConfig() {
        AnalyzeConfig config = new AnalyzeConfig();
        config.tag = tag;
        config.name = name;
        config.baseURL = baseURL;
        config.bookSource = bookSource;
        return config;
    }

    public AnalyzeConfig apply(AnalyzeConfig config) {
        if (config != null) {
            this.tag = config.tag;
            this.name = config.name;
            this.baseURL = config.baseURL;
            this.headerMap = config.headerMap;
            this.bookSource = config.bookSource;
            this.variableStore = config.variableStore;
            this.extras = config.extras;
        }
        return this;
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

    public AnalyzeConfig variableStore(VariableStore variableStore) {
        this.variableStore = variableStore;
        return this;
    }

    public AnalyzeConfig headerMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
        return this;
    }

    public AnalyzeConfig extras(Bundle extras) {
        this.extras = extras;
        return this;
    }

    public AnalyzeConfig extra(String key, String value) {
        if (this.extras == null) {
            this.extras = new Bundle();
        }
        this.extras.putString(key, value);
        return this;
    }

    public AnalyzeConfig extra(String key, int value) {
        if (this.extras == null) {
            this.extras = new Bundle();
        }
        this.extras.putInt(key, value);
        return this;
    }

    public AnalyzeConfig extra(String key, Serializable value) {
        if (this.extras == null) {
            this.extras = new Bundle();
        }
        this.extras.putSerializable(key, value);
        return this;
    }

    public AnalyzeConfig extra(String key, Parcelable value) {
        if (this.extras == null) {
            this.extras = new Bundle();
        }
        this.extras.putParcelable(key, value);
        return this;
    }

    public final Bundle getExtras() {
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

    public final VariableStore getVariableStore() {
        return variableStore;
    }

    public final Map<String, String> getHeaderMap() {
        return headerMap;
    }
}