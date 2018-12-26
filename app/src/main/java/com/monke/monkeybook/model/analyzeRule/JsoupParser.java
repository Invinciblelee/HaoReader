package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

class JsoupParser {

    private JsoupParser() {

    }

    /**
     * 获取Elements
     */
    static Elements getElements(Element temp, String rawRule) {
        Elements elements = new Elements();
        if (temp == null || isEmpty(rawRule)) {
            return elements;
        }
        String elementsType;
        String[] ruleStrS;
        if (rawRule.contains("&")) {
            elementsType = "&";
            ruleStrS = rawRule.split("&+");
        } else if (rawRule.contains("%")) {
            elementsType = "%";
            ruleStrS = rawRule.split("%+");
        } else {
            elementsType = "|";
            ruleStrS = rawRule.split("\\|+");
        }
        List<Elements> elementsList = new ArrayList<>();
        for (String ruleStr : ruleStrS) {
            Elements tempS = getElementsSingle(temp, ruleStr);
            elementsList.add(tempS);
            if (elements.size() > 0 && elementsType.equals("|")) {
                break;
            }
        }
        if (!elementsList.isEmpty()) {
            if (TextUtils.equals(elementsType, "%")) {
                for (int i = 0, size = elementsList.get(0).size(); i < size; i++) {
                    for (Elements es : elementsList) {
                        if (i < es.size()) {
                            elements.add(es.get(i));
                        }
                    }
                }
            } else {
                for (Elements es : elementsList) {
                    elements.addAll(es);
                }
            }
        }
        return elements;
    }

    /**
     * 获取Elements按照一个规则
     */
    static Elements getElementsSingle(Element temp, String rawRule) {
        Elements elements = new Elements();
        try {
            String[] ruleS = rawRule.split("@");
            if (ruleS.length > 1) {
                elements.add(temp);
                for (String rule : ruleS) {
                    Elements es = new Elements();
                    for (Element et : elements) {
                        es.addAll(getElements(et, rule));
                    }
                    elements.clear();
                    elements.addAll(es);
                }
            } else {
                String[] rulePcx = rawRule.split("!");
                String[] rulePc = rulePcx[0].trim().split(">");
                String[] rules = rulePc[0].trim().split("\\.");
                String[] filterRules = ensureFilterRules(rulePc);
                switch (rules[0]) {
                    case "children":
                        Elements children = temp.children();
                        if (filterRules != null) {
                            children = filterElements(children, filterRules);
                        }
                        elements.addAll(children);
                        break;
                    case "class":
                        Elements elementsByClass = temp.getElementsByClass(rules[1]);
                        if (rules.length == 3) {
                            int index = Integer.parseInt(rules[2]);
                            if (index < 0) {
                                elements.add(elementsByClass.get(elementsByClass.size() + index));
                            } else {
                                elements.add(elementsByClass.get(index));
                            }
                        } else {
                            if (filterRules != null) {
                                elementsByClass = filterElements(elementsByClass, filterRules);
                            }
                            elements.addAll(elementsByClass);
                        }
                        break;
                    case "tag":
                        Elements elementsByTag = temp.getElementsByTag(rules[1]);
                        if (rules.length == 3) {
                            int index = Integer.parseInt(rules[2]);
                            if (index < 0) {
                                elements.add(elementsByTag.get(elementsByTag.size() + index));
                            } else {
                                elements.add(elementsByTag.get(index));
                            }
                        } else {
                            if (filterRules != null) {
                                elementsByTag = filterElements(elementsByTag, filterRules);
                            }
                            elements.addAll(elementsByTag);
                        }
                        break;
                    case "id":
                        elements.add(temp.getElementById(rules[1]));
                        break;
                    case "text":
                        Elements elementsByText = temp.getElementsContainingOwnText(rules[1]);
                        if (filterRules != null) {
                            elementsByText = filterElements(elementsByText, filterRules);
                        }
                        elements.addAll(elementsByText);
                        break;
                }
                if (rulePcx.length > 1) {
                    String[] rulePcs = rulePcx[1].split(":");
                    Elements removes = new Elements();
                    if (rulePcs.length < elements.size() - 1) {
                        for (String pc : rulePcs) {
                            int pcInt = Integer.parseInt(pc);
                            if (pcInt < 0 && elements.size() + pcInt >= 0) {
                                removes.add(elements.get(elements.size() + pcInt));
                            } else if (pcInt < elements.size()) {
                                removes.add(elements.get(pcInt));
                            }
                        }
                    }
                    elements.removeAll(removes);
                }
            }
        } catch (Exception ignore) {
        }
        return elements;
    }

    private static String[] ensureFilterRules(String[] rawRules) {
        String[] filterRules = null;
        boolean valid = rawRules.length > 1 && !isEmpty(rawRules[1]);
        if (valid) {
            filterRules = rawRules[1].split("\\.");
            List<String> validKeys = Arrays.asList("class", "id", "tag", "text");
            if (filterRules.length < 2 || !validKeys.contains(filterRules[0]) || isEmpty(filterRules[1])) {
                return null;
            }
        }
        return filterRules;
    }

    private static Elements filterElements(Elements elements, String[] rules) {
        if (rules == null || rules.length < 2) return elements;
        Elements selectedEls = new Elements();
        for (Element ele : elements) {
            boolean isOk = false;
            switch (rules[0]) {
                case "class":
                    isOk = ele.getElementsByClass(rules[1]).size() > 0;
                    break;
                case "id":
                    isOk = ele.getElementById(rules[1]) != null;
                    break;
                case "tag":
                    isOk = ele.getElementsByTag(rules[1]).size() > 0;
                    break;
                case "text":
                    isOk = ele.getElementsContainingOwnText(rules[1]).size() > 0;
                    break;
            }
            if (isOk) {
                selectedEls.add(ele);
            }
        }
        return selectedEls;
    }
}
