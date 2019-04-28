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
            String result = getSingleResultContent(pattern);
            if (!isEmpty(result)) {
                builder.append(result);
                if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    private String getSingleResultContent(RulePattern rulePattern) {
        if (rulePattern.isSimpleJS) {
            return evalStringScript(getParser().getStringSource(), rulePattern);
        } else if (rulePattern.isRedirect) {
            String source = evalStringScript(getParser().getStringSource(), rulePattern);
            RulePattern pattern = fromSingleRule(rulePattern.redirectRule, false);
            return processResultContent(getParser().parseString(source, pattern.elementsRule), pattern);
        } else {
            return processResultContent(getParser().getString(rulePattern.elementsRule), rulePattern);
        }
    }

    @Override
    public String getResultUrl(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        for (RulePattern pattern : rulePatterns.patterns) {
            final String result;
            if (pattern.isSimpleJS) {
                if (RuleMode.Default == pattern.elementsRule.getMode()) {
                    final List<String> list = evalArrayScript(getParser().getStringSource(), pattern);
                    result = list.isEmpty() ? "" : list.get(0);
                } else {
                    result = evalStringScript(getParser().getStringSource(), pattern);
                }
            } else if (pattern.isRedirect) {
                String source = evalStringScript(getParser().getStringSource(), pattern);
                RulePattern newPattern = fromSingleRule(pattern.redirectRule, true);
                result = getParser().parseStringFirst(source, newPattern.elementsRule);
            } else {
                result = getParser().getStringFirst(pattern.elementsRule);
            }
            if (!isEmpty(result)) {
                return processResultUrl(result, pattern);
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
            final List<String> result;
            if (pattern.isSimpleJS) {
                result = evalArrayScript(getParser().getStringSource(), pattern);
            } else if (pattern.isRedirect) {
                String source = evalStringScript(getParser().getStringSource(), pattern);
                RulePattern newPattern = fromSingleRule(pattern.redirectRule, false);
                result = getParser().parseStringList(source, newPattern.elementsRule);
            } else {
                result = getParser().getStringList(pattern.elementsRule);
            }
            if (!result.isEmpty()) {
                processResultContents(result, pattern);
                resultList.addAll(result);
                if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                    break;
                }
            }
        }
        return resultList;
    }

    @Override
    public String parseResultContent(String string, String rule) {
        if (string == null || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final StringBuilder builder = new StringBuilder();
        for (RulePattern pattern : rulePatterns.patterns) {
            String result = processResultContent(getParser().parseString(string, pattern.elementsRule), pattern);
            if (!isEmpty(result)) {
                builder.append(result);
                if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String parseResultUrl(String string, String rule) {
        if (string == null || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), true);
        for (RulePattern pattern : rulePatterns.patterns) {
            final String result;
            if (pattern.isSimpleJS) {
                if (RuleMode.Default == pattern.elementsRule.getMode()) {
                    final List<String> list = evalArrayScript(getParser().getStringSource(), pattern);
                    result = list.isEmpty() ? "" : list.get(0);
                } else {
                    result = evalStringScript(getParser().getStringSource(), pattern);
                }
            } else {
                result = getParser().parseStringFirst(string, pattern.elementsRule);
            }
            if (!isEmpty(result)) {
                return processResultUrl(result, pattern);
            }
        }
        return "";
    }

    @Override
    public List<String> parseResultContents(String source, String rule) {
        if (isEmpty(rule)) {
            return new ArrayList<>();
        }
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
        final List<String> resultList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            final List<String> result;
            if (pattern.isSimpleJS) {
                result = evalArrayScript(source, pattern);
            } else {
                result = getParser().parseStringList(source, pattern.elementsRule);
            }
            if (!result.isEmpty()) {
                processResultContents(result, pattern);
                resultList.addAll(result);
                if (rulePatterns.mergeType == RulePatterns.RULE_MERGE_OR) {
                    break;
                }
            }
        }
        return resultList;
    }

    @Override
    public Map<String, String> getVariableMap(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return new HashMap<>();
        }
        final Map<String, String> resultMap = new HashMap<>();
        final VariablesPattern variablesPattern = VariablesPattern.fromRule(rule);
        if (variablesPattern.putterMap.isEmpty()) {
            return resultMap;
        } else {
            for (Map.Entry<String, String> entry : variablesPattern.putterMap.entrySet()) {
                String value = getResultContent(entry.getValue());
                if (!isEmpty(value)) {
                    resultMap.put(entry.getKey(), value);
                }
            }
        }
        return resultMap;
    }


    @Override
    public AnalyzeCollection getRawCollection(String rule) {
        if (getParser().isSourceEmpty() || isEmpty(rule)) {
            return new AnalyzeCollection(getAnalyzer(), new ArrayList<>());
        }
        return new AnalyzeCollection(getAnalyzer(), getRawList(rule));
    }

    private List<Object> getRawList(String rule) {
        final RulePatterns rulePatterns = fromRule(rule.trim(), false);
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
        if (rulePattern.isRedirect) {
            String source = evalStringScript(getParser().getStringSource(), rulePattern);
            RulePattern pattern = fromSingleRule(rulePattern.redirectRule, false);
            return getParser().parseList(source, pattern.elementsRule);
        } else {
            return getParser().getList(rulePattern.elementsRule);
        }
    }
}
