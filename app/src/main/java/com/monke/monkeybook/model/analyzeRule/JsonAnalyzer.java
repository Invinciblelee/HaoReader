package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.monke.monkeybook.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.monke.monkeybook.model.analyzeRule.JsonParser.optList;
import static com.monke.monkeybook.model.analyzeRule.JsonParser.optString;

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
    public ReadContext parseSource(Object source){
        return JsonPath.parse(source);
    }

    @Override
    public String getResultContent(ReadContext source, String rule) {
        String result = "";
        if (source == null || isEmpty(rule)) {
            return result;
        }
        RulePattern rulePattern = splitSourceRule(rule.trim());
        if (isEmpty(rulePattern.elementsRule)) {
            return result;
        } else {
            result = optString(source, rulePattern.elementsRule);
        }

        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        if (!isEmpty(rulePattern.javaScript)) {
            result = JSParser.evalJS(rulePattern.javaScript, result, getConfig().getBaseURL());
        }
        return result;
    }

    @Override
    public String getResultUrl(ReadContext source, String rule) {
        String result = getResultContent(source, rule);
        if (!isEmpty(result)) {
            result = NetworkUtil.getAbsoluteURL(getConfig().getBaseURL(), result);
        }
        return result;
    }

    @Override
    public List<Object> getRawList(String source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        return optList(parseSource(source), rule);
    }

    @Override
    public List<Object> getRawList(ReadContext source, String rule) {
        if (source == null || isEmpty(rule)) {
            return new ArrayList<>();
        }
        return optList(source, rule);
    }

}
