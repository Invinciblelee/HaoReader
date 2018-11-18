package com.monke.monkeybook.help;

import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import com.monke.monkeybook.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterHelp {

    private static String[] CHAPTER_PATTERNS = new String[]{
            "^(.*?([\\d零〇一二两三四五六七八九十百千万0-9\\s]+)[章节回])[、，。　：:.\\s]*",
            "^([\\(（\\[【]*([\\d零〇一二两三四五六七八九十百千万0-9\\s]+)[】\\]）\\)]*)[、，。　：:.\\s]+"
    };

    private static String SPECIAL_PATTERN = "第.*?[卷篇集].*?第.*[章节回].*?";

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

    public static String getFormatChapterName(String chapterName) {
        if (TextUtils.isEmpty(chapterName)) {
            return "";
        }

        chapterName = StringUtils.fullToHalf(chapterName);

        if(chapterName.matches(SPECIAL_PATTERN)){
            return chapterName;
        }

        for (String str : CHAPTER_PATTERNS) {
            Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(chapterName);
            if (matcher.find()) {
                String SIMPLE_CHAPTER_PATTERN = "^第.*\\d+.*[章节回]";//eg. 第20-24章
                if(matcher.group(0).matches(SIMPLE_CHAPTER_PATTERN)){
                    return matcher.replaceFirst("$1 ");
                }
                int num = StringUtils.stringToInt(matcher.group(2));
                chapterName = num > 0 ? matcher.replaceFirst("第" + num + "章 ") : matcher.replaceFirst("$1 ");
                return chapterName;
            }
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
