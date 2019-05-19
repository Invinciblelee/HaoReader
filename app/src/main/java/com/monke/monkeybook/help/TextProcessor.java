package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.monke.monkeybook.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class TextProcessor {

    private static final String[] CHAPTER_PATTERNS = new String[]{
            "^(.*?([\\d零〇一二两三四五六七八九十百千万0-9\\s]+)[章节回])[、，。　：:.\\s]*",
            "^([\\(（\\[【]*([\\d零〇一二两三四五六七八九十百千万0-9\\s]+)[】\\]）\\)]*)[、，。　：:.\\s]+"
    };

    private static final String SPECIAL_PATTERN = "第.*?[卷篇集].*?第.*[章节回].*?";

    private TextProcessor() {
    }

    public static int guessChapterNum(String name) {
        if (TextUtils.isEmpty(name) || name.matches(SPECIAL_PATTERN)) {
            return -1;
        }
        for (String str : CHAPTER_PATTERNS) {
            Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                return StringUtils.stringToInt(matcher.group(2));
            }
        }
        return -1;
    }

    public static String formatChapterName(String chapterName) {
        if (StringUtils.isBlank(chapterName)) {
            return "";
        }

        String halfString = StringUtils.fullToHalf(chapterName);
        return StringUtils.trim(halfString.replaceAll("\\s+", " "));
    }

    public static String formatBookName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return StringUtils.trim(str.replace("&nbsp;", "")
                .replace(":", "：")
                .replace(",", "，")
                .replaceAll("\\s+", " ")
                .replaceAll("[?？!！。~]+", ""))
                .replaceAll("([\\[［【（(].*[)）】］\\]])", "");
    }

    public static String formatAuthorName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return StringUtils.trim(str.replace("&nbsp;", "")
                .replaceAll("\\s+", " ")
                .replaceAll("作.*?者", "")
                .replaceAll("[?？!！。~：:()（）［］\\[\\]【】]+", ""));
    }


}
