package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.model.analyzeRule.pattern.Patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.STRING_MAP;

final class VariablesPattern {

    final Map<String, String> putterMap;

    private VariablesPattern(@NonNull String ruleStr, int flag) {
        putterMap = new HashMap<>();

        if (flag == 0 && findWhere(ruleStr, Patterns.PATTERN_PUT_SEARCH)) {
            return;
        }

        if (flag == 1 && findWhere(ruleStr, Patterns.PATTERN_PUT_DETAIL)) {
            return;
        }

        analyzePutterMap(ruleStr);
    }

    private boolean findWhere(String ruleStr, Pattern pattern) {
        Matcher matcher = pattern.matcher(ruleStr);
        if (matcher.find()) {
            String group = matcher.group();
            String value = group.substring(8);
            analyzePutterMap(value);
            return true;
        }
        return false;
    }

    private void analyzePutterMap(String rule) {
        try {
            Map<String, String> putVariable = Global.GSON.fromJson(rule, STRING_MAP);
            putterMap.putAll(putVariable);
        } catch (Exception ignore) {
        }
    }

    static VariablesPattern fromRule(@NonNull String ruleStr, int flag) {
        return new VariablesPattern(ruleStr, flag);
    }

}
