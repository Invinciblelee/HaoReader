package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (!rule.contains("{")) {
            try {
                Object object = context.read(rule);
                if (object instanceof List) {
                    object = ((List) object).get(0);
                }
                result = String.valueOf(object);
            } catch (Exception ignore) {
            }
            return result;
        } else {
            result = rule;
            Pattern pattern = Pattern.compile("(?<=\\{).+?(?=\\})");
            Matcher matcher = pattern.matcher(rule);
            while (matcher.find()) {
                result = result.replace(String.format("{%s}", matcher.group()), optString(context, matcher.group()));
            }
            return result;
        }
    }
}
