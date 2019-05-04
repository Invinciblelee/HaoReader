//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;

public interface IWebBookModel {
    /**
     * 网络请求并解析书籍信息
     */
    Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean);

    /**
     * 网络解析图书目录
     */
    Observable<BookShelfBean> getChapterList(BookShelfBean bookShelfBean);

    /**
     * 章节缓存
     */
    Observable<BookContentBean> getBookContent(BookInfoBean bookInfo, ChapterBean chapter);

    /**
     * 其他站点资源整合搜索
     */
    Observable<List<SearchBookBean>> searchBook(String tag, String content, int page);

    /**
     * 发现
     */
    Observable<List<SearchBookBean>> findBook(String tag, String url, int page);

    /**
     * 听书章节
     */
    Observable<ChapterBean> processAudioChapter(String tag, ChapterBean chapter);
}
