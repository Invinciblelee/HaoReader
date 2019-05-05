package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.List;

import static android.text.TextUtils.isEmpty;

final class XPathParser extends SourceParser<JXDocument> {

    private static final String TAG = "XPATH";

    XPathParser() {

    }

    @Override
    String parseObject(Object source) {
        if (source instanceof String) {
            return (String) source;
        } else if (source instanceof Element) {
            return source.toString();
        } else if (source instanceof JXDocument) {
            Object object = ((JXDocument) source).selOne("//*");
            return StringUtils.valueOf(object);
        }
        return "";
    }

    @Override
    JXDocument fromObject(Object source) {
        if (source instanceof String) {
            return JXDocument.create(ensureTableNode((String) source));
        } else if (source instanceof Element) {
            return JXDocument.create(ensureTableNode(source.toString()));
        } else if (source instanceof JXDocument) {
            return (JXDocument) source;
        }
        return null;
    }

    @Override
    List<Object> getList(Rule rule) {
        String ruleStr = rule.getRule();
        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(getSource());
        }
        return ListUtils.toObjectList(parseList(getSource(), ruleStr));
    }

    @Override
    List<Object> parseList(String source, Rule rule) {
        String ruleStr = rule.getRule();
        return ListUtils.toObjectList(parseList(fromObject(source), ruleStr));
    }

    private List<Element> parseList(JXDocument source, String rule) {
        if (TextUtils.isEmpty(rule)) {
            return ListUtils.mutableList();
        }
        final Elements elements = new Elements();
        try {
            List<Object> objects = source.sel(rule);
            for (Object object : objects) {
                if (object instanceof Element) {
                    elements.add((Element) object);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, rule, e);
        }
        return elements;
    }

    @Override
    List<String> getStringList(Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }

        if (isOuterBody(ruleStr)) {
            return ListUtils.mutableList(getStringSource());
        }

        return parseStringList(getSource(), ruleStr);
    }

    @Override
    List<String> parseStringList(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }
        return parseStringList(fromObject(source), ruleStr);
    }

    private List<String> parseStringList(JXDocument source, String xPath) {
        final List<String> resultList = ListUtils.mutableList();

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
    String getString(Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return "";
        }

        if (isOuterBody(ruleStr)) {
            return getStringSource();
        }

        return parseString(getSource(), ruleStr);
    }

    @Override
    String parseString(String source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return "";
        }
        return parseString(fromObject(source), ruleStr);
    }

    @Override
    String getStringFirst(Rule rule) {
        return getString(rule);
    }

    @Override
    String parseStringFirst(String source, Rule rule) {
        return parseString(source, rule);
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

    private String ensureTableNode(String source) {
        // 给表格标签添加完整的框架结构,否则会丢失表格标签;html标准不允许表格标签独立在table之外
        if (source.endsWith("</td>")) {
            source = "<tr>" + source + "</tr>";
        }
        if (source.endsWith("</tr>") || source.endsWith("</tbody>")) {
            source = "<table>" + source + "</table>";
        }
        return source;
    }
}
