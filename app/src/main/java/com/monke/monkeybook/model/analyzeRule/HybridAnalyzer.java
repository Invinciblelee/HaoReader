package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.annotation.RuleType;

public final class HybridAnalyzer extends OutAnalyzer {

    public HybridAnalyzer(AnalyzeConfig config) {
        super(config);
    }

    @Override
    String getRuleType() {
        return RuleType.HYBRID;
    }


    @Override
    SourceParser onCreateSourceParser() {
        return new HybridParser();
    }

    @Override
    IAnalyzerPresenter onCreateAnalyzerPresenter(OutAnalyzer analyzer) {
        return new HybridAnalyzerPresenter(analyzer);
    }
}
