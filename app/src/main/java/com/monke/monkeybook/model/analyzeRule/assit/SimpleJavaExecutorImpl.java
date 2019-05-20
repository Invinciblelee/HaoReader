package com.monke.monkeybook.model.analyzeRule.assit;

import com.monke.monkeybook.help.TextProcessor;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.utils.StringUtils;

import retrofit2.Response;

public class SimpleJavaExecutorImpl implements SimpleJavaExecutor {

    private final String mBaseUrl;

    public SimpleJavaExecutorImpl(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    public SimpleJavaExecutorImpl() {
        this(null);
    }

    @Override
    public final String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(mBaseUrl, urlStr);
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
        return TextProcessor.formatHtml(string);
    }
}
