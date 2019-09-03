package com.monke.monkeybook.model.analyzeRule;

import android.webkit.WebSettings;

import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/3/2.
 * 解析Headers
 */

public class AnalyzeHeaders {

    public static Map<String, String> getMap(BookSourceBean bookSource) {
        Map<String, String> headerMap = new HashMap<>();
        String userAgent = bookSource == null ? null : bookSource.getHttpUserAgent();
        if (StringUtils.isNotBlank(userAgent)) {
            headerMap.put("User-Agent", userAgent);
        } else {
            headerMap.put("User-Agent", getDefaultUserAgent());
        }

        String sourceUrl = bookSource == null ? null : bookSource.getBookSourceUrl();
        if (!isEmpty(sourceUrl)) {
            String cookie = CookieHelper.getInstance().getCookie(sourceUrl);
            if (!isEmpty(cookie)) {
                headerMap.put("Cookie", cookie);
            }
        }

        return headerMap;
    }

    public static String getUserAgent(String userAgent) {
        if (isEmpty(userAgent)) {
            return getDefaultUserAgent();
        } else {
            return userAgent;
        }
    }

    private static String getDefaultUserAgent() {
        return WebSettings.getDefaultUserAgent(ContextHolder.getContext());
    }
}
