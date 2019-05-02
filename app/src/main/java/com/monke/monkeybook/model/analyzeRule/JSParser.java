package com.monke.monkeybook.model.analyzeRule;


import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

final class JSParser {

    private JSParser() {

    }

    private static class JavaScriptEngine {
        private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("rhino");
    }

    static String evalStringScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        return (String) evalObjectScript(jsStr, java, result, baseUrl);
    }

    static List<String> evalArrayScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        final Object object = evalObjectScript(jsStr, java, result, baseUrl);
        final List<String> resultList = new ArrayList<>();
        if (object instanceof List) {
            for (Object obj : (List) object) {
                resultList.add(StringUtils.valueOf(obj));
            }
        } else {
            resultList.add(StringUtils.valueOf(object));
        }
        return resultList;
    }

    private static Object evalObjectScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        try {
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("java", java);
            bindings.put("result", result);
            bindings.put("baseUrl", baseUrl);
            return JavaScriptEngine.scriptEngine.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e("Rhino", jsStr, e);
        }
        return "";
    }

}
