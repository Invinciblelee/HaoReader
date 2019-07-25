package com.monke.monkeybook.model.analyzeRule;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;
import com.monke.monkeybook.utils.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import javax.script.SimpleBindings;

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

    private String splitType;
    private List<Integer> filters;

    private RulePatterns(String rawRule, String baseUrl, VariableStore variableStore, RuleMode ruleMode) {
        rawRule = splitFilters(rawRule);

        rawRule = replaceJavaScripts(rawRule, baseUrl);

        splitRulePattern(variableStore, splitRule(rawRule), ruleMode);
    }

    @SuppressLint("DefaultLocale")
    private String replaceJavaScripts(String rawRule, String baseUrl) {
        if (rawRule.contains("{{") && rawRule.contains("}}")) {
            final StringBuffer buffer = new StringBuffer(rawRule.length());
            final SimpleBindings simpleBindings = new SimpleBindings() {{
                this.put("baseUrl", baseUrl);
            }};
            Matcher expMatcher = AnalyzeGlobal.PATTERN_EXP.matcher(rawRule);
            while (expMatcher.find()) {
                Object result = Assistant.evalObjectScript(expMatcher.group(1), simpleBindings);
                if (result instanceof Double && ((Double) result) % 1.0 == 0) {
                    expMatcher.appendReplacement(buffer, String.format("%.0f", (Double) result));
                } else {
                    expMatcher.appendReplacement(buffer, StringUtils.valueOf(result));
                }
            }
            expMatcher.appendTail(buffer);
            rawRule = buffer.toString();
        }
        return rawRule;
    }

    private String splitFilters(String rawRule) {
        if (rawRule.contains("!!")) {
            String[] rules = rawRule.split("!!");
            rawRule = rules[0];

            filters = new ArrayList<>();
            final String[] arr = rules[1].split(":");
            for (String string : arr) {
                filters.add(Integer.parseInt(string));
            }
        }
        return rawRule;
    }

    private String[] splitRule(String rawRule) {
        final String[] rules;
        if (rawRule.contains("&&")) {
            rules = rawRule.split("&&");
            splitType = "&&";
            mergeType = RULE_MERGE_AND;
        } else if (rawRule.contains("%%")) {
            rules = rawRule.split("%%");
            splitType = "%%";
            mergeType = RULE_MERGE_FILTER;
        } else {
            rules = rawRule.split("\\|\\|");
            splitType = "||";
            mergeType = RULE_MERGE_OR;
        }
        return rules;
    }

    private void splitRulePattern(VariableStore variableStore, String[] rules, RuleMode mode) {
        patterns = new ArrayList<>();

        final List<String> ruleList = new ArrayList<>();
        if (filters != null) {
            if (Collections.max(filters) >= rules.length - 1) {
                ruleList.add(StringUtils.join(splitType, rules));
            } else {
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < rules.length; i++) {
                    builder.append(splitType).append(rules[i]);
                    if (!filters.contains(i)) {
                        ruleList.add(builder.substring(2));
                        builder.setLength(0);
                    }
                }
            }
        } else {
            ruleList.addAll(Arrays.asList(rules));
        }

        for (String ruleStr : ruleList) {
            if (!StringUtils.isBlank(ruleStr)) {
                patterns.add(RulePattern.fromRule(ruleStr, variableStore, mode));
            }
        }

        if (patterns.size() > 1) {
            RulePattern firstPattern = patterns.get(0);
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

                if (pattern != firstPattern && pattern.elementsRule.getMode() == null) {
                    pattern.elementsRule.setMode(firstPattern.elementsRule.getMode());
                }
            }
        }
    }

    static RulePatterns fromRule(String rawRule, String baseUrl, VariableStore variableStore, RuleMode ruleMode) {
        return new RulePatterns(StringUtils.trim(rawRule), baseUrl, variableStore, ruleMode);
    }

    static RulePatterns fromRule(String rawRule, String baseUrl, RuleMode ruleMode) {
        return fromRule(rawRule, baseUrl, null, ruleMode);
    }


    @Override
    public String toString() {
        return "RulePatterns{" +
                "mergeType=" + mergeType +
                ", patterns=" + patterns +
                ", splitType='" + splitType + '\'' +
                ", filters=" + filters +
                '}';
    }
}