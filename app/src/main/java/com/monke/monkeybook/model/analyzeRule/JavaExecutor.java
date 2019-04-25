package com.monke.monkeybook.model.analyzeRule;

import java.util.List;

public interface JavaExecutor {

    String ajax(String url);

    String base64Decode(String string);

    String formatResultContent(String string);

    String formatResultUrl(String string);

    String parseResultContent(String source, String rule);

    String parseResultUrl(String source, String rule);

    List<String> parseResultContents(String source, String rule);

    List<Object> parseList(String source, String rule);
}
