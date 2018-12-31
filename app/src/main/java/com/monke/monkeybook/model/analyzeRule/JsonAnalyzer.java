package com.monke.monkeybook.model.analyzeRule;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.monke.monkeybook.model.analyzeRule.JsonParser.getList;
import static com.monke.monkeybook.model.analyzeRule.JsonParser.getString;

public class JsonAnalyzer extends OutAnalyzer<ReadContext, Object> {

    private JsonContentDelegate mDelegate;

    @Override
    public ContentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = new JsonContentDelegate(this);
        }
        return mDelegate;
    }

    @Override
    public ReadContext parseSource(String source) {
        return JsonPath.parse(source);
    }

    @Override
    public ReadContext parseSource(Object source) {
        return JsonPath.parse(source);
    }

    @Override
    public String getResultContent(ReadContext source, String rule) {
        String result = "";
        if (source == null || isEmpty(rule)) {
            return result;
        }
        RulePattern rulePattern = RulePattern.from(rule.trim());
        if (isEmpty(rulePattern.elementsRule)) {
            return result;
        } else {
            result = getString(source, rulePattern.elementsRule);
        }

        return processingResultContent(result, rulePattern);
    }

    @Override
    public String getResultUrl(ReadContext source, String rule) {
        String result = getResultContent(source, rule);
        return processingResultUrl(result);
    }

    @Override
    public List<Object> getRawList(String source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        return getList(parseSource(source), rule);
    }

    @Override
    public List<Object> getRawList(ReadContext source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        return getList(source, rule);
    }

}
