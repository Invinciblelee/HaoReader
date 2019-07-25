package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.MAP_TYPE;
import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_GET;

final class VariablesPattern {

    Map<String, String> map;
    String rule;

    private VariablesPattern(@NonNull String ruleStr, int flag) {
        map = new HashMap<>();

        if (flag == 0 && findWhere(ruleStr, AnalyzeGlobal.PATTERN_PUT_SEARCH)) {
            return;
        }

        if (flag == 1 && findWhere(ruleStr, AnalyzeGlobal.PATTERN_PUT_DETAIL)) {
            return;
        }

        analyzePutterMap(ruleStr);
    }

    private VariablesPattern(@NonNull String ruleStr, VariableStore variableStore) {
        //分离get规则
        if (variableStore != null) {
            Matcher getMatcher = PATTERN_GET.matcher(ruleStr);
            while (getMatcher.find()) {
                final String group = getMatcher.group();
                final String value = variableStore.getVariable(group.substring(6, group.length() - 1));
                ruleStr = ruleStr.replace(group, value != null ? value : "");
            }
        }
        rule = ruleStr;
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
            Map<String, String> putVariable = Assistant.fromJson(rule, MAP_TYPE);
            map.putAll(putVariable);
        } catch (Exception ignore) {
        }
    }

    static VariablesPattern fromPutterRule(@NonNull String ruleStr, int flag) {
        return new VariablesPattern(ruleStr, flag);
    }

    static VariablesPattern fromGetterRule(@NonNull String ruleStr, VariableStore variableStore) {
        return new VariablesPattern(ruleStr, variableStore);
    }
}
