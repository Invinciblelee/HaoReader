package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.text.TextUtils.isEmpty;

final class CSSParser extends SourceParser<Element> {

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
        String ruleStr = rule.getRule();
        return ListUtils.toObjectList(parseList(fromSource(source), ruleStr));
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
    String parseString(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return "";
        }
        return parseString(fromSource(source), ruleStr);
    }


    private String parseString(Element source, String rule) {
        final List<String> textS = parseStringList(source, rule);
        final StringBuilder content = new StringBuilder();
        for (String text : textS) {
            if (textS.size() > 1) {
                if (text.length() > 0) {
                    if (content.length() > 0) {
                        content.append("\n");
                    }
                    content.append("\u3000\u3000").append(text);
                }
            } else {
                content.append(text);
            }
        }
        return content.toString();
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
    String parseStringFirst(String source, Rule rule) {
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
    List<String> parseStringList(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }
        return parseStringList(fromSource(source), ruleStr);
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
                case "ownText":
                    List<String> keptTags = Arrays.asList("br", "b", "em", "strong");
                    for (Element element : elements) {
                        Element ele = element.clone();
                        for (Element child : ele.children()) {
                            if (!keptTags.contains(child.tagName())) {
                                child.remove();
                            }
                        }
                        String[] htmlS = ele.html().replaceAll("(?i)<br[\\s/]*>", "\n")
                                .replaceAll("<.*?>", "").split("\n");
                        for (String temp : htmlS) {
                            temp = FormatWebText.getContent(temp);
                            if (!isEmpty(temp)) {
                                textS.add(temp);
                            }
                        }
                    }
                    break;
                case "textNodes":
                    for (Element element : elements) {
                        List<TextNode> contentEs = element.textNodes();
                        for (int i = 0; i < contentEs.size(); i++) {
                            String temp = contentEs.get(i).text().trim();
                            temp = FormatWebText.getContent(temp);
                            if (!isEmpty(temp)) {
                                textS.add(temp);
                            }
                        }
                    }
                    break;
                case "html":
                    elements.select("script").remove();
                    String[] htmlS = elements.html().replaceAll("(?i)<(br[\\\\s/]*|p.*?|div.*?|/p|/div)>", "\n")
                            .replaceAll("<.*?>", "")
                            .split("\n");
                    for (String temp : htmlS) {
                        temp = FormatWebText.getContent(temp);
                        if (!isEmpty(temp)) {
                            textS.add(temp);
                        }
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
