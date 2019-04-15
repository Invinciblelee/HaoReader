package com.monke.monkeybook.model;


import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IHttpPostApi;

import io.reactivex.Observable;
import retrofit2.Response;

public class SimpleModel {


    public static Observable<Response<String>> getResponse(AnalyzeUrl analyzeUrl) {
        switch (analyzeUrl.getUrlMode()) {
            case POST:
                return OkHttpHelper.getInstance().createService(analyzeUrl.getHost(), IHttpPostApi.class)
                        .searchBook(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            case GET:
                return OkHttpHelper.getInstance().createService(analyzeUrl.getHost(), IHttpGetApi.class)
                        .searchBook(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            default:
                return OkHttpHelper.getInstance().createService(analyzeUrl.getHost(), IHttpGetApi.class)
                        .getWebContent(analyzeUrl.getPath(),
                                analyzeUrl.getHeaderMap());
        }
    }
}
