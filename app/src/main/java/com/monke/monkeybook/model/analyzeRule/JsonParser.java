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
import java.util.Objects;
import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_JSON;

final class JsonParser extends SourceParser<ReadContext> {

    private static final String TAG = "JSON";

    JsonParser() {
    }

    @Override
    String sourceToString(Object source) {
        if (source == null) {
            return "";
        }

        if (source instanceof String) {
            return (String) source;
        }

        final ReadContext context = fromSource(source);
        Object json = context.json();
        if (json instanceof List || json instanceof Map) {
            return context.jsonString();
        }
        return json.toString();
    }

    @Override
    ReadContext fromSource(Object source) {
        Objects.requireNonNull(source);

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
    List<Object> parseList(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(source);
        }
        return parseList(fromSource(source), ruleStr);
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
    String parseString(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return "";
        }

        if (isOuterBody(ruleStr)) {
            return source;
        }

        return parseString(fromSource(source), ruleStr);
    }

    @Override
    String getStringFirst(Rule rule) {
        return getString(rule);
    }

    @Override
    String parseStringFirst(String source, Rule rule) {
        return parseString(source, rule);
    }

    private String parseString(ReadContext source, String rule) {
        if (!rule.contains("{$.")) {
            final StringBuilder content = new StringBuilder();
            try {
                final Object object = source.read(rule);
                if (object instanceof List) {
                    for (Object o : (List) object) {
                        content.append(StringUtils.valueOf(o)).append("\n");
                    }
                } else {
                    content.append(StringUtils.valueOf(object));
                }
            } catch (Exception e) {
                Logger.e(TAG, rule, e);
            }
            return content.toString();
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
    List<String> parseStringList(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (TextUtils.isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }

        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(source);
        }

        return parseStringList(fromSource(source), ruleStr);
    }

    private List<String> parseStringList(ReadContext source, String rule) {
        final List<String> resultList = new ArrayList<>();

        if (!rule.contains("{$.")) {
            try {
                Object object = source.read(rule);
                if (object instanceof List) {
                    for (Object o : (List) object)
                        resultList.add(StringUtils.valueOf(o));
                } else {
                    resultList.add(StringUtils.valueOf(object));
                }
            } catch (Exception e) {
                Logger.e(TAG, rule, e);
            }
            return resultList;
        } else {
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
