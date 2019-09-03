package com.monke.monkeybook.help;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.bean.SearchHistoryBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.ReplaceRuleManager;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.SharedPreferencesUtil;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by GKF on 2018/1/30.
 * 数据恢复
 */

public class DataRestore {

    private static final String[] FILTER = {"nightTheme", "shelfGroup"};

    public static DataRestore getInstance() {
        return new DataRestore();
    }

    public Boolean run() throws Exception {
        String dirPath = FileUtil.getSdCardPath() + "/HaoYue/backups";
        restoreConfig(dirPath);
        restoreBookSource(dirPath);
        restoreBookShelf(dirPath);
        restoreSearchHistory(dirPath);
        restoreReplaceRule(dirPath);
        return true;
    }

    private void restoreConfig(String dirPath) throws Exception {
        String json = DocumentHelper.readString("config.json", dirPath);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                Iterator<String> it = jsonObject.keys();
                List<String> filter = Arrays.asList(FILTER);
                while (it.hasNext()) {
                    String key = it.next();
                    if (!filter.contains(key)) {
                        Object value = jsonObject.opt(key);
                        SharedPreferencesUtil.saveData(ContextHolder.getContext(), key, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreBookShelf(String file) throws Exception {
        String json = DocumentHelper.readString("myBookShelf.json", file);
        if (json != null) {
            List<BookShelfBean> bookShelfList = new Gson().fromJson(json, new TypeToken<List<BookShelfBean>>() {
            }.getType());
            for (BookShelfBean bookshelf : bookShelfList) {
                if (bookshelf.getNoteUrl() != null) {
                    DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().insertOrReplace(bookshelf);
                }
                if (bookshelf.getBookInfoBean().getNoteUrl() != null) {
                    DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().insertOrReplace(bookshelf.getBookInfoBean());
                }
            }
        }
    }

    private void restoreBookSource(String file) throws Exception {
        String json = DocumentHelper.readString("myBookSource.json", file);
        if (json != null) {
            List<BookSourceBean> bookSourceBeans = new Gson().fromJson(json, new TypeToken<List<BookSourceBean>>() {
            }.getType());
            for (int i = 0; i < bookSourceBeans.size(); i++) {
                bookSourceBeans.get(i).setSerialNumber(i + 1);
            }
            BookSourceManager.addAll(bookSourceBeans);
        }
    }

    private void restoreSearchHistory(String file) throws Exception {
        String json = DocumentHelper.readString("myBookSearchHistory.json", file);
        if (json != null) {
            List<SearchHistoryBean> searchHistoryBeans = new Gson().fromJson(json, new TypeToken<List<SearchHistoryBean>>() {
            }.getType());
            if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
                DbHelper.getInstance().getDaoSession().getSearchHistoryBeanDao().insertOrReplaceInTx(searchHistoryBeans);
            }
        }
    }

    private void restoreReplaceRule(String file) throws Exception {
        String json = DocumentHelper.readString("myBookReplaceRule.json", file);
        if (json != null) {
            List<ReplaceRuleBean> replaceRuleBeans = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
            }.getType());
            ReplaceRuleManager.saveAll(replaceRuleBeans);
        }
    }
}
