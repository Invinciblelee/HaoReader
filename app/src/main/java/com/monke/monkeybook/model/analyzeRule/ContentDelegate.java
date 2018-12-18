package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

public interface ContentDelegate {

    List<SearchBookBean> getSearchBooks(String source);

    BookShelfBean getBook(String source);

    List<ChapterListBean> getChapters(String source);

    BookContentBean getContent(String source);
}
