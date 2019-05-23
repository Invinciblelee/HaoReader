package com.monke.monkeybook.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

    private URLUtils() {
    }

    /**
     * 获取绝对地址
     */
    public static String getAbsUrl(String baseUrl, String relPath) {
        try {
            final String header;
            int index = indexOfHeader(relPath);
            header = index == -1 ? "" : relPath.substring(0, index + 1);
            relPath = relPath.substring(index + 1);

            index = indexOfHeader(baseUrl);
            baseUrl = index == -1 ? baseUrl : baseUrl.substring(index + 1);

            relPath = header + resolve(baseUrl, relPath);

        } catch (Exception ignore) {
        }
        return relPath;
    }

    private static int indexOfHeader(String url) {
        return url.indexOf("}");
    }

    public static String resolve(String baseUrl, String relPath) {
        try {
            URL parseUrl = new URL(new URL(baseUrl), relPath);
            return parseUrl.toString();
        } catch (MalformedURLException ignore) {
        }
        return relPath;
    }

    public static boolean isUrl(String urlStr) {
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }

}
