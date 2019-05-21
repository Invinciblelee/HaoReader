package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.annotation.RuleType;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

public abstract class OutAnalyzer<S> implements IAnalyzerPresenter, ContentDelegate {

    private SourceParser<S> mSourceParser;
    private IAnalyzerPresenter mPresenter;

    private final AnalyzeConfig mConfig;

    public OutAnalyzer(AnalyzeConfig config) {
        this.mConfig = config;
    }

    public final AnalyzeConfig getConfig() {
        return mConfig;
    }


    public final OutAnalyzer<S> setContent(Object source) {
        getParser().setContent(source);
        return this;
    }

    @RuleType
    abstract String getRuleType();

    final SourceParser<S> getParser() {
        if (mSourceParser == null) {
            mSourceParser = onCreateSourceParser();
        }
        return mSourceParser;
    }

    public final IAnalyzerPresenter getPresenter() {
        if (mPresenter == null) {
            mPresenter = onCreateAnalyzerPresenter(this);
        }
        return mPresenter;
    }


    abstract SourceParser<S> onCreateSourceParser();

    IAnalyzerPresenter onCreateAnalyzerPresenter(OutAnalyzer<S> analyzer) {
        return new DefaultAnalyzerPresenter<>(analyzer);
    }

    @Override
    public String getResultContent(String rule) {
        return getPresenter().getResultContent(rule);
    }

    @Override
    public String getResultUrl(String rule) {
        return getPresenter().getResultUrl(rule);
    }

    @Override
    public String getResultContentInternal(String rule) {
        return getPresenter().getResultContentInternal(rule);
    }

    @Override
    public String getResultUrlInternal(String rule) {
        return getPresenter().getResultUrlInternal(rule);
    }

    @Override
    public List<String> getResultContents(String rule) {
        return getPresenter().getResultContents(rule);
    }

    @Override
    public List<String> getResultUrls(String rule) {
        return getPresenter().getResultUrls(rule);
    }

    @Override
    public Map<String, String> putVariableMap(String rule, int flag) {
        return getPresenter().putVariableMap(rule, flag);
    }

    @Override
    public Map<String, String> putVariableMapInternal(String rule, int flag) {
        return getPresenter().putVariableMapInternal(rule, flag);
    }

    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        return getPresenter().getRawCollection(rule);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Observable<List<SearchBookBean>> getSearchBooks(String source) {
        return new DefaultContentDelegate(this).getSearchBooks(source);
    }

    @Override
    public Observable<BookShelfBean> getBook(String source) {
        return new DefaultContentDelegate(this).getBook(source);
    }

    @Override
    public Observable<List<ChapterBean>> getChapters(String source) {
        return new DefaultContentDelegate(this).getChapters(source);
    }

    @Override
    public Observable<BookContentBean> getContent(String source) {
        return new DefaultContentDelegate(this).getContent(source);
    }

    @Override
    public Observable<String> getAudioContent(String source) {
        return new DefaultContentDelegate(this).getAudioContent(source);
    }
}
