package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public abstract class OutAnalyzer<S, T> implements IAnalyzerPresenter, ContentDelegate {

    private final JSParser mJSParser = new JSParser();

    private AnalyzeConfig mConfig;

    public final AnalyzeConfig getConfig() {
        return mConfig;
    }

    public final JSParser getJSParser() {
        return mJSParser;
    }

    public final AnalyzeConfig newConfig() {
        if (mConfig == null) {
            return new AnalyzeConfig();
        } else {
            return mConfig.newConfig();
        }
    }

    public final void apply(@NonNull AnalyzeConfig config) {
        this.mConfig = config;
    }

    public void setContent(String source) {
        getParser().setContent(source);
    }

    public void setContent(T source) {
        getParser().setContent(source);
    }

    public void beginExecute() {
        mJSParser.start();
    }

    public void endExecute() {
        mJSParser.stop();
    }

    abstract SourceParser<S, T> getParser();

    abstract IAnalyzerPresenter getPresenter();

    abstract ContentDelegate getDelegate();

    @Override
    public String getResultContent(String rule) {
        return getPresenter().getResultContent(rule);
    }

    @Override
    public String getResultUrl(String rule) {
        return getPresenter().getResultUrl(rule);
    }

    @Override
    public List<String> getResultContents(String rule) {
        return getPresenter().getResultContents(rule);
    }

    @Override
    public Map<String, String> getVariableMap(String rule) {
        return getPresenter().getVariableMap(rule);
    }

    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        return getPresenter().getRawCollection(rule);
    }

    @Override
    public String parseResultContent(String source, String rule) {
        return getPresenter().parseResultContent(source, rule);
    }

    @Override
    public String parseResultUrl(String source, String rule) {
        return getPresenter().parseResultUrl(source, rule);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<SearchBookBean> getSearchBooks(String source) {
        return getDelegate().getSearchBooks(source);
    }

    @Override
    public BookShelfBean getBook(String source) {
        return getDelegate().getBook(source);
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        return getDelegate().getChapters(source);
    }

    @Override
    public BookContentBean getContent(String source) {
        return getDelegate().getContent(source);
    }

    @Override
    public String getAudioLink(String source) {
        return getDelegate().getAudioLink(source);
    }
}
