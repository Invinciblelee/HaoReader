package com.monke.basemvplib;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggerInterceptor implements Interceptor {

    public static final String TAG = "NetWorkLogger";

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        String body = null;
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF_8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF_8);
            }
            body = buffer.readString(charset);
        }

        Log.d(TAG, "发送请求: method：" + request.method()
                + "\nurl：" + request.url()
                + "\n请求头：" + request.headers()
                + "\n请求参数: " + body);
        Response response = chain.proceed(request);

        ResponseBody responseBody = response.body();
        String rBody;

        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();

        Charset charset = UTF_8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF_8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        rBody = buffer.clone().readString(charset);

        Log.d(TAG, "收到响应: code:" + response.code()
                + "\n请求url：" + response.request().url()
                + "\n请求body：" + body
                + "\nResponse: " + rBody);
        return response;
    }
}