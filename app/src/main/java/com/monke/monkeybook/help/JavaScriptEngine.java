package com.monke.monkeybook.help;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JavaScriptEngine {

    private JavaScriptEngine() {

    }

    private static class EngineSingleton {
        private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");
    }

    public static ScriptEngine getEngine() {
        return EngineSingleton.SCRIPT_ENGINE;
    }
}
