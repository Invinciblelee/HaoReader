package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.model.analyzeRule.assit.Global;

import java.util.HashMap;
import java.util.Map;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.STRING_MAP;

final class VariablesPattern {

    final Map<String, String> putterMap;

    private VariablesPattern(@NonNull String ruleStr) {
        putterMap = new HashMap<>();
        try {
            Map<String, String> putVariable = Global.GSON.fromJson(ruleStr, STRING_MAP);
            putterMap.putAll(putVariable);
        } catch (Exception ignore) {
        }
    }

    static VariablesPattern fromRule(@NonNull String ruleStr) {
        return new VariablesPattern(ruleStr);
    }

}
