package com.monke.monkeybook.model.analyzeRule;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;

public interface ContentDelegate {

    Observable<List<SearchBookBean>> getSearchBooks(String source);

    Observable<BookShelfBean> getBook(String source);

    Observable<List<ChapterBean>> getChapters(String source);

    Observable<BookContentBean> getBookContent(String source);

    Observable<String> getAudioContent(String source);
}
