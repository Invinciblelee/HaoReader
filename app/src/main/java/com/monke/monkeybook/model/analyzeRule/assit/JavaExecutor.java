package com.monke.monkeybook.model.analyzeRule.assit;

public interface JavaExecutor extends SimpleJavaExecutor {

    String putVariable(String key, String val);

    String getVariable(String key);

    String parseContent(Object source, String rule);

    String parseUrl(Object source, String rule);

}
