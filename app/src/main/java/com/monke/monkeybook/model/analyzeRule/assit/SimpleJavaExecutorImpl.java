package com.monke.monkeybook.model.analyzeRule.assit;

import com.monke.monkeybook.help.TextProcessor;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.utils.StringUtils;

import retrofit2.Response;

public class SimpleJavaExecutorImpl implements SimpleJavaExecutor {

    public SimpleJavaExecutorImpl() {
    }

    @Override
    public final String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(StringUtils.getBaseUrl(urlStr), urlStr);
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
