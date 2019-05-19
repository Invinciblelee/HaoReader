package com.monke.monkeybook.web.controller;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.utils.GsonUtils;
import com.monke.monkeybook.web.utils.ReturnData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SourceController {

    public ReturnData saveSource(String postData) {
        BookSourceBean bookSourceBean = GsonUtils.parseJObject(postData, BookSourceBean.class);
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            return returnData.setErrorMsg("书源名称和URL不能为空");
        }
        BookSourceManager.add(bookSourceBean);
        return returnData.setData("");
    }

    public ReturnData saveSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        List<BookSourceBean> okSources = new ArrayList<>();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }
            BookSourceManager.add(bookSourceBean);
            okSources.add(bookSourceBean);
        }
        return (new ReturnData()).setData(okSources);
    }

    public ReturnData getSource(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg("参数url不能为空，请指定书源地址");
        }
        BookSourceBean bookSourceBean = BookSourceManager.getByUrl(strings.get(0));
        if (bookSourceBean == null) {
            return returnData.setErrorMsg("未找到书源，请检查书源地址");
        }
        return returnData.setData(bookSourceBean);
    }

    public ReturnData getSources() {
        List<BookSourceBean> bookSourceBeans = BookSourceManager.getAll();
        ReturnData returnData = new ReturnData();
        if (bookSourceBeans.size() == 0) {
            return returnData.setErrorMsg("设备书源列表为空");
        }
        return returnData.setData(BookSourceManager.getAll());
    }

    public ReturnData deleteSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        BookSourceManager.deleteAll(bookSourceBeans);
        return (new ReturnData()).setData("已执行"/*okSources*/);
    }
}
