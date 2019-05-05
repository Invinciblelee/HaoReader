package com.monke.monkeybook.utils;

import java.net.URL;

public class URLUtils {

    private URLUtils() {
    }

    /**
     * 获取绝对地址
     */
    public static String getAbsoluteURL(String baseURL, String relativePath) {
        try {
            String header = null;
            int index = indexOfHeader(relativePath);
            if (index >= 0) {
                header = relativePath.substring(0, index + 1);
                relativePath = relativePath.substring(index + 1);
            }
            index = indexOfHeader(baseURL);
            if (index >= 0) {
                baseURL = baseURL.substring(index + 1);
            }
            URL absoluteUrl = new URL(baseURL);
            URL parseUrl = new URL(absoluteUrl, relativePath);
            relativePath = parseUrl.toString();
            if (header != null) {
                relativePath = header + relativePath;
            }
            return relativePath;
        } catch (Exception ignore) {
        }
        return relativePath;
    }

    private static int indexOfHeader(String url) {
        if (StringUtils.startWithIgnoreCase(url, "@header:")) {
            return url.indexOf("}");
        }
        return -1;
    }

    public static boolean isUrl(String urlStr) {
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }

}
