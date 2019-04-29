package com.monke.monkeybook.model.analyzeRule;

import com.jayway.jsonpath.ReadContext;
import com.monke.monkeybook.model.annotation.RuleType;

final class JsonAnalyzer extends OutAnalyzer<ReadContext> {

    JsonAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    String getRuleType() {
        return RuleType.JSON;
    }

    @Override
    SourceParser<ReadContext> onCreateSourceParser() {
        return new JsonParser();
    }
}
