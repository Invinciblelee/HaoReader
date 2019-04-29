package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CSSParser extends SourceParser<Element> {

    private static final String TAG = "CSS";

    @Override
    String sourceToString(Object source) {
        if (source instanceof String) {
            return (String) source;
        } else if (source instanceof Element) {
            return source.toString();
        }
        return "";
    }

    @Override
    Element fromSource(Object source) {
        Objects.requireNonNull(source);

        if (source instanceof String) {
            return Jsoup.parse((String) source);
        } else if (source instanceof Element) {
            return (Element) source;
        }
        throw new IllegalAccessError("JsoupParser can not support the source type");
    }

    @Override
    List<Object> getList(Rule rule) {
        String ruleStr = rule.getRule();
        if (isOuterBody(ruleStr)) {
            return Collections.singletonList(getSource());
        }
        return ListUtils.toObjectList(parseList(getSource(), ruleStr));
    }

    @Override
    List<Object> parseList(String source, Rule rule) {
        return null;
    }

    private List<Element> parseList(Element temp, String rule) {
        if (TextUtils.isEmpty(rule)) {
            return ListUtils.mutableList();
        }
        try {
            return temp.select(rule);
        } catch (Exception e) {
            Logger.e(TAG, rule, e);
        }
        return ListUtils.mutableList();
    }

    @Override
    String getString(Rule rule) {
        return null;
    }

    @Override
    String parseString(String source, Rule rule) {
        return null;
    }

    @Override
    String getStringFirst(Rule rule) {
        return null;
    }

    @Override
    String parseStringFirst(String source, Rule rule) {
        return null;
    }

    @Override
    List<String> getStringList(Rule rule) {
        return null;
    }

    @Override
    List<String> parseStringList(String source, Rule rule) {
        return null;
    }
}
