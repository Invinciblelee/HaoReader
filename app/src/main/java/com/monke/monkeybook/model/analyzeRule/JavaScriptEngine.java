package com.monke.monkeybook.model.analyzeRule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

enum JavaScriptEngine {

    INSTANCE;

    private ScriptEngine scriptEngine;

    JavaScriptEngine() {
        scriptEngine = new ScriptEngineManager().getEngineByName("rhino");
    }

    public ScriptEngine getEngine() {
        return scriptEngine;
    }
}
