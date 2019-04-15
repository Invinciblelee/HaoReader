package com.monke.monkeybook.model.analyzeRule;

import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;

public class XPathAnalyzer extends OutAnalyzer<JXDocument, Element> {

    private XPathParser mParser;
    private XPathPresenter mPresenter;
    private XJsoupContentDelegate mDelegate;


    @Override
    SourceParser<JXDocument, Element> getParser() {
        if (mParser == null) {
            mParser = new XPathParser();
        }
        return mParser;
    }

    @Override
    IAnalyzerPresenter getPresenter() {
        if (mPresenter == null) {
            mPresenter = new XPathPresenter(this);
        }
        return mPresenter;
    }

    @Override
    public ContentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = new XJsoupContentDelegate(this);
        }
        return mDelegate;
    }

    private static class XPathPresenter extends AnalyzerPresenter<JXDocument, Element> {

        private XPathPresenter(OutAnalyzer<JXDocument, Element> analyzer) {
            super(analyzer);
        }
    }
}
