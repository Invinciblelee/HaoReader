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
            "^(.*?([\\d零〇一二两三四五六七八九十百千万0-9]+)[章节回场]?)[\\s、，。　：:.]",
            "^([(（\\[【]?([\\d零〇一二两三四五六七八九十百千万0-9]+)[】\\]）)]?)([\\s、，。　：:.]|$)",
            "^(第([\\d零〇一二两三四五六七八九十百千万0-9]+)[卷篇集章节回场集])$"
    };

    private static final String SPECIAL_REGEX = "[\\s、，。　：:._]?第?([\\d零〇一二两三四五六七八九十百千万0-9]+)[章节回场][\\s、，。　：:.]?";

    private static final String SPECIAL_PATTERN = "第.*?[卷篇集].*?" + SPECIAL_REGEX;

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
                return StringUtils.parseInt(matcher.group(2));
            }
        }
        return -1;
    }

    public static String formatChapterName(String chapterName) {
        if (TextUtils.isEmpty(chapterName)) {
            return "";
        }

        chapterName = StringUtils.fullToHalf(chapterName);
        chapterName = StringUtils.trim(chapterName.replace("（", "(")
                .replace("）", ")")
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
                chapterName = num > 0 ? matcher.replaceFirst("第" + num + "章 ") : matcher.replaceFirst("$ ");
                return chapterName;
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
