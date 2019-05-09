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
            patterns = RulePatterns.fromHybridRule(rawRule, getBaseURL(), getVariableStore());
        } else {
            patterns = RulePatterns.fromHybridRule(rawRule, getBaseURL());
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
            pattern = RulePattern.fromHybridRule(rawRule, getVariableStore());
        } else {
            pattern = RulePattern.fromHybridRule(rawRule);
        }
        putCache(rawRule, pattern);
        return pattern;
    }


}
