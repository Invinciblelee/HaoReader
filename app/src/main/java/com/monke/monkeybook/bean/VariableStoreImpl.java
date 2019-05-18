package com.monke.monkeybook.bean;

import com.monke.monkeybook.model.analyzeRule.assit.Global;

import java.util.HashMap;
import java.util.Map;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.STRING_MAP;

public class VariableStoreImpl implements VariableStore {

    private Map<String, String> variableMap;
    private String variableString;

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
        return variableMap;
    }

    @Override
    public void putVariableMap(Map<String, String> variableMap) {
        if (variableMap != null && !variableMap.isEmpty()) {
            if (this.variableMap == null) {
                try {
                    this.variableMap = Global.GSON.fromJson(variableString, STRING_MAP);
                } catch (Exception ignore) {
                }
            }
            if (this.variableMap == null) {
                this.variableMap = new HashMap<>();
            }
            this.variableMap.putAll(variableMap);
            this.variableString = Global.GSON.toJson(this.variableMap);
        }
    }

    @Override
    public void putVariable(String key, String value) {
        if (key != null) {
            if (this.variableMap == null) {
                try {
                    this.variableMap = Global.GSON.fromJson(variableString, STRING_MAP);
                } catch (Exception ignore) {
                }
            }
            if (this.variableMap == null) {
                this.variableMap = new HashMap<>();
            }
            this.variableMap.put(key, value);
            this.variableString = Global.GSON.toJson(this.variableMap);
        }
    }

    @Override
    public String getVariable(String key) {
        if (this.variableMap == null) {
            try {
                this.variableMap = Global.GSON.fromJson(variableString, STRING_MAP);
            } catch (Exception ignore) {
            }
        }
        return (this.variableMap != null && !this.variableMap.isEmpty()) ? this.variableMap.get(key) : null;
    }

}
