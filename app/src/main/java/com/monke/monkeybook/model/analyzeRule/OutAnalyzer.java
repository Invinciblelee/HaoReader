package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

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
    private ContentDelegate mDelegate;

    private final AnalyzeConfig mConfig = new AnalyzeConfig();

    public OutAnalyzer(AnalyzeConfig config) {
        mConfig.apply(config);
    }

    public final AnalyzeConfig getConfig() {
        return mConfig;
    }

    public final AnalyzeConfig newConfig() {
        return mConfig.newConfig();
    }

    public void apply(@NonNull AnalyzeConfig config) {
        this.mConfig.apply(config);
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

    final IAnalyzerPresenter getPresenter() {
        if (mPresenter == null) {
            mPresenter = onCreateAnalyzerPresenter(this);
        }
        return mPresenter;
    }

    private ContentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = onCreateContentDelegate(this);
        }
        return mDelegate;
    }

    abstract SourceParser<S> onCreateSourceParser();

    IAnalyzerPresenter onCreateAnalyzerPresenter(OutAnalyzer<S> analyzer) {
        return new DefaultAnalyzerPresenter<>(analyzer);
    }

    ContentDelegate onCreateContentDelegate(OutAnalyzer<S> analyzer) {
        return new DefaultContentDelegate(analyzer);
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
    public Map<String, String> getVariableMap(String rule, int flag) {
        return getPresenter().getVariableMap(rule, flag);
    }

    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        return getPresenter().getRawCollection(rule);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Observable<List<SearchBookBean>> getSearchBooks(String source) {
        return getDelegate().getSearchBooks(source);
    }

    @Override
    public Observable<BookShelfBean> getBook(String source) {
        return getDelegate().getBook(source);
    }

    @Override
    public Observable<List<ChapterBean>> getChapters(String source) {
        return getDelegate().getChapters(source);
    }

    @Override
    public Observable<BookContentBean> getContent(String source) {
        return getDelegate().getContent(source);
    }

    @Override
    public Observable<String> getAudioLink(String source) {
        return getDelegate().getAudioLink(source);
    }
}
