package com.monke.monkeybook.model.analyzeRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class DefaultAnalyzerPresenter<S> extends BaseAnalyzerPresenter<S> {

    DefaultAnalyzerPresenter(OutAnalyzer<S> analyzer) {
        super(analyzer);
    }

    @Override
    public String getResultContent(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return "";
        }

        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final StringBuilder builder = new StringBuilder();
        for (RulePattern pattern : rulePatterns.patterns) {
            boolean haveResult = false;
            if (pattern.isSimpleJS) {
                final String result = evalStringScript(getParser().getPrimitive(), pattern);
                if (!isEmpty(result)) {
                    builder.append(matchRegex(result, pattern));
                    haveResult = true;
                }
            } else if (pattern.isRedirect) {
                final String source = evalStringScript(getParser().getPrimitive(), pattern);
                final RulePattern newPattern = fromSingleRule(pattern.redirectRule, false);
                final String result = getParser().parseString(source, newPattern.elementsRule);
                if (!isEmpty(result)) {
                    builder.append(processResultContent(result, newPattern));
                    haveResult = true;
                }
            } else {
                final String result = getParser().getString(pattern.elementsRule);
                if (!isEmpty(result)) {
                    builder.append(processResultContent(result, pattern));
                    haveResult = true;
                }
            }
            if (haveResult && rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                break;
            }
        }
        return builder.toString();
    }


    @Override
    public String getResultUrl(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return "";
        }

        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        for (RulePattern pattern : rulePatterns.patterns) {
            if (pattern.isSimpleJS) {
                final String result = evalStringScript(getParser().getPrimitive(), pattern);
                if (!isEmpty(result)) {
                    return processRawUrl(result, pattern);
                }
            } else if (pattern.isRedirect) {
                final String source = evalStringScript(getParser().getPrimitive(), pattern);
                final RulePattern newPattern = fromSingleRule(pattern.redirectRule, true);
                final String result = getParser().parseStringFirst(source, newPattern.elementsRule);
                if (!isEmpty(result)) {
                    return processResultUrl(result, newPattern);
                }
            } else {
                final String result = getParser().getStringFirst(pattern.elementsRule);
                if (!isEmpty(result)) {
                    return processResultUrl(result, pattern);
                }
            }
        }
        return "";
    }

    @Override
    public List<String> getResultContents(String rule) {
        if (isEmpty(rule)) {
            return new ArrayList<>();
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final List<String> resultList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            boolean haveResult = false;
            if (pattern.isSimpleJS) {
                final List<String> result = evalStringArrayScript(getParser().getPrimitive(), pattern);
                if (!result.isEmpty()) {
                    matchRegexes(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            } else if (pattern.isRedirect) {
                final String source = evalStringScript(getParser().getPrimitive(), pattern);
                final RulePattern newPattern = fromSingleRule(pattern.redirectRule, false);
                final List<String> result = getParser().parseStringList(source, newPattern.elementsRule);
                if (!result.isEmpty()) {
                    processResultContents(result, newPattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            } else {
                final List<String> result = getParser().getStringList(pattern.elementsRule);
                if (!result.isEmpty()) {
                    processResultContents(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            }
            if (haveResult && rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                break;
            }
        }
        return resultList;
    }

    @Override
    public List<String> getResultUrls(String rule) {
        if (isEmpty(rule)) {
            return new ArrayList<>();
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final List<String> resultList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            boolean haveResult = false;
            if (pattern.isSimpleJS) {
                final List<String> result = evalStringArrayScript(getParser().getPrimitive(), pattern);
                if (!result.isEmpty()) {
                    processRawUrls(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            } else if (pattern.isRedirect) {
                final String source = evalStringScript(getParser().getPrimitive(), pattern);
                final RulePattern newPattern = fromSingleRule(pattern.redirectRule, false);
                final List<String> result = getParser().parseStringList(source, newPattern.elementsRule);
                if (!result.isEmpty()) {
                    processResultUrls(result, newPattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            } else {
                final List<String> result = getParser().getStringList(pattern.elementsRule);
                if (!result.isEmpty()) {
                    processResultUrls(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            }
            if (haveResult && rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                break;
            }
        }
        return resultList;
    }


    @Override
    public String parseResultContent(Object source, String rule) {
        if (source == null || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final StringBuilder builder = new StringBuilder();
        for (RulePattern pattern : rulePatterns.patterns) {
            boolean haveResult = false;
            if (pattern.isSimpleJS) {
                final String result = evalStringScript(source, pattern);
                if (!isEmpty(result)) {
                    builder.append(matchRegex(result, pattern));
                    haveResult = true;
                }
            } else {
                final String result = getParser().parseString(source, pattern.elementsRule);
                if (!isEmpty(result)) {
                    builder.append(processResultContent(result, pattern));
                    haveResult = true;
                }
            }
            if (haveResult && rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                break;
            }
        }
        return builder.toString();
    }

    @Override
    public String parseResultUrl(Object source, String rule) {
        if (source == null || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        for (RulePattern pattern : rulePatterns.patterns) {
            if (pattern.isSimpleJS) {
                final String result = evalStringScript(source, pattern);
                if (!isEmpty(result)) {
                    return processRawUrl(result, pattern);
                }
            } else {
                final String result = getParser().parseStringFirst(source, pattern.elementsRule);
                if (!isEmpty(result)) {
                    return processResultUrl(result, pattern);
                }
            }
        }
        return "";
    }

    @Override
    public List<String> parseResultContents(Object source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        final List<String> resultList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            boolean haveResult = false;
            if (pattern.isSimpleJS) {
                final List<String> result = evalStringArrayScript(source, pattern);
                if (!result.isEmpty()) {
                    matchRegexes(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            } else {
                final List<String> result = getParser().parseStringList(source, pattern.elementsRule);
                if (!result.isEmpty()) {
                    processResultContents(result, pattern);
                    resultList.addAll(result);
                    haveResult = true;
                }
            }
            if (haveResult && rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                break;
            }
        }
        return resultList;
    }

    @Override
    public Map<String, String> putVariableMap(String rule, int flag) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return getVariableStore().getVariableMap();
        }
        final Map<String, String> resultMap = new HashMap<>();
        final VariablesPattern variablesPattern = VariablesPattern.fromPutterRule(rule, flag);
        if (variablesPattern.map.isEmpty()) {
            return resultMap;
        } else {
            for (Map.Entry<String, String> entry : variablesPattern.map.entrySet()) {
                String value = getResultContent(entry.getValue());
                if (!isEmpty(value)) {
                    resultMap.put(entry.getKey(), value);
                }
            }
        }
        return getVariableStore().putVariableMap(resultMap);
    }


    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return new AnalyzeCollection(new ArrayList<>());
        }
        return new AnalyzeCollection(getRawList(rule));
    }

    private List<Object> getRawList(String rule) {
        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        final List<Object> resultList = new ArrayList<>();
        final List<List<Object>> resultsList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            List<Object> list = getSingleRawList(pattern);
            if (!list.isEmpty()) {
                resultsList.add(list);

                if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                    break;
                }
            }
        }

        if (!resultsList.isEmpty()) {
            if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_FILTER) {
                for (int i = 0, size = resultsList.get(0).size(); i < size; i++) {
                    for (List<Object> list : resultsList) {
                        if (i < list.size()) {
                            resultList.add(list.get(i));
                        }
                    }
                }
            } else {
                for (List<Object> list : resultsList) {
                    resultList.addAll(list);
                }
            }
        }

        return resultList;
    }

    private List<Object> getSingleRawList(RulePattern rulePattern) {
        if (rulePattern.isSimpleJS) {
            return evalObjectArrayScript(getParser().getPrimitive(), rulePattern);
        } else if (rulePattern.isRedirect) {
            String source = evalStringScript(getParser().getPrimitive(), rulePattern);
            RulePattern pattern = fromSingleRule(rulePattern.redirectRule, false);
            return getParser().parseList(source, pattern.elementsRule);
        } else {
            return getParser().getList(rulePattern.elementsRule);
        }
    }
}
