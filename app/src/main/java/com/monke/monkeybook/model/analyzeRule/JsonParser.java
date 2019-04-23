package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.analyzeRule.pattern.Patterns;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_JSON;

final class JsonParser extends SourceParser<ReadContext, Object> {

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

        final ReadContext context;
        if (source instanceof ReadContext) {
            context = (ReadContext) source;
        } else {
            context = fromSource(source);
        }

        Object json = context.json();
        if (json instanceof List || json instanceof Map) {
            return context.jsonString();
        }
        return json.toString();
    }

    @Override
    ReadContext fromSource(String source) {
        return JsonPath.parse(source);
    }

    @Override
    ReadContext fromSource(Object source) {
        return JsonPath.parse(source);
    }

    @Override
    List<Object> getList(String rawRule) {
        if (Patterns.RULE_BODY.equals(rawRule)) {
            return Collections.singletonList(getStringSource());
        }
        return parseList(getSource(), rawRule);
    }

    @Override
    List<Object> parseList(String source, String rawRule) {
        if (Patterns.RULE_BODY.equals(rawRule)) {
            return Collections.singletonList(source);
        }
        return parseList(fromSource(source), rawRule);
    }

    private List<Object> parseList(ReadContext source, String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return Collections.emptyList();
        }

        try {
            return source.read(rawRule);
        } catch (Exception e) {
            Logger.e(TAG, rawRule, e);
        }
        return Collections.emptyList();
    }

    @Override
    String getString(String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return "";
        }

        if (Patterns.RULE_BODY.equals(rawRule)) {
            return getStringSource();
        }

        return parseString(getSource(), rawRule);
    }

    @Override
    String parseString(String source, String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return "";
        }

        if (Patterns.RULE_BODY.equals(rawRule)) {
            return source;
        }

        return parseString(fromSource(source), rawRule);
    }

    private String parseString(ReadContext source, String rawRule) {
        if (!rawRule.contains("{$.")) {
            final StringBuilder content = new StringBuilder();
            try {
                Object object = source.read(rawRule);
                if (object instanceof List) {
                    for (Object o : (List) object) {
                        content.append(StringUtils.valueOf(o)).append("\n");
                    }
                } else {
                    content.append(StringUtils.valueOf(object));
                }
            } catch (Exception e) {
                Logger.e(TAG, rawRule, e);
            }
            return content.toString();
        } else {
            String result = rawRule;
            Matcher matcher = PATTERN_JSON.matcher(rawRule);
            while (matcher.find()) {
                Object object = null;
                try {
                    object = source.read(matcher.group());
                } catch (Exception e) {
                    Logger.e(TAG, rawRule, e);
                }
                result = result.replace(String.format("{%s}", matcher.group()), StringUtils.valueOf(object));
            }
            return result;
        }
    }


    @Override
    List<String> getStringList(String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return Collections.emptyList();
        }

        if (Patterns.RULE_BODY.equals(rawRule)) {
            return ListUtils.mutableList(getStringSource());
        }

        return parseStringList(getSource(), rawRule);
    }

    @Override
    List<String> parseStringList(String source, String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return Collections.emptyList();
        }

        if (Patterns.RULE_BODY.equals(rawRule)) {
            return ListUtils.mutableList(source);
        }

        return parseStringList(fromSource(source), rawRule);
    }

    private List<String> parseStringList(ReadContext source, String rawRule) {
        final List<String> resultList = new ArrayList<>();

        if (!rawRule.contains("{$.")) {
            try {
                Object object = source.read(rawRule);
                if (object instanceof List) {
                    for (Object o : (List) object)
                        resultList.add(StringUtils.valueOf(o));
                } else {
                    resultList.add(StringUtils.valueOf(object));
                }
            } catch (Exception e) {
                Logger.e(TAG, rawRule, e);
            }
            return resultList;
        } else {
            Matcher matcher = PATTERN_JSON.matcher(rawRule);
            while (matcher.find()) {
                List<String> stringList = parseStringList(source, matcher.group());
                for (String string : stringList) {
                    resultList.add(rawRule.replace(String.format("{%s}", matcher.group()), string));
                }
            }
            return resultList;
        }
    }


}
