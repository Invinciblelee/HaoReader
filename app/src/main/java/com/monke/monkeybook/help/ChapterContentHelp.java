package com.monke.monkeybook.help;

import android.text.TextUtils;
import android.util.Log;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

import java.util.List;

public class ChapterContentHelp {

    /**
     * 转繁体
     */
    private static String toTraditional(int convert, String content) {
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
        //替换
        List<ReplaceRuleBean> enabled = ReplaceRuleManager.getInstance().getEnabled();
        if (enabled != null && !enabled.isEmpty()) {
            String[] paragraphs = content.split("\n\u3000\u3000");
            StringBuilder contentBuilder = new StringBuilder();
            for (String paragraph : paragraphs) {
                for (ReplaceRuleBean replaceRule : enabled) {
                    if (isUseTo(replaceRule.getUseTo(), bookName, bookTag)) {
                        try {
                            if(replaceRule.getIsRegex()) {
                                paragraph = paragraph.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                            }else {
                                paragraph = paragraph.replace(replaceRule.getRegex(), replaceRule.getReplacement());
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
                if (paragraph.length() > 0) {
                    if (contentBuilder.length() == 0) {
                        contentBuilder.append(paragraph);
                    } else {
                        contentBuilder.append("\n").append("\u3000\u3000").append(paragraph);
                    }
                }
            }
            content = contentBuilder.toString();
        }
        return toTraditional(ReadBookControl.getInstance().getTextConvert(), content);
    }

    private static boolean isUseTo(String useTo, String bookName, String bookTag) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
