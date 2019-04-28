package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.monkeybook.model.annotation.RuleType;

public class AnalyzerFactory {

    private AnalyzerFactory() {

    }

    public static OutAnalyzer create(@RuleType String ruleType, @NonNull AnalyzeConfig config) {
        OutAnalyzer analyzer;
        if (TextUtils.isEmpty(ruleType)) {
            analyzer = new JsoupAnalyzer(config);
        } else {
            switch (ruleType) {
                case RuleType.XPATH:
                    analyzer = new XPathAnalyzer(config);
                    break;
                case RuleType.JSON:
                    analyzer = new JsonAnalyzer(config);
                    break;
                case RuleType.HYBRID:
                    analyzer = new HybridAnalyzer(config);
                    break;
                case RuleType.DEFAULT:
                default:
                    analyzer = new JsoupAnalyzer(config);
            }
        }
        return analyzer;
    }


}
