package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.List;

import static android.text.TextUtils.isEmpty;

final class XPathParser extends SourceParser<JXDocument> {

    private static final String TAG = "XPATH";

    XPathParser() {
    }

    @Override
    String parseObject(Object source) {
        if (source instanceof JXDocument) {
            Object object = ((JXDocument) source).selOne("//*");
            return StringUtils.valueOf(object);
        }
        return StringUtils.valueOf(source);
    }

    @Override
    JXDocument fromObject(Object source) {
        if (source instanceof String) {
            return JXDocument.create(ensureTableNode((String) source));
        } else if (source instanceof Element) {
            return JXDocument.create(new Elements((Element) source));
        } else if (source instanceof JXNode) {
            JXNode jxNode = (JXNode) source;
            if (jxNode.isElement()) {
                return JXDocument.create(new Elements(jxNode.asElement()));
            }
        } else if (source instanceof JXDocument) {
            return (JXDocument) source;
        }
        return JXDocument.create(ensureTableNode(StringUtils.valueOf(source)));
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
    List<Object> parseList(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        return ListUtils.toObjectList(parseList(fromObject(source), ruleStr));
    }

    private List<JXNode> parseList(JXDocument source, String rule) {
        if (TextUtils.isEmpty(rule)) {
            return ListUtils.mutableList();
        }
        try {
            return source.selN(rule);
        } catch (Exception e) {
            Logger.e(TAG, rule, e);
        }
        return ListUtils.mutableList();
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
    List<String> parseStringList(Object source, Rule rule) {
        String ruleStr = rule.getRule();
        if (isEmpty(ruleStr)) {
            return ListUtils.mutableList();
        }
        return parseStringList(fromObject(source), ruleStr);
    }

    private List<String> parseStringList(JXDocument source, String xPath) {
        final List<String> resultList = ListUtils.mutableList();

        try {
            for (JXNode jxNode : source.selN(xPath)) {
                resultList.add(StringUtils.valueOf(jxNode));
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
    String parseString(Object source, Rule rule) {
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
    String parseStringFirst(Object source, Rule rule) {
        return parseString(source, rule);
    }

    private String parseString(JXDocument document, String xPath) {
        try {
            Object object = document.selOne(xPath);
            return StringUtils.valueOf(object);
        } catch (Exception e) {
            Logger.e(TAG, xPath, e);
        }
        return "";
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
