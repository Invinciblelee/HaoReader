package com.monke.monkeybook.model.analyzeRule;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.basemvplib.RequestMethod;
import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;
import com.monke.monkeybook.utils.UrlEncoderUtils;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.script.SimpleBindings;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.MAP_TYPE;
import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_HEADER;
import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.PATTERN_PAGE;

/**
 * Created by GKF on 2018/1/24.
 * 搜索URL规则解析
 */

public class AnalyzeUrl {

    private String requestUrl;
    private String baseUrl;
    private String url;
    private String host;
    private String urlPath;
    private String queryStr;
    private byte[] postData;
    private String encoding;
    private Map<String, String> queryMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();
    private RequestMethod requestMethod = RequestMethod.DEFAULT;

    public AnalyzeUrl(String baseUrl, String urlRule) throws Exception {
        this(baseUrl, urlRule, null);
    }

    public AnalyzeUrl(String baseUrl, String ruleUrl, Map<String, String> headerMap) throws Exception {
        this(baseUrl, ruleUrl, null, headerMap);
    }

    public AnalyzeUrl(String baseUrl, String ruleUrl, Integer page, Map<String, String> headerMap) throws Exception {
        this(baseUrl, ruleUrl, null, page, headerMap);
    }

    public AnalyzeUrl(String baseUrl, String ruleUrl, String key, Integer page, Map<String, String> headerMap) throws Exception {
        if (!TextUtils.isEmpty(baseUrl)) {
            this.baseUrl = PATTERN_HEADER.matcher(baseUrl).replaceAll("");
        }

        //解析Header
        ruleUrl = analyzeHeader(ruleUrl, headerMap);
        //替换关键字
        if (!TextUtils.isEmpty(key)) {
            ruleUrl = ruleUrl.replace("searchKey", key);
        }
        //分离编码规则
        ruleUrl = splitCharCode(ruleUrl);

        //判断是否有下一页
        if (page != null && page > 1 && withoutPaging(ruleUrl)) {
            throw new Exception("no next page");
        }

        //替换js
        ruleUrl = analyzeJs(ruleUrl, baseUrl, key, page);

        //设置页数
        ruleUrl = analyzePage(ruleUrl, page);

        //分离post参数
        String[] ruleUrlS = ruleUrl.split("@");
        if (ruleUrlS.length > 1) {
            requestMethod = RequestMethod.POST;
        } else {
            //分离get参数
            ruleUrlS = ruleUrlS[0].split("\\?");
            if (ruleUrlS.length > 1) {
                requestMethod = RequestMethod.GET;
            }
        }

        generateUrlPath(ruleUrlS[0]);
        if (requestMethod != RequestMethod.DEFAULT) {
            analyzeQuery(queryStr = ruleUrlS[1]);
            postData = generatePostData();
        }
    }

    /**
     * 没有分页规则
     */
    private boolean withoutPaging(String ruleUrl) {
        return !ruleUrl.contains("searchPage")
                && !PATTERN_PAGE.matcher(ruleUrl).find();
    }

    /**
     * 解析Header
     */
    private String analyzeHeader(String ruleUrl, Map<String, String> headerMapF) {
        if (headerMapF != null) {
            headerMap.putAll(headerMapF);
        }
        Matcher matcher = PATTERN_HEADER.matcher(ruleUrl);
        if (matcher.find()) {
            String find = matcher.group(0);
            ruleUrl = ruleUrl.replace(find, "");
            find = find.substring(8);
            try {
                headerMap.putAll(Assistant.fromJson(find, MAP_TYPE));
            } catch (Exception ignore) {
            }
        }
        return ruleUrl;
    }

    /**
     * 解析页数
     */
    private String analyzePage(String ruleUrl, Integer searchPage) throws Exception {
        if (searchPage == null) return ruleUrl;
        Matcher matcher = PATTERN_PAGE.matcher(ruleUrl);
        if (matcher.find()) {
            String[] pages = matcher.group().substring(1, matcher.group().length() - 1).split(",");
            if (searchPage <= pages.length) {
                ruleUrl = ruleUrl.replace(matcher.group(), pages[searchPage - 1].trim());
            } else {
                final String page = pages[pages.length - 1].trim();
                if (withoutPaging(page)) {
                    throw new Exception("no next page");
                }
                ruleUrl = ruleUrl.replace(matcher.group(), page);
            }
        }
        return ruleUrl.replace("searchPage-1", String.valueOf(searchPage - 1))
                .replace("searchPage+1", String.valueOf(searchPage + 1))
                .replace("searchPage", String.valueOf(searchPage));
    }

    /**
     * 替换js
     */
    @SuppressLint("DefaultLocale")
    private String analyzeJs(String ruleUrl, String baseUrl, String searchKey, Integer searchPage) {
        if (ruleUrl.contains("{{") && ruleUrl.contains("}}")) {
            final StringBuffer buffer = new StringBuffer(ruleUrl.length());
            final SimpleBindings simpleBindings = new SimpleBindings() {{
                this.put("baseUrl", baseUrl);
                this.put("searchKey", searchKey);
                if (searchPage != null) {
                    this.put("searchPage", searchPage);
                }
            }};
            Matcher expMatcher = AnalyzeGlobal.PATTERN_EXP.matcher(ruleUrl);
            while (expMatcher.find()) {
                Object result = Assistant.evalObjectScript(expMatcher.group(1), simpleBindings);
                if (result instanceof Double && ((Double) result) % 1.0 == 0) {
                    expMatcher.appendReplacement(buffer, String.format("%.0f", (Double) result));
                } else {
                    expMatcher.appendReplacement(buffer, StringUtils.valueOf(result));
                }
            }
            expMatcher.appendTail(buffer);
            ruleUrl = buffer.toString();
        }
        return ruleUrl;
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
                        encoding = gz[1];
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
            if (TextUtils.isEmpty(encoding)) {
                if (UrlEncoderUtils.hasUrlEncoded(value)) {
                    queryMap.put(queryM[0], value);
                } else {
                    queryMap.put(queryM[0], URLEncoder.encode(value, "UTF-8"));
                }
            } else if (encoding.equals("escape")) {
                queryMap.put(queryM[0], StringUtils.escape(value));
            } else {
                queryMap.put(queryM[0], URLEncoder.encode(value, encoding));
            }
        }
    }

    /**
     * PostData
     */
    private byte[] generatePostData() {
        if (queryMap != null && !queryMap.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryMap.keySet();
            for (String key : keys) {
                builder.append(String.format("%s=%s&", key, queryMap.get(key)));
            }
            builder.deleteCharAt(builder.lastIndexOf("&"));
            return builder.toString().getBytes();
        }
        return null;
    }

    private void generateUrlPath(String ruleUrl) {
        url = URLUtils.getAbsUrl(baseUrl, ruleUrl);
        host = StringUtils.getBaseUrl(url);
        urlPath = url.substring(host.length());
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestUrl() {
        if (requestUrl == null) {
            return baseUrl;
        }
        return requestUrl;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return urlPath;
    }

    public String getUrl() {
        return url;
    }

    public String getQueryUrl() {
        if (StringUtils.isBlank(queryStr)) {
            return url;
        }
        return String.format("%s?%s", url, queryStr);
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public byte[] getPostData() {
        return postData;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod == null ? RequestMethod.DEFAULT : requestMethod;
    }

    @NonNull
    @Override
    public String toString() {
        return "AnalyzeUrl{" +
                "requestUrl='" + requestUrl + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", urlPath='" + urlPath + '\'' +
                ", queryStr='" + queryStr + '\'' +
                ", postData=" + Arrays.toString(postData) +
                ", encoding='" + encoding + '\'' +
                ", queryMap=" + queryMap +
                ", headerMap=" + headerMap +
                ", requestMethod=" + requestMethod +
                '}';
    }
}
