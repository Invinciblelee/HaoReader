package com.monke.monkeybook.model.analyzeRule;

final class HybridAnalyzer extends OutAnalyzer {

    HybridAnalyzer(AnalyzeConfig config) {
        super(config);
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
