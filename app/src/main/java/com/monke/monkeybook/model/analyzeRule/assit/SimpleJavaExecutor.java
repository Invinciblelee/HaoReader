package com.monke.monkeybook.model.analyzeRule.assit;

public interface SimpleJavaExecutor{

    String ajax(String url);

    String base64Decode(String string);

    String base64Encode(String string);

    String formatHtml(String string);

    String unescapeHtml3(String string);

    String unescapeHtml4(String string);
}
