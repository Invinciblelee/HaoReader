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
            "^(.{0,20}?([\\d零〇一二两三四五六七八九十百千万]+)[　\\s]?(章节$|[章节回场]))[\\s、,，。　：:._]{0,2}(?!([）】\\)\\]])$)",
            "^(第([\\d零〇一二两三四五六七八九十百千万]+)(章节|[章节回场]))$",
            "^(\\d{1,7}[\\s、，。　：:.]?)(?!([-：:.\\s]{0,2}\\d+))",
            "^([\\d零〇一二两三四五六七八九十百千万]{1,7})$",
            "^(.{0,20}?([\\d零〇一二两三四五六七八九十百千万]+)[章节回场]?)[\\s、,，。　：:._]{1,2}(?!([-：:.\\s]{0,2}\\d+))",
            "^([(（\\[【]?([\\d零〇一二两三四五六七八九十百千万]{1,7})[】\\]）)]?)[\\s、,，。　：:._]{0,2}(?!([-：:.\\s]{0,2}\\d+))"
    };

    private static final String SPECIAL_PATTERN = "^.{0,20}[卷篇集].*?[\\d零〇一二两三四五六七八九十百千万]+[章节回场][\\s、,，。　：:._]?";

    private TextProcessor() {

    }

    public static int guessChapterNum(String chapterName) {
        if (TextUtils.isEmpty(chapterName)) {
            return -1;
        }

        chapterName = StringUtils.trim(StringUtils.fullToHalf(chapterName)
                .replaceAll("\\s+", " "));


        if (isMatches(chapterName, SPECIAL_PATTERN)) {
            return -1;
        }

        for (int i = 0; i < CHAPTER_PATTERNS.length; i++) {
            String chapterPattern = CHAPTER_PATTERNS[i];
            Pattern pattern = Pattern.compile(chapterPattern, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(chapterName);
            if (matcher.find()) {
                int num = StringUtils.stringToInt(matcher.group(i == 2 || i == 3 ? 1 : 2));
                if (num > 0) {
                    return num;
                }
            }
        }
        return -1;
    }

    private static boolean isMatches(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static String formatChapterName(String chapterName) {
        if (TextUtils.isEmpty(chapterName)) {
            return "";
        }

        chapterName = StringUtils.trim(StringUtils.fullToHalf(chapterName)
                .replaceAll("\\s+", " "));

        if (isMatches(chapterName, SPECIAL_PATTERN)) {
            return chapterName;
        }

        for (int i = 0; i < CHAPTER_PATTERNS.length; i++) {
            String chapterPattern = CHAPTER_PATTERNS[i];
            Pattern pattern = Pattern.compile(chapterPattern, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(chapterName);
            if (matcher.find()) {
                String group = matcher.group(i == 2 || i == 3 ? 1 : 2);
                int num = StringUtils.stringToInt(group);
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

    public static String formatKeyword(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            String regex = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

            //去网址
            keyword = keyword.replaceAll(regex, "");

            int start = keyword.indexOf("《");
            int end = keyword.indexOf("》");
            if (start >= 0 && end > 1) {
                keyword = keyword.substring(start + 1, end);
            } else if (keyword.length() > 16) {
                keyword = keyword.substring(0, 16);
            }
        }

        return keyword;
    }
}
