package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.annotation.RuleType;

import org.jsoup.nodes.Element;

final class CSSAnalyzer extends OutAnalyzer<Element> {
    CSSAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    String getRuleType() {
        return RuleType.CSS;
    }

    @Override
    SourceParser<Element> onCreateSourceParser() {
        return new CSSParser();
    }
}
