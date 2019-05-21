package com.monke.monkeybook.bean;

import java.util.Map;

public interface VariableStore {

    String getVariableString();

    void setVariableString(String variableString);

    Map<String, String> putVariableMap(Map<String, String> variableMap);

    String putVariable(String key, String value);

    Map<String, String> getVariableMap();

    String getVariable(String key);
}
