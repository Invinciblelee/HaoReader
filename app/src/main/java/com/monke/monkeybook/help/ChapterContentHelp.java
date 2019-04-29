package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

public class ChapterContentHelp {

    /**
     * 转繁体
     */
    private static String toTraditional(String content) {
        int convert = ReadBookControl.getInstance().getTextConvert();
        switch (convert) {
            case 0:
                break;
            case 1:
                content = ChineseUtils.toSimplified(content);
                break;
            case 2:
                content = ChineseUtils.toTraditional(content);
                break;
        }
        return content;
    }

    /**
     * 替换净化
     */
    public static String replaceContent(String bookName, String bookTag, String content) {
        if (ReplaceRuleManager.getInstance().getEnabled().size() == 0) {
            return toTraditional(content);
        }
        //替换
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getInstance().getEnabled()) {
            if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                try {
                    content = content.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement()).trim();
                } catch (Exception ignored) {
                }
            }
        }
        return toTraditional(content);
    }

    private static boolean isUseTo(String useTo, String bookName, String bookTag) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
