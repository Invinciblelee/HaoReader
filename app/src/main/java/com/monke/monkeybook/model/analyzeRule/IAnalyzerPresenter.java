package com.monke.monkeybook.model.analyzeRule;

import java.util.List;
import java.util.Map;

public interface IAnalyzerPresenter {

    String getResultContent(String rule);

    String getResultUrl(String rule);

    String getResultContentDirectly(String rule);

    String getResultUrlDirectly(String rule);

    List<String> getResultContents(String rule);

    List<String> getResultUrls(String rule);

    Map<String, String> putVariableMap(String rule, int flag);

    Map<String, String> putVariableMapDirectly(String rule, int flag);

    AnalyzeCollection getRawCollection(String rule);

}
