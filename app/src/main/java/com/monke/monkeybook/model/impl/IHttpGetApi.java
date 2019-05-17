package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/21.
 * get web content
 */

public interface IHttpGetApi {
    @GET
    Observable<Response<String>> getWebContent(@Url String url,
                                               @HeaderMap Map<String, String> headers);

    @GET
    Observable<Response<String>> searchBook(@Url String url,
                                            @QueryMap(encoded = true) Map<String, String> queryMap,
                                            @HeaderMap Map<String, String> headers);

    @GET
    Call<String> getWebContentCall(@Url String url,
                                   @HeaderMap Map<String, String> headers);

    @GET("/webapi/book/info.php")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<String>> getbookdetail(@QueryMap Map<String, String> fieldMap);

    @GET("/webapi/book/chapterlist.php")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<String>> getchapterlist(@QueryMap Map<String, String> fieldMap);

    @GET("/novel/i.php")
    Observable<Response<String>> getsearchBook(@Query("do") String doo, @Query("p") String pp, @Query("q") String content, @Query("page") int page, @Query("size") String size);
}
