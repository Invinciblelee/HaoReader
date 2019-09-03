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
            "^(.{0,20}?([\\d零〇一二两三四五六七八九十百千万]+)[章节回场]){1,2}[\\s、，。　：:.]?",
            "^(.{0,20}?([\\d零〇一二两三四五六七八九十百千万]+)[章节回场]?){1,2}[\\s、，。　：:.](?!([-\\s]?[-\\d零〇一二两三四五六七八九十百千万]+))",
            "^(第([\\d零〇一二两三四五六七八九十百千万]+)[卷篇集章节回场])$",
            "^([(（\\[【]?([\\d零〇一二两三四五六七八九十百千万]{0,7})[】\\]）)]?)[\\s、，。　：:.](?!([-\\s]?[-\\d零〇一二两三四五六七八九十百千万]+))"
    };

    private static final String SPECIAL_REGEX = "[\\s、，。　：:._]?第?([\\d零〇一二两三四五六七八九十百千万]+)[章节回场][\\s、，。　：:.]?";

    private static final String SPECIAL_PATTERN = "第.*?[卷篇集].*?" + SPECIAL_REGEX;

    private TextProcessor() {
    }

    public static int guessChapterNum(String chapterName) {
        if (TextUtils.isEmpty(chapterName) || chapterName.matches(SPECIAL_PATTERN)) {
            return -1;
        }

        chapterName = StringUtils.trim(StringUtils.fullToHalf(chapterName)
                .replace("(", "（")
                .replace(")", "）")
                .replaceAll("[\\[\\]【】]+", "")
                .replaceAll("\\s+", " "));

        Pattern pattern = Pattern.compile(SPECIAL_PATTERN);
        Matcher matcher = pattern.matcher(chapterName);
        if (matcher.find()) {
            return StringUtils.stringToInt(matcher.group(1));
        }

        for (String chapterPattern : CHAPTER_PATTERNS) {
            pattern = Pattern.compile(chapterPattern, Pattern.MULTILINE);
            matcher = pattern.matcher(chapterName);
            if (matcher.find()) {
                int num = StringUtils.stringToInt(matcher.group(2));
                if (num > 0) {
                    return num;
                }
            }
        }
        return -1;
    }

    public static String formatChapterName(String chapterName) {
        if (TextUtils.isEmpty(chapterName)) {
            return "";
        }

        chapterName = StringUtils.trim(StringUtils.fullToHalf(chapterName)
                .replace("(", "（")
                .replace(")", "）")
                .replaceAll("[\\[\\]【】]+", "")
                .replaceAll("\\s+", " "));

        Pattern pattern = Pattern.compile(SPECIAL_PATTERN);
        Matcher matcher = pattern.matcher(chapterName);
        if (matcher.find()) {
            int num = StringUtils.stringToInt(matcher.group(1));
            chapterName = chapterName.replaceAll(SPECIAL_REGEX, " 第" + num + "章 ");
            return chapterName;
        }

        for (String chapterPattern : CHAPTER_PATTERNS) {
            pattern = Pattern.compile(chapterPattern, Pattern.MULTILINE);
            matcher = pattern.matcher(chapterName);
            if (matcher.find()) {
                int num = StringUtils.stringToInt(matcher.group(2));
                if (num > 0) {
                    chapterName = matcher.replaceFirst("第" + num + "章 ");
                    break;
                }
            }
        }

        return chapterName;
    }

    public static String formatBookName(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }

        str = StringUtils.fullToHalf(str);

        return StringUtils.trim(str.replaceAll("\\s+", " "));
    }

    public static String formatAuthorName(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }

        str = StringUtils.fullToHalf(str);

        return StringUtils.trim(str.replaceAll("\\s+", " ")
                .replaceAll("作.*?者", "")
                .replaceAll("[?？!！。~：:()（）【】]+", ""));
    }


    public static String formatHtml(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        return html.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")  // 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")               // 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n\u3000\u3000")                   // 移除空行,并增加段前缩进2个汉字
                .replaceAll("^[\\n\\s]+", "\u3000\u3000")
                .replaceAll("[\\n\\s]+$", "");
    }
}
