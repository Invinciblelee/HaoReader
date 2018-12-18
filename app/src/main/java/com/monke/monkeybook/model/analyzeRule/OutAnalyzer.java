package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;

import java.util.List;

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

    public S parseSource(Object source){
        return null;
    }

    public abstract String getResultContent(S source, String rule);

    public abstract String getResultUrl(S source, String rule);

    public abstract List<R> getRawList(String source, String rule);

    public abstract List<R> getRawList(S source, String rule);

    static RulePattern splitSourceRule(String ruleStr) {
        RulePattern rulePattern = new RulePattern();
        //分离js
        String[] ruleStrJ = ruleStr.split("@js:");
        if (ruleStrJ.length > 1) {
            ruleStr = ruleStrJ[0];
            rulePattern.javaScript = ruleStrJ[1];
        }
        //分离正则表达式
        String[] ruleStrS = ruleStr.split("#");
        rulePattern.elementsRule = ruleStrS[0];
        if (ruleStrS.length > 1) {
            rulePattern.replaceRegex = ruleStrS[1];
        }

        if (ruleStrS.length > 2) {
            rulePattern.replacement = ruleStrS[2];
        }
        return rulePattern;
    }

    static class RulePattern {
        String elementsRule = "";
        String replaceRegex = "";
        String replacement = "";
        String javaScript = "";
    }
}
