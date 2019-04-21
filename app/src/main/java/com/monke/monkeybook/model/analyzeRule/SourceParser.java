package com.monke.monkeybook.model.analyzeRule;

import java.util.List;

abstract class SourceParser<S, T> {

    private S mSource;
    private String mStringSource;

    final void setContent(String source){
        mStringSource = source;
        mSource = fromSource(source);
    }

    final void setContent(T source){
        mStringSource = sourceToString(source);
        mSource = fromSource(source);
    }

    final S getSource() {
        return mSource;
    }

    final String getStringSource(){
        return mStringSource;
    }

    final boolean empty(){
        return mSource == null;
    }

    abstract String sourceToString(T source);

    abstract S fromSource(String source);

    abstract S fromSource(T source);

    abstract List<T> getList(String rawRule);

    abstract List<T> parseList(String source, String rawRule);

    abstract String getString(String rawRule);

    abstract String parseString(String source, String rawRule);

    abstract List<String> getStringList(String rawRule);

    abstract List<String> parseStringList(String source, String rawRule);

}
