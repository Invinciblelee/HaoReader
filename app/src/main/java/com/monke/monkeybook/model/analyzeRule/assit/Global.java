package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public final class Global {

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final Gson GSON = new Gson();

    private Global() {
    }
}
