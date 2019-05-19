package com.monke.monkeybook.help;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ChapterContentHelp {

    public static String getCacheFolderPath(DownloadBookBean book) {
        return getChapterFolderName(book.getName(), book.getAuthor()) + File.separator + formatFileName(book.getTag()) + File.separator;
    }

    public static String getCacheFolderPath(BookInfoBean book) {
        return getChapterFolderName(book.getName(), book.getAuthor()) + File.separator + formatFileName(book.getTag()) + File.separator;
    }

    public static String getCacheFileName(ChapterBean chapter) {
        return String.format(Locale.getDefault(), "%d-%s", chapter.getDurChapterIndex(), chapter.getDurChapterName());
    }

    public static String getCacheFileName(BookContentBean content) {
        return String.format(Locale.getDefault(), "%d-%s", content.getDurChapterIndex(), content.getDurChapterName());
    }

    public static String getChapterFolderName(String name, String author) {
        return formatFileName(String.format(Locale.getDefault(), "%s(%s)", name, author));
    }

    public static boolean isChapterCached(DownloadBookBean book, ChapterBean chapter) {
        return isChapterCached(getCacheFolderPath(book), getCacheFileName(chapter));
    }

    public static boolean isChapterCached(BookInfoBean book, ChapterBean chapter) {
        return isChapterCached(getCacheFolderPath(book), getCacheFileName(chapter));
    }

    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存过)
     */
    // be careful to use this method, the storage path (folderName) has been changed
    private static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(Constant.BOOK_CHAPTER_PATH + folderName, formatFileName(fileName) + FileHelp.SUFFIX_NB);
        return file.exists();
    }

    /**
     * 删除章节文件
     */
    public static void delChapter(String folderName, String fileName) {
        FileHelp.deleteFile(Constant.BOOK_CHAPTER_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    /**
     * 创建或获取存储文件
     */
    public static File getBookFile(String folderName, String fileName) {
        return FileHelp.getFile(Constant.BOOK_CHAPTER_PATH + folderName, formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    private static String formatFileName(String fileName) {
        return fileName.replace("/", "")
                .replace(":", "")
                .replace(".", "");
    }

    /**
     * 存储章节
     */
    public static boolean saveChapterInfo(String folderName, String fileName, String content) {
        if (content == null) {
            return false;
        }
        File file = getBookFile(folderName, formatFileName(fileName));
        //获取流并存储
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            writer.flush();
            return true;
        } catch (IOException ignore) {
        }
        return false;
    }

    public static String getChapterCache(BookShelfBean bookShelfBean, ChapterBean chapter) {
        @SuppressLint("DefaultLocale")
        File file = ChapterContentHelp.getBookFile(ChapterContentHelp.getCacheFolderPath(bookShelfBean.getBookInfoBean()),
                ChapterContentHelp.getCacheFileName(chapter));
        if (!file.exists()) return null;

        byte[] contentByte = DocumentHelper.getBytes(file);
        return new String(contentByte, StandardCharsets.UTF_8);
    }

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
        if (ReplaceRuleManager.getEnabledCount() == 0) {
            return toTraditional(content);
        }
        //替换
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
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
