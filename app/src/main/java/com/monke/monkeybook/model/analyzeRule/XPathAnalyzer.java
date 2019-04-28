package com.monke.monkeybook.model.analyzeRule;

import org.seimicrawler.xpath.JXDocument;

final class XPathAnalyzer extends OutAnalyzer<JXDocument> {

    XPathAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    SourceParser<JXDocument> onCreateSourceParser() {
        return new XPathParser();
    }
}
