package com.monke.basemvplib;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class OkHttpHelper {

    private OkHttpClient okHttpClient;

    private OkHttpHelper() {
    }

    private volatile static OkHttpHelper mInstance;

    public static OkHttpHelper getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpHelper.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpHelper();
                }
            }
        }
        return mInstance;
    }

    private Retrofit getRetrofitString(String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    private Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create(encode))
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    public <T> T createService(String url, Class<T> tClass) {
        return getRetrofitString(url).create(tClass);
    }

    public <T> T createService(String url, String encode, Class<T> tClass) {
        return getRetrofitString(url, encode).create(tClass);
    }



    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);

            SSLHelper.SSLParams sslParams = SSLHelper.getSslSocketFactory();
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    .hostnameVerifier(SSLHelper.UnSafeHostnameVerifier)
                    .connectionSpecs(specs)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(getHeaderInterceptor())
                    .addInterceptor(new RetryInterceptor(1))
                    .build();
        }
        return okHttpClient;
    }

    private Interceptor getHeaderInterceptor() {
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

    //请求头拦截器
    public class RequestHeadersInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            if (!NetworkUtil.isNetworkAvailable()) {
                // 无网络时，强制使用缓存
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }
            return chain.proceed(builder.build());
        }
    }

    //响应头拦截器
    public class CacheControlInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            String cacheControl = response.header("Cache-Control");
            if (TextUtils.isEmpty(cacheControl)) {
                return response.newBuilder().removeHeader("Pragma").header("Cache-Control", "public, max-age=60").build();
            }
            return response;
        }
    }
}
