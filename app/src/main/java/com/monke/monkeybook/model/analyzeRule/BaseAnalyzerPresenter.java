package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;

import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

abstract class BaseAnalyzerPresenter<S, T> implements IAnalyzerPresenter {

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


    String evalJS(@NonNull String result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            for (String javaScript : rulePattern.javaScripts) {
                result = getJSParser().evalJS(javaScript, this, result, getConfig().getBaseURL());
            }
        }
        return result;
    }

    void processResultContents(@NonNull List<String> result, @NonNull RulePattern rulePattern) {
        if (!rulePattern.javaScripts.isEmpty()) {
            ListIterator<String> iterator = result.listIterator();
            while (iterator.hasNext()) {
                iterator.set(evalJS(iterator.next(), rulePattern));
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
        result = evalJS(result, rulePattern);

        if (!isEmpty(rulePattern.replaceRegex)) {
            result = result.replaceAll(rulePattern.replaceRegex, rulePattern.replacement);
        }
        return result;
    }


    String processResultUrl(@NonNull String result, @NonNull RulePattern rulePattern) {
        result = evalJS(result, rulePattern);

        if (!isEmpty(result)) {
            result = NetworkUtil.getAbsoluteURL(getConfig().getBaseURL(), result);
        }
        return result;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * js实现跨域访问,不能删
     */
    @SuppressWarnings("unused")
    public final String ajax(String urlStr) {
        try {
<<<<<<< HEAD
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr);
=======
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr, getConfig().getBaseURL());
>>>>>>> 3c2c4a986d97feb3f36c665e7701c2c1d0411ad8
            Response<String> response = SimpleModel.getResponse(analyzeUrl)
                    .blockingFirst();
            return response.body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * js实现解码,不能删
     */
    @SuppressWarnings("unused")
    public final String base64Decode(String base64) {
        return StringUtils.base64Decode(base64);
    }

    /**
     * js调用,不能删
     */
    @SuppressWarnings("unused")
    public final String formatResultContent(String string) {
        if (isEmpty(string)) {
            return "";
        }
        return string.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")  // 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")               // 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n　　");                   // 移除空行,并增加段前缩进2个汉字
    }
}
