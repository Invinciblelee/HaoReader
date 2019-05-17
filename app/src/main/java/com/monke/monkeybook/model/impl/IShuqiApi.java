package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface IShuqiApi {


    @GET("/webapi/book/info.php")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<String>> getBookDetail(@QueryMap Map<String, String> fieldMap);

    @GET("/webapi/book/chapterlist.php")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<String>> getChapterList(@QueryMap Map<String, String> fieldMap);

    @GET("/novel/i.php")
    Observable<Response<String>> getSearchBook(@Query("do") String doo, @Query("p") String pp, @Query("q") String content, @Query("page") int page, @Query("size") String size);

}
