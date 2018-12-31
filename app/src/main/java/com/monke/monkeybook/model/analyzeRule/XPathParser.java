package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import java.util.List;

class XPathParser {
    private XPathParser(){

    }

    static Elements getAsElements(@NonNull JXDocument document, String xPath) {
        Elements elements = new Elements();
        List<Object> objects = document.sel(xPath);
        for (Object object : objects) {
            if (object instanceof Element) {
                elements.add((Element) object);
            }
        }
        return elements;
    }

    static String getAsString(@NonNull JXDocument document, String rule) {
        String result;
        Object object = document.selOne(rule);
        if (object instanceof Element) {
            result = ((Element) object).html()
                    .replaceAll("(?i)<(br[\\s/]*|p.*?|div.*?|/p|/div)>", "\n")
                    .replaceAll("<.*?>", "");
        } else {
            result = (String) object;
        }
        return result;
    }
}
