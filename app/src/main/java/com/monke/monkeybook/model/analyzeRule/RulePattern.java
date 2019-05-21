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

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_JS;

final class RulePattern {

    boolean isRedirect;
    String redirectRule;

    boolean isKeep;
    boolean isSimpleJS;

    Rule elementsRule;

    String replaceRegex;
    String replacement;
    List<String> javaScripts;

    private RulePattern(@NonNull String rawRule, @Nullable VariableStore variableStore, @Nullable RuleMode ruleMode) {
        elementsRule = new Rule();

        if (ruleMode == null) {
            Rule rule = RootRule.fromStringRule(rawRule);
            elementsRule.setMode(rule.getMode());

            rawRule = rule.getRule();
        } else {
            elementsRule.setMode(ruleMode);
        }

        initRulePattern(rawRule, variableStore, elementsRule.getMode());
    }

    private void initRulePattern(String rawRule, VariableStore variableStore, RuleMode ruleMode) {
        //分离get规则
        rawRule = VariablesPattern.fromGetterRule(rawRule, variableStore).rule;

        //不共用js规则和正则
        rawRule = ensureKeepRule(rawRule);

        //分离重定向规则（规则重定向）
        rawRule = ensureRedirectRule(rawRule);

        //分离正则表达式
        rawRule = ensureRegexRule(rawRule, ruleMode == RuleMode.CSS || ruleMode == RuleMode.XPath);

        //分离js
        int start = ensureJavaScripts(rawRule);

        //最终的规则
        elementsRule.setRule(start == -1 ? rawRule : rawRule.substring(0, start));

        //是否全部使用js
        isSimpleJS = !isRedirect && TextUtils.isEmpty(elementsRule.getRule()) && !javaScripts.isEmpty();

    }

    private String ensureKeepRule(String rawRule) {
        isKeep = rawRule.startsWith(Patterns.RULE_KEEP);
        if (isKeep) {
            rawRule = rawRule.substring(1);
        }
        return rawRule;
    }

    private String ensureRedirectRule(String rawRule) {
        final String[] ruleS = rawRule.split(Patterns.REGEX_REDIRECT);
        if (ruleS.length > 1) {
            isRedirect = true;
            redirectRule = ruleS[1];
            rawRule = ruleS[0];
        } else {
            isRedirect = false;
            redirectRule = "";
        }
        return rawRule;
    }

    private String ensureRegexRule(String rawRule, boolean trait) {
        final String[] rules = rawRule.split(trait ? Patterns.RULE_REGEX_TRAIT : Patterns.RULE_REGEX);
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
        return rawRule;
    }

    private int ensureJavaScripts(String rawRule) {
        int start = -1;
        javaScripts = new ArrayList<>();
        Matcher jsMatcher = PATTERN_JS.matcher(rawRule);
        while (jsMatcher.find()) {
            final String group = jsMatcher.group();
            if (StringUtils.startsWithIgnoreCase(group, "<js>")) {
                javaScripts.add(group.substring(4, group.lastIndexOf("<")));
            } else {
                javaScripts.add(group.substring(4));
            }
            if (start == -1) {
                start = jsMatcher.start();
            }
        }
        return start;
    }

    static RulePattern fromRule(@NonNull String rawRule, @Nullable VariableStore variableStore, @Nullable RuleMode ruleMode) {
        return new RulePattern(StringUtils.trim(rawRule), variableStore, ruleMode);
    }

    static RulePattern fromRule(@NonNull String rawRule, @Nullable RuleMode ruleMode) {
        return fromRule(rawRule, null, ruleMode);
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