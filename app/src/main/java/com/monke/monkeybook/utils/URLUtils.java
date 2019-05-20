package com.monke.monkeybook.utils;

import org.jsoup.helper.StringUtil;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

    private URLUtils() {
    }

    /**
     * 获取绝对地址
     */
    public static String getAbsoluteURL(String baseUrl, String relPath) {
        try {
            String header = null;
            int index = indexOfHeader(relPath);
            if (index >= 0) {
                header = relPath.substring(0, index + 1);
                relPath = relPath.substring(index + 1);
            }
            index = indexOfHeader(baseUrl);
            if (index >= 0) {
                baseUrl = baseUrl.substring(index + 1);
            }

            relPath = resolve(baseUrl, relPath);

            if (header != null) {
                relPath = header + relPath;
            }
        } catch (Exception ignore) {
        }
        return relPath;
    }

    private static int indexOfHeader(String url) {
        if (StringUtils.startWithIgnoreCase(url, "@header:")) {
            return url.indexOf("}");
        }
        return -1;
    }

    public static String resolve(String baseUrl, String relPath){
        try {
            URL absoluteUrl = new URL(baseUrl);
            URL parseUrl = new URL(absoluteUrl, relPath);
            return parseUrl.toString();
        }catch (MalformedURLException ignore) {
        }
        return relPath;
    }

    public static boolean isUrl(String urlStr) {
        String regex = "^(https?)://.+$";//设置正则表达式
        return urlStr.matches(regex);
    }

}
