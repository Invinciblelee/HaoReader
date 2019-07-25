package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;
import com.monke.monkeybook.utils.StringUtils;

final class RootRule extends Rule {

    private RootRule(String rule, RuleMode mode) {
        super(rule, mode);
    }

    static RootRule fromStringRule(String rawRule) {
        final String rule;
        final RuleMode mode;
        if (StringUtils.startWithIgnoreCase(rawRule, AnalyzeGlobal.RULE_XPATH)) {
            mode = RuleMode.XPath;
            rule = rawRule.substring(7);
        } else if (StringUtils.startWithIgnoreCase(rawRule, AnalyzeGlobal.RULE_XPATH_TRAIT)) {//XPath特征很明显,无需配置单独的识别标头
            mode = RuleMode.XPath;
            rule = rawRule;
        } else if (StringUtils.startWithIgnoreCase(rawRule, AnalyzeGlobal.RULE_JSON)) {
            mode = RuleMode.JSon;
            rule = rawRule.substring(6);
        } else if (StringUtils.startWithIgnoreCase(rawRule, AnalyzeGlobal.RULE_JSON_TRAIT)) {
            mode = RuleMode.JSon;
            rule = rawRule;
        } else if (StringUtils.startWithIgnoreCase(rawRule, AnalyzeGlobal.RULE_CSS)) {
            mode = RuleMode.CSS;
            rule = rawRule.substring(5);
        }else {
            mode = RuleMode.Default;
            rule = rawRule;
        }
        return new RootRule(rule, mode);
    }

}
