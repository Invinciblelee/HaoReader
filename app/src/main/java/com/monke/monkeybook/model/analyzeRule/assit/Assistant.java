package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public final class Assistant {

    private static final String TAG = Assistant.class.getSimpleName();

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    private Assistant() {
    }

    public static boolean canConvertToJson(Object object) {
        if (object instanceof List || object instanceof Map) {
            return true;
        } else {
            return StringUtils.isJsonType(StringUtils.valueOf(object));
        }
    }

    public static String[] splitRegexRule(String str) {
        int start = 0, index = 0, len = str.length();
        List<String> list = new ArrayList<>();
        while (start < len) {
            if ((str.charAt(start) == '$') && (str.charAt(start + 1) >= '0') && (str.charAt(start + 1) <= '9')) {
                if (start > index) list.add(str.substring(index, start));
                if ((start + 2 < len) && (str.charAt(start + 2) >= '0') && (str.charAt(start + 2) <= '9')) {
                    list.add(str.substring(start, start + 3));
                    index = start += 3;
                } else {
                    list.add(str.substring(start, start + 2));
                    index = start += 2;
                }
            } else {
                ++start;
            }
        }
        if (start > index) list.add(str.substring(index, start));
        return list.toArray(new String[0]);
    }

    public static List<Object> evalArrayScript(String jsStr, JavaExecutor java, Object result, String baseUrl) {
        final Object object = evalObjectScript(jsStr, java, result, baseUrl);
        final List<Object> resultList = new ArrayList<>();
        if (object instanceof List) {
            resultList.addAll((List) object);
        } else if (object != null) {
            resultList.add(object);
        }
        return resultList;
    }

    public static Object evalObjectScript(String jsStr, SimpleBindings bindings) {
        try {
            return Assistant.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }

    public static Object evalObjectScript(String jsStr, JavaExecutor java, Object result, String baseUrl) {
        try {
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("java", java);
            bindings.put("result", result);
            bindings.put("baseUrl", baseUrl);
            return Assistant.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }
}
