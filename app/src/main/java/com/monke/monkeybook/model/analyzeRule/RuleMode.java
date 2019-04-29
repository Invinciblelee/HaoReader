package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.annotation.RuleType;

enum RuleMode {
    XPath, JSon, CSS, Default;


    static RuleMode fromRuleType(@RuleType String ruleType) {
        switch (ruleType) {
            case RuleType.XPATH:
                return XPath;
            case RuleType.JSON:
                return JSon;
            case RuleType.CSS:
                return CSS;
            case RuleType.DEFAULT:
                return Default;
            case RuleType.HYBRID:
            default:
                return null;
        }
    }


}