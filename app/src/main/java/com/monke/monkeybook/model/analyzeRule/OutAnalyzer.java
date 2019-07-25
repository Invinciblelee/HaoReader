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

    private final AnalyzeConfig mConfig;

    public OutAnalyzer(AnalyzeConfig config) {
        this.mConfig = config;
    }

    public final AnalyzeConfig getConfig() {
        return mConfig;
    }


    public final OutAnalyzer<S> setContent(Object source) {
        getParser().setSource(source);
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
    public String getText(String rule) {
        return getPresenter().getText(rule);
    }

    @Override
    public String getRawUrl(String rule) {
        return getPresenter().getRawUrl(rule);
    }

    @Override
    public String getAbsUrl(String rule) {
        return getPresenter().getAbsUrl(rule);
    }

    @Override
    public String getTextDirectly(String rule) {
        return getPresenter().getTextDirectly(rule);
    }

    @Override
    public String getRawUrlDirectly(String rule) {
        return getPresenter().getRawUrlDirectly(rule);
    }

    @Override
    public String getAbsUrlDirectly(String rule) {
        return getPresenter().getAbsUrlDirectly(rule);
    }

    @Override
    public List<String> getTextList(String rule) {
        return getPresenter().getTextList(rule);
    }

    @Override
    public List<String> getRawUrlList(String rule) {
        return getPresenter().getRawUrlList(rule);
    }

    @Override
    public List<String> getAbsUrlList(String rule) {
        return getPresenter().getAbsUrlList(rule);
    }

    @Override
    public Map<String, String> putVariableMap(String rule, int flag) {
        return getPresenter().putVariableMap(rule, flag);
    }

    @Override
    public Map<String, String> putVariableMapDirectly(String rule, int flag) {
        return getPresenter().putVariableMapDirectly(rule, flag);
    }

    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        return getPresenter().getRawCollection(rule);
    }

    @Override
    public String processUrl(@NonNull String result) {
        return getPresenter().processUrl(result);
    }

    @Override
    public void processUrlList(List<String> result) {
        getPresenter().processUrlList(result);
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
    public Observable<BookContentBean> getBookContent(String source) {
        return new DefaultContentDelegate(this).getBookContent(source);
    }

    @Override
    public Observable<String> getAudioContent(String source) {
        return new DefaultContentDelegate(this).getAudioContent(source);
    }
}
