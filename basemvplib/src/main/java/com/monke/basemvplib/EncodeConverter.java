package com.monke.basemvplib;

import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class EncodeConverter extends Converter.Factory {
    private String encode;

    private EncodeConverter() {

    }

    private EncodeConverter(String encode) {
        this.encode = encode;
    }

    public static EncodeConverter create() {
        return new EncodeConverter();
    }

    public static EncodeConverter create(String en) {
        return new EncodeConverter(en);
    }

    @Override
    public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return value -> {
            final byte[] responseBytes = value.bytes();
            if (!TextUtils.isEmpty(encode)) {
                return new String(responseBytes, encode);
            }

            String charsetStr = null;
            MediaType mediaType = value.contentType();
            //根据http头判断
            if (mediaType != null) {
                Charset charset = mediaType.charset();
                if (charset != null) {
                    charsetStr = charset.displayName();
                }
            }

            if (charsetStr == null) {
                charsetStr = EncodingDetect.getHtmlEncode(responseBytes);
            }

            return new String(responseBytes, Charset.forName(charsetStr));
        };
    }
}
