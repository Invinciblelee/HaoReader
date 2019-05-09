package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.help.MemoryCache;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

abstract class BaseAnalyzerPresenter<S> implements IAnalyzerPresenter, JavaExecutor {

    private final OutAnalyzer<S> mAnalyzer;

    BaseAnalyzerPresenter(OutAnalyzer<S> analyzer) {
        this.mAnalyzer = analyzer;
    }

    final OutAnalyzer<S> getAnalyzer() {
        return mAnalyzer;
    }

    final SourceParser<S> getParser() {
        return mAnalyzer.getParser();
    }

    final AnalyzeConfig getConfig() {
        return mAnalyzer.getConfig();
    }

    RulePatterns fromRule(String rawRule, boolean withVariableStore) {
        MemoryCache cache = MemoryCache.getInstance();
        RulePatterns patterns = cache.getCache(rawRule);
        if (patterns != null) {
            return patterns;
        }
        RuleMode mode = RuleMode.fromRuleType(mAnalyzer.getRuleType());
        if (withVariableStore) {
            patterns = RulePatterns.fromRule(rawRule, getConfig().getBaseURL(), getConfig().getVariableStore(), mode);
        } else {
            patterns = RulePatterns.fromRule(rawRule, getConfig().getBaseURL(), mode);
        }
        cache.putCache(rawRule, patterns);
        return patterns;
    }


    RulePattern fromSingleRule(String rawRule, boolean withVariableStore) {
        MemoryCache cache = MemoryCache.getInstance();
        RulePattern pattern = cache.getCache(rawRule);
        if (pattern != null) {
            return pattern;
        }
        RuleMode mode = RuleMode.fromRuleType(mAnalyzer.getRuleType());
        if (withVariableStore) {
            pattern = RulePattern.fromRule(rawRule, getConfig().getVariableStore(), mode);
        } else {
            pattern = RulePattern.fromRule(rawRule, mode);
        }
        cache.putCache(rawRule, pattern);
        return pattern;
    }

    String evalStringScript(@NonNull String string, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                string = Global.evalStringScript(javaScript, this, string, getConfig().getBaseURL());
            }
        }
        return string;
    }

    List<String> evalStringArrayScript(@NonNull String string, @NonNull RulePattern rulePattern) {
        final List<String> list = new ArrayList<>();
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                List<Object> result = Global.evalArrayScript(javaScript, this, string, getConfig().getBaseURL());
                list.addAll(ListUtils.toStringList(result));
            }
        }
        return list;
    }

    List<Object> evalObjectArrayScript(@NonNull String string, @NonNull RulePattern rulePattern) {
        final List<Object> list = new ArrayList<>();
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                list.addAll(Global.evalArrayScript(javaScript, this, string, getConfig().getBaseURL()));
            }
        }
        return list;
    }

    void evalReplace(@NonNull List<String> list, @NonNull RulePattern rulePattern) {
        if (!isEmpty(rulePattern.replaceRegex)) {
            ListIterator<String> iterator = list.listIterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterator.set(string.replaceAll(rulePattern.replaceRegex, rulePattern.replacement));
            }
        }
    }

    void processResultContents(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                iterator.set(evalStringScript(iterator.next(), rulePattern));
            }
        }

        evalReplace(result, rulePattern);
    }

    String processResultContent(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        result = evalReplace(result, rulePattern);

        return formatHtml(result);
    }


    String processResultUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        result = evalJoinUrl(result, rulePattern);
        return result;
    }

    String evalReplace(@NonNull String result, @NonNull RulePattern rulePattern) {
        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        return result;
    }

    String evalJoinUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalReplace(result, rulePattern);

        if (!isEmpty(result)) {
            result = URLUtils.getAbsoluteURL(getConfig().getBaseURL(), result);
        }
        return result;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), urlStr);
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
