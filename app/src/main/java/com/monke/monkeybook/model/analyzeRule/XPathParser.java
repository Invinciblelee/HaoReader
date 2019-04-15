package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

final class XPathParser extends SourceParser<JXDocument, Element> {

    private static final String TAG = "XPATH";

    XPathParser() {

    }

    @Override
    String sourceToString(Element source) {
        if (source == null) {
            return "";
        }

        return source.toString();
    }

    @Override
    JXDocument fromSource(String source) {
        // 给表格标签添加完整的框架结构,否则会丢失表格标签;html标准不允许表格标签独立在table之外
        if (source.endsWith("</td>")) {
            source = "<tr>" + source + "</tr>";
        }
        if (source.endsWith("</tr>") || source.endsWith("</tbody>")) {
            source = "<table>" + source + "</table>";
        }
        return JXDocument.create(source);
    }

    @Override
    JXDocument fromSource(Element source) {
        String string = sourceToString(source);
        return fromSource(string);
    }

    @Override
    List<Element> getList(String rawRule) {
        return parseList(getSource(), rawRule);
    }

    @Override
    List<Element> parseList(String source, String rawRule) {
        return parseList(fromSource(source), rawRule);
    }

    private List<Element> parseList(JXDocument source, String rawRule) {
        if (TextUtils.isEmpty(rawRule)) {
            return new Elements();
        }
        final Elements elements = new Elements();
        try {
            List<Object> objects = source.sel(rawRule);
            for (Object object : objects) {
                if (object instanceof Element) {
                    elements.add((Element) object);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, rawRule, e);
        }
        return elements;
    }

    @Override
    List<String> getStringList(String rawRule) {
        if (isEmpty(rawRule)) {
            return Collections.emptyList();
        }

        if (rawRule.equals(OUTER_BODY)) {
            return ListUtils.mutableList(getStringSource());
        }

        return parseStringList(getSource(), rawRule);
    }

    @Override
    List<String> parseStringList(String source, String rawRule) {
        if (isEmpty(rawRule)) {
            return Collections.emptyList();
        }

        if (rawRule.equals(OUTER_BODY)) {
            return ListUtils.mutableList(source);
        }

        return parseStringList(fromSource(source), rawRule);
    }

    private List<String> parseStringList(JXDocument source, String xPath) {
        final List<String> resultList = new ArrayList<>();

        try {
            final List<Object> objects = source.sel(xPath);
            for (Object object : objects) {
                if (object instanceof String) {
                    String result = (String) object;
                    result = result.replaceAll("^,|,$", "");// 移除Xpath匹配结果首尾多余的逗号
                    resultList.add(result);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, xPath, e);
        }
        return resultList;
    }

    @Override
    String getString(String rawRule) {
        if (isEmpty(rawRule)) {
            return "";
        }

        if (rawRule.equals(OUTER_BODY)) {
            return getStringSource();
        }


        return parseString(getSource(), rawRule);
    }

    @Override
    String parseString(String source, String rawRule) {
        if (isEmpty(rawRule)) {
            return "";
        }

        if (rawRule.equals(OUTER_BODY)) {
            return source;
        }

        return parseString(fromSource(source), rawRule);
    }

    private String parseString(JXDocument document, String xPath) {
        try {
            Object object = document.selOne(xPath);
            if (object instanceof Element) {
                return formatHtmlString(((Element) object).html());
            } else {
                return formatHtmlString(StringUtils.valueOf(object));
            }

        } catch (Exception e) {
            Logger.e(TAG, xPath, e);
        }
        return "";
    }

    private String formatHtmlString(String html) {
        return html.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")  // 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")               // 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n　　");                   // 移除空行,并增加段前缩进2个汉字
    }
}
