package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

class Rule {

    private String rule;
    private RuleMode mode;

    Rule() {
    }

    Rule(String rule, RuleMode mode) {
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

    @NonNull
    @Override
    public String toString() {
        return "Rule{" +
                "rule='" + rule + '\'' +
                ", mode=" + mode +
                '}';
    }
}
