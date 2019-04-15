package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

public interface ContentDelegate {

    List<SearchBookBean> getSearchBooks(String source);

    BookShelfBean getBook(String source);

    List<ChapterBean> getChapters(String source);

    BookContentBean getContent(String source);

    String getAudioLink(String source);
}
