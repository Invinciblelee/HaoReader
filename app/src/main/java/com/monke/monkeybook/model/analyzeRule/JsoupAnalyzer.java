package com.monke.monkeybook.model.analyzeRule;

import org.jsoup.nodes.Element;

/**
 * Created by GKF on 2018/1/25.
 * 书源规则解析
 */

final class JsoupAnalyzer extends OutAnalyzer<Element> {

    JsoupAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    SourceParser<Element> onCreateSourceParser() {
        return new JsoupParser();
    }
}

