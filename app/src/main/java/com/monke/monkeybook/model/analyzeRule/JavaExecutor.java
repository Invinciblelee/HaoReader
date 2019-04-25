package com.monke.monkeybook.model.analyzeRule;

public interface JavaExecutor {

    String ajax(String url);

    String base64Decode(String string);

    String formatResultContent(String string);

    String formatResultUrl(String string);

    String parseResultContent(String source, String rule);

    String parseResultUrl(String source, String rule);

}
