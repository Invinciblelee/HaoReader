package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.utils.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    final int mergeType;
    final List<RulePattern> patterns;

    private RuleMode mode;

    private RulePatterns(String rawRule, VariableStore variableStore, boolean hybrid) {
        Objects.requireNonNull(rawRule);

        if (hybrid) {
            Rule rule = Rule.fromStringRule(rawRule);
            rawRule = rule.getRule();
            mode = rule.getMode();
        }

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

        patterns = new ArrayList<>();
        for (String rule : rules) {
            if (!StringUtils.isTrimEmpty(rule)) {
                patterns.add(RulePattern.fromRule(rule.trim(), variableStore));
            }
        }

        if (!patterns.isEmpty()) {
            RulePattern lastPattern = patterns.get(patterns.size() - 1);
            for (RulePattern pattern : patterns) {
                pattern.setMode(mode);
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

    static RulePatterns fromRule(String rawRule, VariableStore variableStore) {
        return new RulePatterns(rawRule, variableStore, false);
    }

    static RulePatterns fromRule(String rawRule) {
        return new RulePatterns(rawRule, null, false);
    }

    static RulePatterns fromHybridRule(String rawRule, VariableStore variableStore) {
        return new RulePatterns(rawRule, variableStore, true);
    }

    static RulePatterns fromHybridRule(String rawRule) {
        return new RulePatterns(rawRule, null, true);
    }
}