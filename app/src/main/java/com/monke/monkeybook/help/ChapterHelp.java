package com.monke.monkeybook.help;

import android.text.TextUtils;
import android.util.Log;

import com.monke.monkeybook.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterHelp {

    private static Pattern chapterNamePattern = Pattern.compile("^(.*?([\\d零〇一二两三四五六七八九十百千万0-9\\s]+)[章节篇回集])[、，。　：:.\\s]*");

    public static int guessChapterNum(String name) {
        if (TextUtils.isEmpty(name) || name.matches("第.*?卷.*?第.*[章节回]"))
            return -1;
        Matcher matcher = chapterNamePattern.matcher(name);
        if (matcher.find()) {
            return StringUtils.stringToInt(matcher.group(2));
        }
        return -1;
    }

    public static String getFormatChapterName(String chapterName) {
        if(chapterName == null) return "";
        Matcher matcher = chapterNamePattern.matcher(chapterName);
        if (matcher.find()) {
            int num = StringUtils.stringToInt(matcher.group(2));
            chapterName = num > 0 ? matcher.replaceFirst("第" + num + "章 ") : matcher.replaceFirst("$1 ");
            return chapterName;
        }
        return chapterName;
    }


    public String getPureChapterName(String chapterName) {
        return chapterName == null ? ""
                : StringUtils.fullToHalf(chapterName).replaceAll("\\s", "")
                .replaceAll("^第.*?章|[(\\[][^()\\[\\]]{2,}[)\\]]$", "")
                .replaceAll("[^\\w\\u4E00-\\u9FEF〇\\u3400-\\u4DBF\\u20000-\\u2A6DF\\u2A700-\\u2EBEF]", "");
        // 所有非字母数字中日韩文字 CJK区+扩展A-F区
    }

}
