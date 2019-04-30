package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.UrlEncoderUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_HEADER;
import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.PATTERN_PAGE;
import static com.monke.monkeybook.model.analyzeRule.pattern.Patterns.STRING_MAP;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */

public class AnalyzeUrl {

    private static final String TAG = AnalyzeUrl.class.getSimpleName();

    private String url;
    private String hostUrl;
    private String urlPath;
    private String queryStr;
    private String userAgent;
    private Map<String, String> queryMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();
    private String charCode;
    private UrlMode urlMode = UrlMode.DEFAULT;

    public AnalyzeUrl(String urlRule, String baseUrl) throws Exception {
        this(urlRule, null, null, null, baseUrl);
    }

    public AnalyzeUrl(String ruleUrl, Map<String, String> headerMap, String baseUrl) throws Exception {
        this(ruleUrl, null, null, headerMap, baseUrl);
    }

    public AnalyzeUrl(String ruleUrl, Integer page, Map<String, String> headerMap, String baseUrl) throws Exception {
        this(ruleUrl, "", page, headerMap, baseUrl);
    }

    public AnalyzeUrl(String ruleUrl, String key, Integer page, Map<String, String> headerMap, String baseUrl) throws Exception {
        this.hostUrl = baseUrl;
        //解析Header
        ruleUrl = analyzeHeader(ruleUrl, headerMap);
        //替换关键字
        if (!TextUtils.isEmpty(key)) {
            ruleUrl = ruleUrl.replace("searchKey", key);
        }
        //分离编码规则
        ruleUrl = splitCharCode(ruleUrl);

        //设置页数
        //设置页数
        if (page != null) {
            ruleUrl = analyzePage(ruleUrl, page);
        }

        //分离post参数
        String[] ruleUrlS = ruleUrl.split("@");
        if (ruleUrlS.length > 1) {
            urlMode = UrlMode.POST;
        } else {
            //分离get参数
            ruleUrlS = ruleUrlS[0].split("\\?");
            if (ruleUrlS.length > 1) {
                urlMode = UrlMode.GET;
            }
        }
        generateUrlPath(ruleUrlS[0]);
        if (urlMode != UrlMode.DEFAULT) {
            analyzeQuery(queryStr = ruleUrlS[1]);
        }
    }

    /**
     * 解析Header
     */
    private String analyzeHeader(String ruleUrl, Map<String, String> headerMapF) {
        if (headerMapF != null) {
            headerMap.putAll(headerMapF);
            userAgent = headerMapF.get("User-Agent");
        }
        Matcher matcher = PATTERN_HEADER.matcher(ruleUrl);
        Gson gson = new Gson();
        if (matcher.find()) {
            String find = matcher.group(0);
            ruleUrl = ruleUrl.replace(find, "");
            find = find.substring(8);
            try {
                Map<String, String> map = gson.fromJson(find, STRING_MAP);
                headerMap.putAll(map);
            } catch (Exception ignore) {
            }
        }
        return ruleUrl;
    }

    /**
     * 解析页数
     */
    private String analyzePage(String ruleUrl, final int searchPage) {
        Matcher matcher = PATTERN_PAGE.matcher(ruleUrl);
        if (matcher.find()) {
            String[] pages = matcher.group(0).split(",");
            if (searchPage <= pages.length) {
                ruleUrl = ruleUrl.replaceAll("\\{.*?\\}", pages[searchPage - 1].trim());
            } else {
                ruleUrl = ruleUrl.replaceAll("\\{.*?\\}", pages[pages.length - 1].trim());
            }
        }
        return ruleUrl.replace("searchPage-1", String.valueOf(searchPage - 1))
                .replace("searchPage+1", String.valueOf(searchPage + 1))
                .replace("searchPage", String.valueOf(searchPage));
    }


    /**
     * 解析编码规则
     */
    private String splitCharCode(String rule) {
        String[] ruleUrlS = rule.split("\\|");
        if (ruleUrlS.length > 1) {
            if (!TextUtils.isEmpty(ruleUrlS[1])) {
                String[] qtS = ruleUrlS[1].split("&");
                for (String qt : qtS) {
                    String[] gz = qt.split("=");
                    if (gz[0].equals("char")) {
                        charCode = gz[1];
                    }
                }
            }
        }
        return ruleUrlS[0];
    }

    /**
     * QueryMap
     */
    private void analyzeQuery(String allQuery) throws Exception {
        String[] queryS = allQuery.split("&");
        for (String query : queryS) {
            String[] queryM = query.split("=");
            String value = queryM.length > 1 ? queryM[1] : "";
            if (TextUtils.isEmpty(charCode)) {
                if (UrlEncoderUtils.hasUrlEncoded(value)) {
                    queryMap.put(queryM[0], value);
                } else {
                    queryMap.put(queryM[0], URLEncoder.encode(value, "UTF-8"));
                }
            } else if (charCode.equals("escape")) {
                queryMap.put(queryM[0], StringUtils.escape(value));
            } else {
                queryMap.put(queryM[0], URLEncoder.encode(value, charCode));
            }
        }
    }

    private void generateUrlPath(String ruleUrl) {
        String baseUrl = StringUtils.getBaseUrl(ruleUrl);
        if (StringUtils.isBlank(baseUrl) && hostUrl != null) {
            url = hostUrl + ruleUrl;
            urlPath = ruleUrl;
        } else {
            url = ruleUrl;
            hostUrl = baseUrl;
            if (hostUrl != null) {
                urlPath = ruleUrl.substring(hostUrl.length());
            }
        }
        Logger.d(TAG, toString());
    }

    public String getHost() {
        return hostUrl;
    }

    public String getPath() {
        return urlPath;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public byte[] getPostData() {
        StringBuilder builder = new StringBuilder();
        Set<String> keys = queryMap.keySet();
        for (String key : keys) {
            builder.append(String.format("%s=%s&", key, queryMap.get(key)));
        }
        builder.deleteCharAt(builder.lastIndexOf("&"));
        return builder.toString().getBytes();
    }

    public UrlMode getUrlMode() {
        return urlMode;
    }

    public enum UrlMode {
        GET, POST, DEFAULT
    }

    @NonNull
    @Override
    public String toString() {
        return "AnalyzeUrl{" +
                "url='" + url + '\'' +
                ", hostUrl='" + hostUrl + '\'' +
                ", urlPath='" + urlPath + '\'' +
                ", queryStr='" + queryStr + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", queryMap=" + queryMap +
                ", headerMap=" + headerMap +
                ", charCode='" + charCode + '\'' +
                ", urlMode=" + urlMode +
                '}';
    }
}
