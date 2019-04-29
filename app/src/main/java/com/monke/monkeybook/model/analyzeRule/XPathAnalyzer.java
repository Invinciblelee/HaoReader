package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.annotation.RuleType;

import org.seimicrawler.xpath.JXDocument;

final class XPathAnalyzer extends OutAnalyzer<JXDocument> {

    XPathAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    String getRuleType() {
        return RuleType.XPATH;
    }

    @Override
    SourceParser<JXDocument> onCreateSourceParser() {
        return new XPathParser();
    }
}
