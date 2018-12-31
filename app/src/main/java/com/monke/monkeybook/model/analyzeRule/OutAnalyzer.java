package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.utils.NetworkUtil;

import java.util.List;

import static android.text.TextUtils.isEmpty;

public abstract class OutAnalyzer<S, R> {

    private AnalyzeConfig config;

    public final AnalyzeConfig getConfig() {
        return config;
    }

    public final AnalyzeConfig newConfig() {
        if (config == null) {
            return new AnalyzeConfig();
        } else {
            return config.newConfig();
        }
    }

    public final void apply(@NonNull AnalyzeConfig config) {
        this.config = config;
    }

    public abstract ContentDelegate getDelegate();

    public abstract S parseSource(String source);

    public S parseSource(Object source) {
        return null;
    }

    public abstract String getResultContent(S source, String rule);

    public abstract String getResultUrl(S source, String rule);

    public abstract List<R> getRawList(String source, String rule);

    public abstract List<R> getRawList(S source, String rule);

    final String processingResultContent(@NonNull String result, @NonNull RulePattern rulePattern) {
        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        if (!isEmpty(rulePattern.javaScript)) {
            result = JSParser.evalJS(rulePattern.javaScript, result, getConfig().getBaseURL());
        }
        return result;
    }

    final String processingResultUrl(@NonNull String result) {
        if (!isEmpty(result) && !result.startsWith("http")) {
            result = NetworkUtil.getAbsoluteURL(getConfig().getBaseURL(), result);
        }
        return result;
    }

    static class RulePattern {
        String elementsRule;
        String replaceRegex ;
        String replacement;
        String javaScript;

        private RulePattern(@NonNull String ruleStr){
            //分离js
            String[] ruleStrJ = ruleStr.split("@js:");
            if (ruleStrJ.length > 1) {
                ruleStr = ruleStrJ[0];
                javaScript = ruleStrJ[1];
            }else {
                javaScript = "";
            }
            //分离正则表达式
            String[] ruleStrS = ruleStr.split("#");
            elementsRule = ruleStrS[0];
            if (ruleStrS.length > 1) {
                replaceRegex = ruleStrS[1];
            }else {
                replaceRegex = "";
            }

            if (ruleStrS.length > 2) {
                replacement = ruleStrS[2];
            }else {
                replacement = "";
            }
        }

        static RulePattern from(@NonNull String ruleStr){
            return new RulePattern(ruleStr);
        }
    }
}
