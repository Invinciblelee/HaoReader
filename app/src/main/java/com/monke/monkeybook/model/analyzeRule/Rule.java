package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.analyzeRule.pattern.Patterns;
import com.monke.monkeybook.utils.StringUtils;

final class Rule {

    private String rule;
    private RuleMode mode;

    Rule(){
    }

    private Rule(String rule, RuleMode mode) {
        this.rule = rule;
        this.mode = mode;
    }

    String getRule() {
        return rule;
    }

    RuleMode getMode() {
        return mode;
    }

    void setRule(String rule) {
        this.rule = rule;
    }

    void setMode(RuleMode mode) {
        this.mode = mode;
    }

    static Rule fromStringRule(String rawRule) {
        final String rule;
        final RuleMode mode;
        if (StringUtils.startWithIgnoreCase(rawRule, Patterns.RULE_XPATH)) {
            mode = RuleMode.XPath;
            rule = rawRule.substring(7);
        } else if (StringUtils.startWithIgnoreCase(rawRule, Patterns.RULE_XPATH_TRAIT)) {//XPath特征很明显,无需配置单独的识别标头
            mode = RuleMode.XPath;
            rule = rawRule;
        } else if (StringUtils.startWithIgnoreCase(rawRule, Patterns.RULE_JSON)) {
            mode = RuleMode.JSon;
            rule = rawRule.substring(6);
        } else if (StringUtils.startWithIgnoreCase(rawRule, Patterns.RULE_JSON_TRAIT)) {
            mode = RuleMode.JSon;
            rule = rawRule;
        } else {
            mode = RuleMode.Default;
            rule = rawRule;
        }
        return new Rule(rule, mode);
    }

    @Override
    public String toString() {
        return "Rule{" +
                "rule='" + rule + '\'' +
                ", mode=" + mode +
                '}';
    }
}
