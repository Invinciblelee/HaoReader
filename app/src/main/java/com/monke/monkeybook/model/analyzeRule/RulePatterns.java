package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.utils.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.IntDef;

final class RulePatterns {

    static final int RULE_RESULT_AND = 0x001;
    static final int RULE_RESULT_OR = 0x002;
    static final int RULE_RESULT_FILTER = 0x003;

    @IntDef({
            RULE_RESULT_AND,
            RULE_RESULT_OR,
            RULE_RESULT_FILTER
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ResultType {
    }

    @ResultType
    final int resultType;
    final List<RulePattern> patterns;

    private RulePatterns(String rawRule, VariableStore variableStore) {
        Objects.requireNonNull(rawRule);

        final String[] rules;
        if (rawRule.contains("&&")) {
            rules = rawRule.split("&&");
            resultType = RULE_RESULT_AND;
        } else if (rawRule.contains("%%")) {
            rules = rawRule.split("%%");
            resultType = RULE_RESULT_FILTER;
        } else {
            rules = rawRule.split("\\|\\|");
            resultType = RULE_RESULT_OR;
        }

        patterns = new ArrayList<>();
        for (String rule : rules) {
            if (!StringUtils.isTrimEmpty(rule)) {
                patterns.add(RulePattern.fromRule(rule.trim(), variableStore));
            }
        }

        if (!patterns.isEmpty()) {
            RulePattern lastPattern = patterns.get(patterns.size() - 1);
            if (!TextUtils.isEmpty(lastPattern.replaceRegex) || !lastPattern.javaScripts.isEmpty()) {
                for (int i = 0, size = patterns.size() - 1; i < size; i++) {
                    RulePattern pattern = patterns.get(i);
                    if (pattern.isKeep) {
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
    }

    static RulePatterns fromRule(String rawRule, VariableStore variableStore) {
        return new RulePatterns(rawRule, variableStore);
    }

    static RulePatterns fromRule(String rawRule) {
        return new RulePatterns(rawRule, null);
    }

}