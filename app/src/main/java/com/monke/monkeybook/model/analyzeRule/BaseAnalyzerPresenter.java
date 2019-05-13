package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.VariableStore;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.NativeObject;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

abstract class BaseAnalyzerPresenter<S> implements IAnalyzerPresenter, JavaExecutor {

    private final OutAnalyzer<S> mAnalyzer;

    private final Map<String, Object> mCache = new HashMap<>();

    BaseAnalyzerPresenter(OutAnalyzer<S> analyzer) {
        this.mAnalyzer = analyzer;
    }

    final OutAnalyzer<S> getAnalyzer() {
        return mAnalyzer;
    }

    final SourceParser<S> getParser() {
        return mAnalyzer.getParser();
    }

    final String getBaseURL() {
        return mAnalyzer.getConfig().getBaseURL();
    }

    final VariableStore getVariableStore() {
        return mAnalyzer.getConfig().getVariableStore();
    }

    final BookSourceBean getBookSource() {
        return mAnalyzer.getConfig().getBookSource();
    }

    final Object getCache(String key) {
        if (getBookSource().getBookSourceCacheEnabled()) {
            return mCache.get(key);
        }
        return null;
    }

    final void putCache(String key, Object value) {
        if (getBookSource().getBookSourceCacheEnabled()) {
            mCache.put(key, value);
        }
    }

    final String evalStringScript(@NonNull Object result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                result = Global.evalObjectScript(javaScript, this, result, getBaseURL());
            }
        }
        return StringUtils.valueOf(result);
    }

    final List<String> evalStringArrayScript(@NonNull Object result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                result = Global.evalArrayScript(javaScript, this, result, getBaseURL());
            }
        }
        final List<String> list = new ArrayList<>();
        if (result instanceof List) {
            for (Object object : (List) result) {
                list.add(StringUtils.valueOf(object));
            }
        }
        return list;
    }

    final List<Object> evalObjectArrayScript(@NonNull Object result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                result = Global.evalArrayScript(javaScript, this, result, getBaseURL());
            }
        }
        final List<Object> list = new ArrayList<>();
        if (result instanceof List) {
            list.addAll((List) result);
        }
        return list;
    }


    final void processResultContents(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                iterator.set(evalStringScript(iterator.next(), rulePattern));
            }
        }

        matchRegexes(result, rulePattern);
    }


    final void processResultUrls(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                iterator.set(evalStringScript(iterator.next(), rulePattern));
            }
        }

        processRawUrls(result, rulePattern);
    }

    final void processRawUrls(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        matchRegexes(result, rulePattern);

        ListIterator<String> iterator = result.listIterator();
        while (iterator.hasNext()) {
            iterator.set(URLUtils.getAbsoluteURL(getBaseURL(), iterator.next()));
        }
    }

    final void matchRegexes(@NonNull List<String> list, @NonNull RulePattern rulePattern) {
        if (!isEmpty(rulePattern.replaceRegex)) {
            ListIterator<String> iterator = list.listIterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterator.set(string.replaceAll(rulePattern.replaceRegex, rulePattern.replacement));
            }
        }
    }

    final String processResultContent(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        result = matchRegex(result, rulePattern);

        return formatHtml(result);
    }


    final String processResultUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        result = processRawUrl(result, rulePattern);

        return result;
    }

    final String processRawUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = matchRegex(result, rulePattern);

        result = URLUtils.getAbsoluteURL(getBaseURL(), result);

        return result;
    }

    final String matchRegex(@NonNull String result, @NonNull RulePattern rulePattern) {
        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        return result;
    }

    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        RulePatterns patterns = (RulePatterns) getCache(rawRule);
        if (patterns != null) {
            return patterns;
        }
        RuleMode mode = RuleMode.fromRuleType(mAnalyzer.getRuleType());
        if (withVariableStore) {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), getVariableStore(), mode);
        } else {
            patterns = RulePatterns.fromRule(rawRule, getBaseURL(), mode);
        }
        putCache(rawRule, patterns);
        return patterns;
    }


    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        RulePattern pattern = (RulePattern) getCache(rawRule);
        if (pattern != null) {
            return pattern;
        }
        RuleMode mode = RuleMode.fromRuleType(mAnalyzer.getRuleType());
        if (withVariableStore) {
            pattern = RulePattern.fromRule(rawRule, getVariableStore(), mode);
        } else {
            pattern = RulePattern.fromRule(rawRule, mode);
        }
        putCache(rawRule, pattern);
        return pattern;
    }

    @Override
    public String getResultContentInternal(String rule) {
        final Object object = getParser().getPrimitive();
        if (object instanceof NativeObject) {
            return StringUtils.valueOf(((NativeObject) object).get(rule));
        } else if (object instanceof Element) {
            Element element = (Element) object;
            Element find = element.selectFirst(rule);
            return StringUtils.checkNull(find == null ? null : find.text(), element.text());
        } else if (object instanceof JXNode) {
            return StringUtils.valueOf(((JXNode) object).selOne(rule));
        }
        return getResultContent(rule);
    }

    @Override
    public String getResultUrlInternal(String rule) {
        final Object object = getParser().getPrimitive();
        if (object instanceof NativeObject) {
            return StringUtils.valueOf(((NativeObject) object).get(rule));
        } else if (object instanceof Element) {
            Element element = (Element) object;
            Element find = element.selectFirst(rule);
            return StringUtils.checkNull(find == null ? null : find.text(), element.attr(rule));
        } else if (object instanceof JXNode) {
            return StringUtils.valueOf(((JXNode) object).selOne(rule));
        }
        return getResultUrl(rule);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(getBaseURL(), urlStr);
            Response<String> response = SimpleModel.getResponse(analyzeUrl)
                    .blockingFirst();
            return response.body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    public final String base64Decode(String base64) {
        return StringUtils.base64Decode(base64);
    }


    @Override
    public String base64Encode(String string) {
        return StringUtils.base64Encode(string);
    }

    @Override
    public final String formatHtml(String string) {
        return StringUtils.formatHtml(string);
    }
}
