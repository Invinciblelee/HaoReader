package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.monke.monkeybook.utils.StringUtils;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class FormatWebText {

    public static String getBookName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return StringUtils.trim(str.replace("&nbsp;", "")
                .replace(":", "：")
                .replace(",", "，")
                .replaceAll("\\s+", " ")
                .replaceAll("[?？!！。~]+", ""))
                .replaceAll("([\\[【（(].*[)）】\\]])", "");
    }

    public static String getAuthor(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        return StringUtils.trim(str.replace("&nbsp;", "")
                .replaceAll("\\s+", " ")
                .replaceAll("作.*?者", "")
                .replaceAll("[?？!！。~：:()（）【】]+", ""));
    }


}
