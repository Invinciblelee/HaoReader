package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public interface IAnalyzerPresenter {

    String getText(String rule);

    String getRawUrl(String rule);

    String getAbsUrl(String rule);

    String getTextDirectly(String rule);

    String getRawUrlDirectly(String rule);

    String getAbsUrlDirectly(String rule);

    List<String> getTextList(String rule);

    List<String> getRawUrlList(String rule);

    List<String> getAbsUrlList(String rule);

    Map<String, String> putVariableMap(String rule, int flag);

    Map<String, String> putVariableMapDirectly(String rule, int flag);

    AnalyzeCollection getRawCollection(String rule);

    void processUrlList(List<String> result);

    String processUrl(@NonNull String result);
}
