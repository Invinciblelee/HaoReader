package com.monke.monkeybook.model.analyzeRule;

class HybridAnalyzerPresenter extends DefaultAnalyzerPresenter {

    @SuppressWarnings("unchecked")
    HybridAnalyzerPresenter(OutAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        if(withVariableStore){
            return RulePatterns.fromHybridRule(rawRule, getConfig().getVariableStore());
        }else {
            return RulePatterns.fromHybridRule(rawRule);
        }
    }

    @Override
    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        if(withVariableStore){
            return RulePattern.fromHybridRule(rawRule, getConfig().getVariableStore());
        }else {
            return RulePattern.fromHybridRule(rawRule);
        }
    }
}
