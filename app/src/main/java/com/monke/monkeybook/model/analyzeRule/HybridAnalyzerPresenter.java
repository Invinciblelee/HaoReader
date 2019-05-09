package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.help.MemoryCache;

class HybridAnalyzerPresenter extends DefaultAnalyzerPresenter {

    @SuppressWarnings("unchecked")
    HybridAnalyzerPresenter(OutAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        MemoryCache cache = MemoryCache.getInstance();
        RulePatterns patterns = cache.getCache(rawRule);
        if (patterns != null) {
            return patterns;
        }
        if (withVariableStore) {
            patterns = RulePatterns.fromHybridRule(rawRule, getConfig().getBaseURL(), getConfig().getVariableStore());
        } else {
            patterns = RulePatterns.fromHybridRule(rawRule, getConfig().getBaseURL());
        }
        cache.putCache(rawRule, patterns);
        return patterns;
    }

    @Override
    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        MemoryCache cache = MemoryCache.getInstance();
        RulePattern pattern = cache.getCache(rawRule);
        if (pattern != null) {
            return pattern;
        }
        if (withVariableStore) {
            pattern = RulePattern.fromHybridRule(rawRule, getConfig().getVariableStore());
        } else {
            pattern = RulePattern.fromHybridRule(rawRule);
        }
        cache.putCache(rawRule, pattern);
        return pattern;
    }


}
