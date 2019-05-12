package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

final class CSSParser extends SourceParser<Element> {

    private static final String TAG = "CSS";

    @Override
    String parseObject(Object source) {
        return StringUtils.valueOf(source);
    }

    @Override
    Element fromObject(Object source) {
        if (source instanceof String) {
            return Jsoup.parse((String) source);
        } else if (source instanceof Element) {
            return (Element) source;
        } else if (source instanceof JXNode) {
            JXNode jxNode = (JXNode) source;
            if (jxNode.isElement()) {
                return jxNode.asElement();
            } else {
                return new Element(jxNode.toString());
            }
        }
        return Jsoup.parse(StringUtils.valueOf(source));
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
    List<Object> parseList(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        return ListUtils.toObjectList(parseList(fromObject(source), ruleStr));
    }

    private List<Element> parseList(Element temp, String rule) {
        if (isEmpty(rule)) {
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
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
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
        if (isEmpty(ruleStr)) {
            return "";
        }
        return parseString(fromObject(source), ruleStr);
    }


    private String parseString(Element source, String rule) {
        final List<String> textS = parseStringList(source, rule);
        if (textS.isEmpty()) {
            return "";
        }
        return StringUtils.join("\n", textS);
    }

    @Override
    String getStringFirst(Rule rule) {
        if (isOuterBody(rule.getRule())) {
            return getStringSource();
        }
        final List<String> result = getStringList(rule);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return "";
    }

    @Override
    String parseStringFirst(Object source, Rule rule) {
        final List<String> result = parseStringList(source, rule);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return "";
    }

    @Override
    List<String> getStringList(Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
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
        if (isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }
        return parseStringList(fromObject(source), ruleStr);
    }

    private List<String> parseStringList(Element element, String rule) {
        final int lastIndex = rule.lastIndexOf('@');
        if (lastIndex > 0) {
            final String cssRule = rule.substring(0, lastIndex);
            final String lastRule = rule.substring(lastIndex + 1);
            return parseLastResult(element.select(cssRule), lastRule);
        } else {
            final Elements elements = new Elements(element);
            return parseLastResult(elements, rule);
        }
    }

    private List<String> parseLastResult(Elements elements, String lastRule) {
        final List<String> textS = new ArrayList<>();
        try {
            switch (lastRule) {
                case "text":
                    for (Element element : elements) {
                        String text = element.text();
                        if (!isEmpty(text)) {
                            textS.add(text);
                        }
                    }
                    break;
                case "textNodes":
                    for (Element element : elements) {
                        List<TextNode> contentEs = element.textNodes();
                        for (int i = 0; i < contentEs.size(); i++) {
                            String text = contentEs.get(i).text();
                            if (!isEmpty(text)) {
                                textS.add(text);
                            }
                        }
                    }
                    break;
                case "ownText":
                    List<String> keptTags = Arrays.asList("br", "b", "em", "strong");
                    for (Element element : elements) {
                        Element ele = element.clone();
                        for (Element child : ele.children()) {
                            if (!keptTags.contains(child.tagName())) {
                                child.remove();
                            }
                        }
                        String text = ele.html();
                        if (!isEmpty(text)) {
                            textS.add(text);
                        }
                    }
                    break;
                case "html":
                    elements.select("script").remove();
                    String text = elements.html();
                    if (!isEmpty(text)) {
                        textS.add(text);
                    }
                    break;
                default:
                    for (Element element : elements) {
                        String attr = element.attr(lastRule);
                        if (!isEmpty(attr) && !textS.contains(attr)) {
                            textS.add(attr);
                        }
                    }
            }
        } catch (Exception e) {
            Logger.e(TAG, lastRule, e);
        }
        return textS;
    }
}
