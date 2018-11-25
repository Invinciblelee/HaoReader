package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.monke.monkeybook.utils.StringUtils;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class FormatWebText {

    public static String getContent(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return trim(str.replace("\r", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace("&nbsp;", "")
                .replaceAll("\\s", " "));
    }

    public static String getBookName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        str = StringUtils.fullToHalf(str);

        return trim(str.replace("&nbsp;", "")
                .replace(":", "：")
                .replace(",", "，")
                .replaceAll("\\s+", " ")
                .replaceAll("[?？!！。~]+", ""))
                .replaceAll("([\\[【(].*[)】\\]])", "");
    }

    public static String getAuthor(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return trim(str.replace("&nbsp;", "")
                .replaceAll("\\s+", " ")
                .replaceAll("作.*?者", "")
                .replaceAll("[?？!！。~：:()（）【】]+", ""));
    }


    public static String trim(String s) {
        String result = "";
        if (null != s && !"".equals(s)) {
            result = s.replaceAll("^[　*| *| *|//s*]*", "").replaceAll("[　*| *| *|//s*]*$", "");
        }
        return result;
    }

}
