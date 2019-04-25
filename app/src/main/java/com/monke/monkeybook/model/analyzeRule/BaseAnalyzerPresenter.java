package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

abstract class BaseAnalyzerPresenter<S, T> implements IAnalyzerPresenter, JavaExecutor {

    private final OutAnalyzer<S, T> mAnalyzer;

    BaseAnalyzerPresenter(OutAnalyzer<S, T> analyzer) {
        this.mAnalyzer = analyzer;
    }

    final OutAnalyzer<S, T> getAnalyzer() {
        return mAnalyzer;
    }

    final JSParser getJSParser() {
        return mAnalyzer.getJSParser();
    }

    final SourceParser<S, T> getParser() {
        return mAnalyzer.getParser();
    }

    final AnalyzeConfig getConfig() {
        return mAnalyzer.getConfig();
    }


    String evalStringScript(@NonNull String string, @NonNull RulePattern rulePattern) {
        String result = string;
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                final Object value = getJSParser().evalStringScript(javaScript, this, string, getConfig().getBaseURL());
                result = StringUtils.valueOf(value);
            }
        }
        return result;
    }

    List<Object> evalArrayScript(@NonNull String string, @NonNull RulePattern rulePattern) {
        List<Object> result = new ArrayList<>();
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                result = getJSParser().evalArrayScript(javaScript, this, string, getConfig().getBaseURL());
            }
        }
        return result;
    }

    void processResultContents(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                iterator.set(evalStringScript(iterator.next(), rulePattern));
            }
        }

        if (!isEmpty(rulePattern.replaceRegex)) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterator.set(string.replaceAll(rulePattern.replaceRegex, rulePattern.replacement));
            }
        }
    }

    String processResultContent(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        return result;
    }


    String processResultUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalStringScript(result, rulePattern);

        if (!isEmpty(result)) {
            result = NetworkUtil.getAbsoluteURL(getConfig().getBaseURL(), result);
        }
        return result;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr, getConfig().getBaseURL());
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
    public final String formatResultContent(String string) {
        if (isEmpty(string)) {
            return "";
        }
        return string.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")  // 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")               // 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n　　");                   // 移除空行,并增加段前缩进2个汉字
    }

    @Override
    public String formatResultUrl(String string) {
        return NetworkUtil.getAbsoluteURL(getConfig().getBaseURL(), string);
    }
}
