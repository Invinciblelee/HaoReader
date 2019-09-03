package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.ReplaceRuleManager;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;
import java.util.List;

/**
 * Created by GKF on 2018/1/30.
 * 数据备份
 */

public class DataBackup {

    public static DataBackup getInstance() {
        return new DataBackup();
    }

    public boolean run() {
        File dir = FileHelp.getFolder(FileUtil.getSdCardPath(), "HaoYue/backups");
        String dirPath = dir.getAbsolutePath();
        backupBookShelf(dirPath);
        backupBookSource(dirPath);
        backupSearchHistory(dirPath);
        backupReplaceRule(dirPath);
        backupConfig(dirPath);
        return true;
    }

    private void backupBookShelf(String file) {
        List<BookShelfBean> bookShelfList = BookshelfHelp.queryAllBook();
        if (bookShelfList != null && bookShelfList.size() > 0) {
            for (BookShelfBean bookshelf : bookShelfList) {
                bookshelf.setChapterList(null, false);
            }
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String bookshelf = gson.toJson(bookShelfList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookShelf.json", file);
            DocumentHelper.writeString(bookshelf, docFile);
        }
        BookshelfHelp.queryAllBook();
    }

    private void backupBookSource(String file) {
        List<BookSourceBean> bookSourceList = BookSourceManager.getAll();
        if (bookSourceList != null && bookSourceList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(bookSourceList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSource.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupSearchHistory(String file) {
        List<SearchHistoryBean> searchHistoryBeans = DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao()
                .queryBuilder().list();
        if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(searchHistoryBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSearchHistory.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupReplaceRule(String file) {
        List<ReplaceRuleBean> replaceRuleBeans = ReplaceRuleManager.getAll();
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(replaceRuleBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookReplaceRule.json", file);
            DocumentHelper.writeString(str, docFile);
        }
    }

    private void backupConfig(String file) {
        DocumentFile docFile = DocumentHelper.createFileIfNotExist("config.json", file);
        SharedPreferences pref = ContextHolder.getContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(pref.getAll());
        DocumentHelper.writeString(json, docFile);
    }
}
