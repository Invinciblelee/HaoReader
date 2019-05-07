package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public final class Global {

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    private Global() {
    }

    public static boolean isJson(String string) {
        try {
            GSON.fromJson(string, Object.class);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }
}
