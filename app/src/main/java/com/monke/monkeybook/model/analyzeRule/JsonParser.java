package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;

class JsonParser {

    private JsonParser() {

    }

    static List<Object> optList(@NonNull ReadContext context, String rule) {
        try {
            return context.read(rule);
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    static String optString(@NonNull ReadContext context, String rule) {
        String result = "";
        if (TextUtils.isEmpty(rule)) return result;
        try {
            Object object = context.read(rule);
            if (object instanceof List) {
                object = ((List) object).get(0);
            }
            result = String.valueOf(object);
        } catch (Exception ignore) {
        }
        return result;
    }
}
