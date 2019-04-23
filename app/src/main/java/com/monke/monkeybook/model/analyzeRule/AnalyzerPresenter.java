package com.monke.monkeybook.model.analyzeRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class AnalyzerPresenter<S, T> extends BaseAnalyzerPresenter<S, T> {

    AnalyzerPresenter(OutAnalyzer<S, T> analyzer) {
        super(analyzer);
    }

    @Override
    public String getResultContent(String rule) {
        if (getParser().empty() || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim());
        final StringBuilder builder = new StringBuilder();
        for (RulePattern pattern : rulePatterns.patterns) {
            String result = getSingleResultContent(pattern);
            if (!isEmpty(result)) {
                builder.append(result);
                if (rulePatterns.resultType == RulePatterns.RULE_RESULT_OR) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    private String getSingleResultContent(RulePattern rulePattern) {
        if (rulePattern.isSimpleJS) {
            return evalJS(getParser().getStringSource(), rulePattern);
        } else if (rulePattern.isRedirect) {
            String source = evalJS(getParser().getStringSource(), rulePattern);
            RulePattern pattern = RulePattern.fromRule(rulePattern.redirectRule);
            return processResultContent(getParser().parseString(source, pattern.elementsRule), pattern);
        } else {
            return processResultContent(getParser().getString(rulePattern.elementsRule), rulePattern);
        }
    }

    @Override
    public String getResultUrl(String rule) {
        if (getParser().empty() || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim(), getConfig().getVariableStore());
        if (getParser() instanceof JsoupParser) {
            for (RulePattern pattern : rulePatterns.patterns) {
                final List<String> result;
                if (pattern.isSimpleJS) {
                    result = new ArrayList<>();
                    result.add(evalJS(getParser().getStringSource(), pattern));
                } else if (pattern.isRedirect) {
                    String source = evalJS(getParser().getStringSource(), pattern);
                    RulePattern newPattern = RulePattern.fromRule(pattern.redirectRule);
                    result = getParser().parseStringList(source, newPattern.elementsRule);
                } else {
                    result = getParser().getStringList(pattern.elementsRule);
                }
                if (!result.isEmpty()) {
                    return processResultUrl(result.get(0), pattern);
                }
            }
        } else {
            for (RulePattern pattern : rulePatterns.patterns) {
                final String result;
                if (pattern.isSimpleJS) {
                    result = evalJS(getParser().getStringSource(), pattern);
                } else if (pattern.isRedirect) {
                    String source = evalJS(getParser().getStringSource(), pattern);
                    RulePattern newPattern = RulePattern.fromRule(pattern.redirectRule, getConfig().getVariableStore());
                    result = getParser().parseString(source, newPattern.elementsRule);
                } else {
                    result = getParser().getString(pattern.elementsRule);
                }
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
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim());
        final List<String> resultList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            final List<String> result;
            if (pattern.isSimpleJS) {
                result = new ArrayList<>();
                result.add(evalJS(getParser().getStringSource(), pattern));
            } else if (pattern.isRedirect) {
                String source = evalJS(getParser().getStringSource(), pattern);
                RulePattern newPattern = RulePattern.fromRule(pattern.redirectRule);
                result = getParser().parseStringList(source, newPattern.elementsRule);
            } else {
                result = getParser().getStringList(pattern.elementsRule);
            }
            if (!result.isEmpty()) {
                processResultContents(result, pattern);
                resultList.addAll(result);
                if (rulePatterns.resultType == RulePatterns.RULE_RESULT_OR) {
                    break;
                }
            }
        }
        return resultList;
    }


    @Override
    public Map<String, String> getVariableMap(String rule) {
        if (getParser().empty() || isEmpty(rule)) {
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
        if (getParser().empty() || isEmpty(rule)) {
            return new AnalyzeCollection(getAnalyzer(), new ArrayList<>());
        }

        return new AnalyzeCollection(getAnalyzer(), getRawList(rule));
    }

    private List<T> getRawList(String rule) {
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim());
        final List<T> resultList = new ArrayList<>();
        final List<List<T>> resultsList = new ArrayList<>();
        for (RulePattern pattern : rulePatterns.patterns) {
            List<T> list = getSingleRawList(pattern);
            if (!list.isEmpty()) {
                resultsList.add(list);

                if (rulePatterns.resultType == RulePatterns.RULE_RESULT_OR) {
                    break;
                }
            }
        }

        if (!resultsList.isEmpty()) {
            if (rulePatterns.resultType == RulePatterns.RULE_RESULT_FILTER) {
                for (int i = 0, size = resultsList.get(0).size(); i < size; i++) {
                    for (List<T> list : resultsList) {
                        if (i < list.size()) {
                            resultList.add(list.get(i));
                        }
                    }
                }
            } else {
                for (List<T> list : resultsList) {
                    resultList.addAll(list);
                }
            }
        }
        return resultList;
    }

    private List<T> getSingleRawList(RulePattern rulePattern) {
        if (rulePattern.isRedirect) {
            String source = evalJS(getParser().getStringSource(), rulePattern);
            RulePattern pattern = RulePattern.fromRule(rulePattern.redirectRule);
            return getParser().parseList(source, pattern.elementsRule);
        } else {
            return getParser().getList(rulePattern.elementsRule);
        }
    }


    @Override
    public String parseResultContent(String string, String rule) {
        if (string == null || isEmpty(rule)) {
            return "";
        }
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim());
        final StringBuilder builder = new StringBuilder();
        for (RulePattern pattern : rulePatterns.patterns) {
            String result = processResultContent(getParser().parseString(string, pattern.elementsRule), pattern);
            if (!isEmpty(result)) {
                builder.append(result);
                if (rulePatterns.resultType == RulePatterns.RULE_RESULT_OR) {
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
        final RulePatterns rulePatterns = RulePatterns.fromRule(rule.trim(), getConfig().getVariableStore());
        if (getParser() instanceof JsoupParser) {
            for (RulePattern pattern : rulePatterns.patterns) {
                final List<String> result = getParser().parseStringList(string, pattern.elementsRule);
                if (!result.isEmpty()) {
                    return processResultUrl(result.get(0), pattern);
                }
            }
        } else {
            for (RulePattern pattern : rulePatterns.patterns) {
                String result = getParser().parseString(string, pattern.elementsRule);
                if (!isEmpty(result)) {
                    return processResultUrl(result, pattern);
                }
            }
        }
        return "";
    }

}
