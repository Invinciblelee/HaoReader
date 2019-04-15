package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.help.Constant;

import androidx.annotation.NonNull;

public class AnalyzerFactory {

    private AnalyzerFactory() {

    }

    public static OutAnalyzer create(@Constant.RuleType String ruleType) {
        OutAnalyzer analyzer;
        if (TextUtils.isEmpty(ruleType)) {
            analyzer = new JsoupAnalyzer();
        } else {
            switch (ruleType) {
                case Constant.RuleType.XPATH:
                    analyzer = new XPathAnalyzer();
                    break;
                case Constant.RuleType.JSON:
                    analyzer = new JsonAnalyzer();
                    break;
                case Constant.RuleType.DEFAULT:
                default:
                    analyzer = new JsoupAnalyzer();
            }
        }
        return analyzer;
    }

    public static OutAnalyzer create(@Constant.RuleType String ruleType, @NonNull AnalyzeConfig config) {
        OutAnalyzer analyzer = create(ruleType);
        analyzer.apply(config);
        return analyzer;
    }

}
