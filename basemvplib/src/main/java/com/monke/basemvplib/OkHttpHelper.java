package com.monke.basemvplib;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class OkHttpHelper {

    private OkHttpClient okHttpClient;


    private OkHttpHelper() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.createTrustAllManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .addInterceptor(getHeaderInterceptor())
                .addInterceptor(new RetryInterceptor(1)).build();
    }

    private static class InstanceHolder {
        private static final OkHttpHelper SINGLETON = new OkHttpHelper();
    }

    public static OkHttpHelper getInstance() {
        return InstanceHolder.SINGLETON;
    }

    private Retrofit getRetrofitString(String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create(encode))
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public <T> T createService(String url, Class<T> tClass) {
        return getRetrofitString(url).create(tClass);
    }

    public <T> T createService(String url, String encode, Class<T> tClass){
        return getRetrofitString(url, encode).create(tClass);
    }

    private static Interceptor getHeaderInterceptor() {
        return chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Keep-Alive", "300")
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Cache-Control", "no-cache")
                    .build();
            return chain.proceed(request);
        };
    }
}
