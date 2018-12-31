package com.monke.monkeybook.model.analyzeRule;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.monke.monkeybook.model.analyzeRule.XPathParser.getAsElements;
import static com.monke.monkeybook.model.analyzeRule.XPathParser.getAsString;

public class XPathAnalyzer extends OutAnalyzer<Element, Element> {

    private XJsoupContentDelegate mDelegate;

    @Override
    public ContentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = new XJsoupContentDelegate(this);
        }
        return mDelegate;
    }

    @Override
    public Document parseSource(String source) {
        return Jsoup.parse(source);
    }

    @Override
    public String getResultContent(Element source, String rule) {
        String result = "";
        if (source == null || isEmpty(rule)) {
            return result;
        }
        RulePattern rulePattern = RulePattern.from(rule.trim());
        if (isEmpty(rulePattern.elementsRule)) {
            return result;
        } else {
            JXDocument jxDocument = JXDocument.create(source.children());
            result = getAsString(jxDocument, rulePattern.elementsRule);
        }

        return processingResultContent(result, rulePattern);
    }

    @Override
    public String getResultUrl(Element source, String rule) {
        String result = getResultContent(source, rule);
        return processingResultUrl(result);
    }

    @Override
    public List<Element> getRawList(String source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        JXDocument jxDocument = JXDocument.create(source);
        return getAsElements(jxDocument, rule);
    }

    @Override
    public List<Element> getRawList(Element source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        JXDocument jxDocument = JXDocument.create(source.children());
        return getAsElements(jxDocument, rule);
    }
}
