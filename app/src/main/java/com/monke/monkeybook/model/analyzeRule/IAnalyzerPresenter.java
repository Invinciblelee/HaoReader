package com.monke.monkeybook.model.analyzeRule;

import java.util.List;
import java.util.Map;

public interface IAnalyzerPresenter {

    String getResultContent(String rule);

    String getResultUrl(String rule);

    String getResultContentInternal(String rule);

    String getResultUrlInternal(String rule);

    List<String> getResultContents(String rule);

    List<String> getResultUrls(String rule);

    Map<String, String> toVariableMap(String rule, int flag);

    Map<String, String> toVariableMapInternal(String rule, int flag);

    AnalyzeCollection toRawCollection(String rule);

}
