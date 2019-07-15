package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.StringUtils;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public final class Assistant {

    private static final String TAG = Assistant.class.getSimpleName();

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    private Assistant() {
    }

    public static boolean isPrimitiveJson(Object object) {
        if (object instanceof List || object instanceof Map) {
            return true;
        } else {
            return StringUtils.isJsonType(StringUtils.valueOf(object));
        }
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

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T fromJson(Reader reader, Type type) {
        return GSON.fromJson(reader, type);
    }

    public static <T> T fromJson(JsonReader reader, Type type) {
        return GSON.fromJson(reader, type);
    }

    public static <T> T fromJson(JsonElement element, Type type) {
        return GSON.fromJson(element, type);
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static String toJson(JsonElement element) {
        return GSON.toJson(element);
    }
}
