package com.monke.monkeybook.model.analyzeRule;


import com.monke.monkeybook.model.analyzeRule.pattern.Patterns;

import java.util.List;

abstract class SourceParser<S> {

    private S mSource;

    final void setContent(Object source) {
        mSource = fromObject(source);
    }

    final S getSource() {
        return mSource;
    }

    final String getStringSource() {
        return parseObject(mSource);
    }

    boolean isSourceEmpty() {
        return mSource == null;
    }

    final boolean isOuterBody(String rule) {
        return Patterns.RULE_BODY.equals(rule);
    }

    abstract String parseObject(Object source);

    abstract S fromObject(Object source);

    abstract List<Object> getList(Rule rule);

    abstract List<Object> parseList(String source, Rule rule);

    abstract String getString(Rule rule);

    abstract String parseString(String source, Rule rule);

    abstract String getStringFirst(Rule rule);

    abstract String parseStringFirst(String source, Rule rule);

    abstract List<String> getStringList(Rule rule);

    abstract List<String> parseStringList(String source, Rule rule);

}
