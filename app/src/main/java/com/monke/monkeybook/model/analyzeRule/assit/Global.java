package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.analyzeRule.JavaExecutor;
import com.monke.monkeybook.utils.StringUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public final class Global {

    private static final String TAG = Global.class.getSimpleName();

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    private static final Collator COLLATOR = Collator.getInstance(Locale.ENGLISH);

    public static final Comparator<String> STRING_COMPARATOR = COLLATOR::compare;

    private Global() {
    }

    public static boolean canConvertToJson(Object object) {
        boolean result = false;
        if (object instanceof List || object instanceof Map) {
            result = true;
        } else {
            String str = StringUtils.valueOf(object);
            if (StringUtils.isNotBlank(str)) {
                str = str.trim();
                if (str.startsWith("{") && str.endsWith("}")) {
                    result = true;
                } else if (str.startsWith("[") && str.endsWith("]")) {
                    result = true;
                }
            }
        }
        return result;
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
            return Global.SCRIPT_ENGINE.eval(jsStr, bindings);
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
            return Global.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }
}
