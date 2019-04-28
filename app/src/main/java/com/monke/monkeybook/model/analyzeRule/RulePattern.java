package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.model.analyzeRule.pattern.Patterns;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_GET;
import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_JS;

final class RulePattern {

    final boolean isRedirect;
    final String redirectRule;

    final boolean isKeep;
    final boolean isSimpleJS;

    final Rule elementsRule;

    String replaceRegex;
    String replacement;
    List<String> javaScripts;

    private RulePattern(@NonNull String rawRule, @Nullable VariableStore variableStore, boolean hybrid) {
        elementsRule = new Rule();
        if (hybrid) {
            Rule rule = Rule.fromStringRule(rawRule);
            rawRule = rule.getRule();
            elementsRule.setMode(rule.getMode());
        }

        //分离get规则
        if (variableStore != null) {
            Matcher getMatcher = PATTERN_GET.matcher(rawRule);
            while (getMatcher.find()) {
                String find = getMatcher.group();
                String value = variableStore.getVariable(find.substring(6, find.length() - 1));
                rawRule = rawRule.replace(find, value != null ? value : "");
            }
        }

        //不共用js规则和正则
        isKeep = rawRule.startsWith(Patterns.RULE_KEEP);
        if (isKeep) {
            rawRule = rawRule.substring(1);
        }

        //分离重定向规则（规则重定向）
        final String[] ruleS = rawRule.split(Patterns.REGEX_REDIRECT);
        if (ruleS.length > 1) {
            isRedirect = true;
            redirectRule = ruleS[1];
            rawRule = ruleS[0];
        } else {
            isRedirect = false;
            redirectRule = "";
        }

        //分离正则表达式
        final String[] rules = rawRule.split(Patterns.RULE_REGEX);
        rawRule = rules[0];
        if (rules.length > 1) {
            replaceRegex = rules[1];
        } else {
            replaceRegex = "";
        }

        if (rules.length > 2) {
            replacement = rules[2];
        } else {
            replacement = "";
        }

        //分离js
        int start = -1;
        javaScripts = new ArrayList<>();
        Matcher jsMatcher = PATTERN_JS.matcher(rawRule);
        while (jsMatcher.find()) {
            String string = jsMatcher.group();
            if (StringUtils.startsWithIgnoreCase(string, "<js>")) {
                javaScripts.add(string.substring(4, string.lastIndexOf("<")));
            } else {
                javaScripts.add(string.substring(4));
            }
            if (start == -1) {
                start = jsMatcher.start();
            }
        }

        elementsRule.setRule(start == -1 ? rawRule : rawRule.substring(0, start));

        //是否全部使用js
        isSimpleJS = !isRedirect && TextUtils.isEmpty(elementsRule.getRule()) && !javaScripts.isEmpty();
    }

    void setMode(RuleMode mode) {
        elementsRule.setMode(mode);
    }

    static RulePattern fromRule(@NonNull String rawRule, @Nullable VariableStore variableStore) {
        return new RulePattern(rawRule, variableStore, false);
    }

    static RulePattern fromRule(@NonNull String rawRule) {
        return new RulePattern(rawRule, null, false);
    }

    static RulePattern fromHybridRule(@NonNull String rawRule, @Nullable VariableStore variableStore) {
        return new RulePattern(rawRule, variableStore, true);
    }

    static RulePattern fromHybridRule(@NonNull String rawRule) {
        return new RulePattern(rawRule, null, true);
    }

    @Override
    public String toString() {
        return "RulePattern{" +
                "isRedirect=" + isRedirect +
                ", redirectRule='" + redirectRule + '\'' +
                ", isKeep=" + isKeep +
                ", isSimpleJS=" + isSimpleJS +
                ", elementsRule=" + elementsRule +
                ", replaceRegex='" + replaceRegex + '\'' +
                ", replacement='" + replacement + '\'' +
                ", javaScripts=" + javaScripts +
                '}';
    }
}