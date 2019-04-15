package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.bean.VariableStore;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class RulePattern {
    private static final Pattern PATTERN_GET = Pattern.compile("@get:\\{.+?\\}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_JS = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);

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

        isKeep = ruleStr.startsWith("^");
        if (isKeep) {
            ruleStr = ruleStr.substring(1);
        }

        isRedirect = ruleStr.startsWith("?");
        String[] rules = ruleStr.split("(?i)@redirect:");
        String rawRule = rules[0];

        //分离正则表达式
        String[] ruleStrS = rawRule.split("#");
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