package com.monke.monkeybook.model.analyzeRule;

import java.util.List;
import java.util.Objects;

final class HybridParser extends SourceParser<Object> {

    private JsoupParser jsoupParser;
    private JsonParser jsonParser;
    private XPathParser xPathParser;

    private SourceParser<?> currentParser;

    private boolean sourceChangedXP = false;
    private boolean sourceChangedJS = false;
    private boolean sourceChangedJP = false;

    private JsoupParser getJsoupParser() {
        if (jsoupParser == null) {
            jsoupParser = new JsoupParser();
            jsoupParser.setContent(getSource());
        } else if (sourceChangedJP) {
            jsoupParser.setContent(getSource());
            sourceChangedJP = false;
        }
        currentParser = jsoupParser;
        return jsoupParser;
    }

    private JsonParser getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
            jsonParser.setContent(getSource());
        } else if (sourceChangedJS) {
            jsonParser.setContent(getSource());
            sourceChangedJS = false;
        }
        currentParser = jsonParser;
        return jsonParser;
    }

    private XPathParser getXPathParser() {
        if (xPathParser == null) {
            xPathParser = new XPathParser();
            xPathParser.setContent(getSource());
        } else if (sourceChangedXP) {
            xPathParser.setContent(getSource());
            sourceChangedXP = false;
        }
        currentParser = xPathParser;
        return xPathParser;
    }

    @Override
    boolean isSourceEmpty() {
        return false;
    }

    @Override
    String sourceToString(Object source) {
        return currentParser.sourceToString(source);
    }

    @Override
    Object fromSource(Object source) {
        sourceChangedXP = true;
        sourceChangedJS = true;
        sourceChangedJP = true;
        return source;
    }

    @Override
    List<Object> getList(Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().getList(rule);
            case JSon:
                return getJsonParser().getList(rule);
            case Default:
            default:
                return getJsoupParser().getList(rule);
        }
    }

    @Override
    List<Object> parseList(String source, Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().parseList(source, rule);
            case JSon:
                return getJsonParser().parseList(source, rule);
            case Default:
            default:
                return getJsoupParser().parseList(source, rule);
        }
    }

    @Override
    String getString(Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().getString(rule);
            case JSon:
                return getJsonParser().getString(rule);
            case Default:
            default:
                return getJsoupParser().getString(rule);
        }
    }

    @Override
    String parseString(String source, Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().parseString(source, rule);
            case JSon:
                return getJsonParser().parseString(source, rule);
            case Default:
            default:
                return getJsoupParser().parseString(source, rule);
        }
    }

    @Override
    String getStringFirst(Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().getStringFirst(rule);
            case JSon:
                return getJsonParser().getStringFirst(rule);
            case Default:
            default:
                return getJsoupParser().getStringFirst(rule);
        }
    }

    @Override
    String parseStringFirst(String source, Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().parseStringFirst(source, rule);
            case JSon:
                return getJsonParser().parseStringFirst(source, rule);
            case Default:
            default:
                return getJsoupParser().parseStringFirst(source, rule);
        }
    }

    @Override
    List<String> getStringList(Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().getStringList(rule);
            case JSon:
                return getJsonParser().getStringList(rule);
            case Default:
            default:
                return getJsoupParser().getStringList(rule);
        }
    }

    @Override
    List<String> parseStringList(String source, Rule rule) {
        switch (rule.getMode()) {
            case XPath:
                return getXPathParser().parseStringList(source, rule);
            case JSon:
                return getJsonParser().parseStringList(source, rule);
            case Default:
            default:
                return getJsoupParser().parseStringList(source, rule);
        }
    }

}
