package com.monke.monkeybook.model.analyzeRule;

import org.jsoup.nodes.Element;

/**
 * Created by GKF on 2018/1/25.
 * 书源规则解析
 */

public class JsoupAnalyzer extends OutAnalyzer<Element, Element> {

    private JsoupParser mParser;
    private JsoupPresenter mPresenter;
    private ContentDelegate mDelegate;

    @Override
    SourceParser<Element, Element> getParser() {
        if(mParser == null){
            mParser = new JsoupParser();
        }
        return mParser;
    }

    @Override
    IAnalyzerPresenter getPresenter() {
        if(mPresenter == null){
            mPresenter = new JsoupPresenter(this);
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

    private static class JsoupPresenter extends AnalyzerPresenter<Element, Element>{

        private JsoupPresenter(OutAnalyzer<Element, Element> analyzer) {
            super(analyzer);
        }
    }
}

