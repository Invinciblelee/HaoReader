package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
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
            jsoupParser.setSource(getSource());
        } else if (sourceChangedJP) {
            jsoupParser.setSource(getSource());
            sourceChangedJP = false;
        }
        currentParser = jsoupParser;
    }

    private void ensureJsonParser() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
            jsonParser.setSource(getSource());
        } else if (sourceChangedJS) {
            jsonParser.setSource(getSource());
            sourceChangedJS = false;
        }
        currentParser = jsonParser;
    }

    private void ensureXPathParser() {
        if (xPathParser == null) {
            xPathParser = new XPathParser();
            xPathParser.setSource(getSource());
        } else if (sourceChangedXP) {
            xPathParser.setSource(getSource());
            sourceChangedXP = false;
        }
        currentParser = xPathParser;
    }

    private void ensureCSSParser() {
        if (cssParser == null) {
            cssParser = new CSSParser();
            cssParser.setSource(getSource());
        } else if (sourceChangedCS) {
            cssParser.setSource(getSource());
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
    void onAttachSource(Object source) {
        sourceChangedXP = true;
        sourceChangedJS = true;
        sourceChangedJP = true;
        sourceChangedCS = true;
        isJSon = Assistant.isPrimitiveJson(source);
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
        return source;
    }

    @Override
    List<Object> getList(Rule rule) {
        return getCurrentParser(rule.getMode()).getList(rule);
    }

    @Override
    List<Object> parseList(Object source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseList(source, rule);
    }

    @Override
    String getString(Rule rule) {
        return getCurrentParser(rule.getMode()).getString(rule);
    }

    @Override
    String parseString(Object source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseString(source, rule);
    }

    @Override
    String getStringFirst(Rule rule) {
        return getCurrentParser(rule.getMode()).getStringFirst(rule);
    }

    @Override
    String parseStringFirst(Object source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseStringFirst(source, rule);
    }

    @Override
    List<String> getStringList(Rule rule) {
        return getCurrentParser(rule.getMode()).getStringList(rule);
    }

    @Override
    List<String> parseStringList(Object source, Rule rule) {
        return getCurrentParser(rule.getMode()).parseStringList(source, rule);
    }

}
