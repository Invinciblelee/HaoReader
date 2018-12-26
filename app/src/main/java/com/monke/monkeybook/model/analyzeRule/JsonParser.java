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

    static List<Object> optList(@NonNull ReadContext context, String rawRule) {
        try {
            return context.read(rawRule);
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    static String optString(@NonNull ReadContext context, String rawRule) {
        String result = "";
        if (TextUtils.isEmpty(rawRule)) return result;
        if (!rawRule.contains("{")) {
            String[] rules;
            boolean isAnd;
            if(rawRule.contains("&")){
                rules = rawRule.split("&+");
                isAnd = true;
            }else {
                rules = rawRule.split("\\|+");
                isAnd = false;
            }
            result = readAll(context, rules, isAnd);
            return result;
        } else {
            result = rawRule;
            Pattern pattern = Pattern.compile("(?<=\\{).+?(?=\\})");
            Matcher matcher = pattern.matcher(rawRule);
            while (matcher.find()) {
                result = result.replace(String.format("{%s}", matcher.group()), optString(context, matcher.group()));
            }
            return result;
        }
    }

    private static String readAll(@NonNull ReadContext context, String[] rules, boolean isAnd){
        StringBuilder result = new StringBuilder();
        for (String rule : rules) {
            Object object = null;
            try {
                object = context.read(rule);
                if (object instanceof List) {
                    object = ((List) object).get(0);
                }
            } catch (Exception ignore) {
            }
            if (object != null) {
                result.append(String.valueOf(object));

                if(!isAnd) break;
            }
        }
        return result.toString();
    }
}
