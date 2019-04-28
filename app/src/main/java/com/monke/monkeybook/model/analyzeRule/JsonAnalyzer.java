package com.monke.monkeybook.model.analyzeRule;

import com.jayway.jsonpath.ReadContext;

final class JsonAnalyzer extends OutAnalyzer<ReadContext> {

    JsonAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    SourceParser<ReadContext> onCreateSourceParser() {
        return new JsonParser();
    }
}
