package com.monke.monkeybook.model.analyzeRule;

import android.util.Log;

import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.StringUtils;

import java.util.List;

final class HybridParser extends SourceParser<Object> {

    private JsoupParser jsoupParser;
    private JsonParser jsonParser;
    private XPathParser xPathParser;
    private CSSParser cssParser;

    private SourceParser<?> currentParser;

    private boolean sourceChangedXP = false;
    private boolean sourceChangedJS = false;
    private boolean sourceChangedJP = false;
    private boolean sourceChangedCS = false;

    private boolean isJSon = false;

    private void ensureJsoupParser() {
        if (jsoupParser == null) {
            jsoupParser = new JsoupParser();
            jsoupParser.setContent(getSource());
        } else if (sourceChangedJP) {
            jsoupParser.setContent(getSource());
            sourceChangedJP = false;
        }
        currentParser = jsoupParser;
    }

    private void ensureJsonParser() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
            jsonParser.setContent(getSource());
        } else if (sourceChangedJS) {
            jsonParser.setContent(getSource());
            sourceChangedJS = false;
        }
        currentParser = jsonParser;
    }

    private void ensureXPathParser() {
        if (xPathParser == null) {
            xPathParser = new XPathParser();
            xPathParser.setContent(getSource());
        } else if (sourceChangedXP) {
            xPathParser.setContent(getSource());
            sourceChangedXP = false;
        }
        currentParser = xPathParser;
    }

    private void ensureCSSParser() {
        if (cssParser == null) {
            cssParser = new CSSParser();
            cssParser.setContent(getSource());
        } else if (sourceChangedCS) {
            cssParser.setContent(getSource());
            sourceChangedCS = false;
        }
        currentParser = cssParser;
    }

    private SourceParser<?> getCurrentParser(RuleMode mode) {
        switch (mode) {
            case XPath:
                ensureXPathParser();
                break;
            case JSon:
                ensureJsonParser();
                break;
            case CSS:
                ensureCSSParser();
                break;
            case Default:
            default:
                if (isJSon) {
                    ensureJsonParser();
                } else {
                    ensureJsoupParser();
                }

        }
        return currentParser;
    }

    @Override
    boolean isSourceEmpty() {
        return false;
    }

    @Override
    String parseObject(Object source) {
        if (currentParser == null) {
            return StringUtils.valueOf(source);
        }
        return currentParser.parseObject(source);
    }

    @Override
    Object fromObject(Object source) {
        sourceChangedXP = true;
        sourceChangedJS = true;
        sourceChangedJP = true;
        sourceChangedCS = true;
        isJSon = Global.isJson(StringUtils.valueOf(source));
        return source;
    }

    @Override
    List<Object> getList(Rule rule) {
        return getCurrentParser(rule.getMode()).getList(rule);
    }

    @Override
    List<Object> parseList(String source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseList(source, rule);
    }

    @Override
    String getString(Rule rule) {
        return getCurrentParser(rule.getMode()).getString(rule);
    }

    @Override
    String parseString(String source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseString(source, rule);
    }

    @Override
    String getStringFirst(Rule rule) {
        return getCurrentParser(rule.getMode()).getStringFirst(rule);
    }

    @Override
    String parseStringFirst(String source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseStringFirst(source, rule);
    }

    @Override
    List<String> getStringList(Rule rule) {
        return getCurrentParser(rule.getMode()).getStringList(rule);
    }

    @Override
    List<String> parseStringList(String source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseStringList(source, rule);
    }

}
