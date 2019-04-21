package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

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

    final String elementsRule;

    final boolean isRedirect;
    final String redirectRule;

    final boolean isKeep;

    final boolean isSimpleJS;

    String replaceRegex;
    String replacement;
    List<String> javaScripts;

    private RulePattern(@NonNull String ruleStr, @Nullable VariableStore variableStore) {
        //分离get规则
        if (variableStore != null) {
            Matcher getMatcher = PATTERN_GET.matcher(ruleStr);
            while (getMatcher.find()) {
                String find = getMatcher.group();
                String value = variableStore.getVariable(find.substring(6, find.length() - 1));
                ruleStr = ruleStr.replace(find, value != null ? value : "");
            }
        }

        isKeep = ruleStr.startsWith(Patterns.RULE_KEEP);
        if (isKeep) {
            ruleStr = ruleStr.substring(1);
        }

        isRedirect = ruleStr.startsWith(Patterns.RULE_REDIRECT);
        String[] rules = ruleStr.split(Patterns.REGEX_REDIRECT);
        String rawRule = rules[0];

        //分离正则表达式
        String[] ruleStrS = rawRule.split(Patterns.RULE_REGEX);
        rawRule = ruleStrS[0];
        if (ruleStrS.length > 1) {
            replaceRegex = ruleStrS[1];
        } else {
            replaceRegex = "";
        }

        if (ruleStrS.length > 2) {
            replacement = ruleStrS[2];
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

        if (isRedirect) {
            elementsRule = start == -1 ? rawRule.substring(1) : rawRule.substring(1, start);
            if (rules.length > 1) {
                redirectRule = rules[1];
            } else {
                redirectRule = elementsRule;
            }
        } else {
            redirectRule = "";
            elementsRule = start == -1 ? rawRule : rawRule.substring(0, start);
        }

        isSimpleJS = !isRedirect && TextUtils.isEmpty(elementsRule) && !javaScripts.isEmpty();
    }

    static RulePattern fromRule(@NonNull String ruleStr, @Nullable VariableStore variableStore) {
        return new RulePattern(ruleStr, variableStore);
    }

    static RulePattern fromRule(@NonNull String ruleStr) {
        return new RulePattern(ruleStr, null);
    }

    @Override
    public String toString() {
        return "RulePattern{" +
                "elementsRule='" + elementsRule + '\'' +
                ", replaceRegex='" + replaceRegex + '\'' +
                ", replacement='" + replacement + '\'' +
                ", javaScripts=" + javaScripts +
                ", isRedirect=" + isRedirect +
                ", redirectRule='" + redirectRule + '\'' +
                '}';
    }
}