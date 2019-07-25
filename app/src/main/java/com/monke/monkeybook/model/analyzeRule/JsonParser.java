package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_JSON;

final class JsonParser extends SourceParser<ReadContext> {

    private static final String TAG = "JSON";

    JsonParser() {
    }

    @Override
    String parseObject(Object source) {
        if (source == null) {
            return "";
        }

        final ReadContext context = fromObject(source);
        Object json = context.json();
        if (json instanceof List || json instanceof Map) {
            return context.jsonString();
        }

        return StringUtils.valueOf(json);
    }

    @Override
    ReadContext fromObject(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof ReadContext) {
            return (ReadContext) source;
        } else if (source instanceof String) {
            return JsonPath.parse((String) source);//必须强转为String
        }
        return JsonPath.parse(source);
    }

    @Override
    List<Object> getList(Rule rule) {
        String ruleStr = rule.getRule();
        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(getSource());
        }
        return parseList(getSource(), ruleStr);
    }

    @Override
    List<Object> parseList(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        return parseList(fromObject(source), ruleStr);
    }

    private List<Object> parseList(ReadContext source, String rule) {
        if (TextUtils.isEmpty(rule)) {
            return ListUtils.mutableList();
        }
        try {
            return source.read(rule);
        } catch (Exception e) {
            Logger.e(TAG, rule, e);
        }
        return ListUtils.mutableList();
    }

    @Override
    String getString(Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return "";
        }

        if (isOuterBody(ruleStr)) {
            return getStringSource();
        }

        return parseString(getSource(), ruleStr);
    }

    @Override
    String parseString(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return "";
        }
        return parseString(fromObject(source), ruleStr);
    }

    @Override
    String getStringFirst(Rule rule) {
        return getString(rule);
    }

    @Override
    String parseStringFirst(Object source, Rule rule) {
        return parseString(source, rule);
    }

    private String parseString(ReadContext source, String rule) {
        if (!rule.contains("{$.")) {
            try {
                return StringUtils.join("\n", ListUtils.toStringList(source.read(rule)));
            } catch (Exception e) {
                Logger.e(TAG, rule, e);
            }
            return "";
        } else {
            String result = rule;
            Matcher matcher = PATTERN_JSON.matcher(rule);
            while (matcher.find()) {
                final String group = matcher.group();
                final String string = parseString(source, group);
                result = result.replace(String.format("{%s}", group), string);
            }
            return result;
        }
    }


    @Override
    List<String> getStringList(Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }

        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(getStringSource());
        }

        return parseStringList(getSource(), ruleStr);
    }

    @Override
    List<String> parseStringList(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }
        return parseStringList(fromObject(source), ruleStr);
    }

    private List<String> parseStringList(ReadContext source, String rule) {
        if (!rule.contains("{$.")) {
            try {
                return ListUtils.toStringList(source.read(rule));
            } catch (Exception e) {
                Logger.e(TAG, rule, e);
            }
            return ListUtils.mutableList();
        } else {
            final List<String> resultList = new ArrayList<>();
            Matcher matcher = PATTERN_JSON.matcher(rule);
            while (matcher.find()) {
                final String group = matcher.group();
                final List<String> stringList = parseStringList(source, group);
                for (String string : stringList) {
                    resultList.add(rule.replace(String.format("{%s}", group), string));
                }
            }
            return resultList;
        }
    }


}
