package com.monke.monkeybook.bean;

import com.monke.monkeybook.model.analyzeRule.assit.Assistant;

import java.util.HashMap;
import java.util.Map;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.MAP_TYPE;

public class VariableStoreImpl implements VariableStore {

    private Map<String, String> variableMap;
    private String variableString;

    public VariableStoreImpl() {
    }

    public VariableStoreImpl(String variableString) {
        this.variableString = variableString;
    }

    @Override
    public String getVariableString() {
        return this.variableString;
    }

    @Override
    public void setVariableString(String variableString) {
        this.variableString = variableString;
    }

    @Override
    public Map<String, String> getVariableMap() {
        if (variableMap == null) {
            try {
                this.variableMap = Assistant.fromJson(variableString, MAP_TYPE);
            } catch (Exception ignore) {
            }
        }
        if (this.variableMap == null) {
            this.variableMap = new HashMap<>();
        }
        return variableMap;
    }

    @Override
    public Map<String, String> putVariableMap(Map<String, String> variableMap) {
        if (variableMap != null && !variableMap.isEmpty()) {
            Map<String, String> map = getVariableMap();
            map.putAll(variableMap);
            this.variableString = Assistant.toJson(map);
        }
        return this.variableMap;
    }

    @Override
    public String putVariable(String key, String value) {
        if (key != null && value != null) {
            Map<String, String> map = getVariableMap();
            map.put(key, value);
            this.variableString = Assistant.toJson(map);
        }
        return value;
    }

    @Override
    public String getVariable(String key) {
        Map<String, String> map = getVariableMap();
        return !map.isEmpty() ? map.get(key) : null;
    }

}
