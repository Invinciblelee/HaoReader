package com.monke.monkeybook.model.analyzeRule;

class HybridAnalyzerPresenter extends DefaultAnalyzerPresenter {

    @SuppressWarnings("unchecked")
    HybridAnalyzerPresenter(OutAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        final RulePatterns patterns;
        if (withVariableStore) {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), getVariableStore(), null);
        } else {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), null);
        }
        return patterns;
    }

    @Override
    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        final RulePattern pattern;
        if (withVariableStore) {
            pattern = RulePattern.fromRule(rawRule, getVariableStore(), null);
        } else {
            pattern = RulePattern.fromRule(rawRule, null);
        }
        return pattern;
    }


}
