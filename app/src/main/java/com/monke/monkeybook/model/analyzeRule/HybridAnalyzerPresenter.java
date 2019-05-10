package com.monke.monkeybook.model.analyzeRule;

class HybridAnalyzerPresenter extends DefaultAnalyzerPresenter {

    @SuppressWarnings("unchecked")
    HybridAnalyzerPresenter(OutAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        RulePatterns patterns = (RulePatterns) getCache(rawRule);
        if (patterns != null) {
            return patterns;
        }
        if (withVariableStore) {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), getVariableStore(), null);
        } else {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), null);
        }
        putCache(rawRule, patterns);
        return patterns;
    }

    @Override
    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        RulePattern pattern = (RulePattern) getCache(rawRule);
        if (pattern != null) {
            return pattern;
        }
        if (withVariableStore) {
            pattern = RulePattern.fromRule(rawRule, getVariableStore(), null);
        } else {
            pattern = RulePattern.fromRule(rawRule, null);
        }
        putCache(rawRule, pattern);
        return pattern;
    }


}
