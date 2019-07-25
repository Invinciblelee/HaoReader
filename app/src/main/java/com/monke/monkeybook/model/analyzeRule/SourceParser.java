package com.monke.monkeybook.model.analyzeRule;


import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;

import java.util.List;

abstract class SourceParser<S> {

    private Object mPrimitive;
    private S mSource;
    private String mStringSource;

    final void setSource(Object source) {
        mPrimitive = source;
        mSource = null;
        mStringSource = null;
        onAttachSource(source);
    }

    final S getSource() {
        if (mSource == null) {
            mSource = fromObject(mPrimitive);
        }
        return mSource;
    }

    final String getStringSource() {
        if (mStringSource == null) {
            mStringSource = parseObject(mPrimitive);
        }
        return mStringSource;
    }

    final Object getPrimitive() {
        return mPrimitive;
    }

    boolean isSourceEmpty() {
        return mPrimitive == null && mSource == null;
    }

    final boolean isOuterBody(String rule) {
        return AnalyzeGlobal.RULE_BODY.equals(rule);
    }

    void onAttachSource(Object source) {

    }

    abstract String parseObject(Object source);

    abstract S fromObject(Object source);

    abstract List<Object> getList(Rule rule);

    abstract List<Object> parseList(Object source, Rule rule);

    abstract String getString(Rule rule);

    abstract String parseString(Object source, Rule rule);

    abstract String getStringFirst(Rule rule);

    abstract String parseStringFirst(Object source, Rule rule);

    abstract List<String> getStringList(Rule rule);

    abstract List<String> parseStringList(Object source, Rule rule);
}
