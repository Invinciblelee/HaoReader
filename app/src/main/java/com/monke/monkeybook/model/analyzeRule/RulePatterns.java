package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.utils.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

final class RulePatterns {

    static final int RULE_MERGE_AND = 0x001;
    static final int RULE_MERGE_OR = 0x002;
    static final int RULE_MERGE_FILTER = 0x003;

    @IntDef({
            RULE_MERGE_AND,
            RULE_MERGE_OR,
            RULE_MERGE_FILTER
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface MergeType {
    }

    @MergeType
    int mergeType;
    List<RulePattern> patterns;

    private RuleMode mode;

    private RulePatterns(String rawRule, VariableStore variableStore) {
        Rule rule = RootRule.fromStringRule(rawRule);
        rawRule = rule.getRule();
        this.mode = rule.getMode();

        splitRulePattern(variableStore, splitRule(rawRule), this.mode);
    }

    private RulePatterns(String rawRule, VariableStore variableStore, RuleMode ruleMode) {
        this.mode = ruleMode;

        splitRulePattern(variableStore, splitRule(rawRule), this.mode);
    }

    private String[] splitRule(String rawRule) {
        final String[] rules;
        if (rawRule.contains("&&")) {
            rules = rawRule.split("&&");
            mergeType = RULE_MERGE_AND;
        } else if (rawRule.contains("%%")) {
            rules = rawRule.split("%%");
            mergeType = RULE_MERGE_FILTER;
        } else {
            rules = rawRule.split("\\|\\|");
            mergeType = RULE_MERGE_OR;
        }
        return rules;
    }

    private void splitRulePattern(VariableStore variableStore, String[] rules, RuleMode mode) {
        patterns = new ArrayList<>();
        for (String ruleStr : rules) {
            if (!StringUtils.isBlank(ruleStr)) {
                patterns.add(RulePattern.fromRule(ruleStr.trim(), variableStore, mode));
            }
        }

        if (!patterns.isEmpty()) {
            RulePattern lastPattern = patterns.get(patterns.size() - 1);
            for (RulePattern pattern : patterns) {
                if (pattern == lastPattern || pattern.isKeep) {
                    continue;
                }
                if (TextUtils.isEmpty(pattern.replaceRegex)) {
                    pattern.replaceRegex = lastPattern.replaceRegex;
                    pattern.replacement = lastPattern.replacement;
                }

                if (pattern.javaScripts.isEmpty()) {
                    pattern.javaScripts = lastPattern.javaScripts;
                }
            }
        }
    }

    static RulePatterns fromRule(String rawRule, VariableStore variableStore, RuleMode ruleMode) {
        return new RulePatterns(rawRule, variableStore, ruleMode);
    }

    static RulePatterns fromRule(String rawRule, RuleMode ruleMode) {
        return fromRule(rawRule, null, ruleMode);
    }

    static RulePatterns fromHybridRule(String rawRule, VariableStore variableStore) {
        return new RulePatterns(rawRule, variableStore);
    }

    static RulePatterns fromHybridRule(String rawRule) {
        return fromHybridRule(rawRule, null);
    }

}