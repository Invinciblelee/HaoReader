package com.monke.monkeybook.model.analyzeRule;

import java.util.List;
import java.util.Map;

public interface IAnalyzerPresenter  {

    String getResultContent(String rule);

    String getResultUrl(String rule);

    List<String> getResultContents(String rule);

    Map<String, String> getVariableMap(String rule);

    AnalyzeCollection getRawCollection(String rule);

}
