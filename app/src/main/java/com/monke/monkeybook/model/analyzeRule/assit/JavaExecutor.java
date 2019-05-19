package com.monke.monkeybook.model.analyzeRule.assit;

import java.util.List;

public interface JavaExecutor extends SimpleJavaExecutor{

    String parseResultContent(Object source, String rule);

    String parseResultUrl(Object source, String rule);

    List<String> parseResultContents(Object source, String rule);
}
