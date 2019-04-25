package com.monke.monkeybook.model.analyzeRule;

import com.jayway.jsonpath.ReadContext;

public class JsonAnalyzer extends OutAnalyzer<ReadContext, Object> {

    private JsonParser mParser;
    private JsonPresenter mPresenter;
    private ContentDelegate mDelegate;

    @Override
    SourceParser<ReadContext, Object> getParser() {
        if (mParser == null) {
            mParser = new JsonParser();
        }
        return mParser;
    }

    @Override
    IAnalyzerPresenter getPresenter() {
        if (mPresenter == null) {
            mPresenter = new JsonPresenter(this);
        }
        return mPresenter;
    }

    @Override
    public ContentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = new ContentDelegateImpl(this);
        }
        return mDelegate;
    }

    private static class JsonPresenter extends AnalyzerPresenter<ReadContext, Object> {
        private JsonPresenter(OutAnalyzer<ReadContext, Object> analyzer) {
            super(analyzer);
        }
    }
}
