package com.monke.monkeybook.model.analyzeRule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

class JSParser {

    private JSParser(){

    }

    private static class EngineHelper {
        private static final ScriptEngine INSTANCE = new ScriptEngineManager().getEngineByName("rhino");
    }

    static String evalJS(String jsStr, Object result, String baseUrl) {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("result", result);
        bindings.put("baseUrl", baseUrl);
        try {
            result = EngineHelper.INSTANCE.eval(jsStr, bindings);
        } catch (ScriptException ignored) {
        }
        return String.valueOf(result);
    }

}
