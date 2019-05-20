package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import java.util.List;

import io.reactivex.Observable;

final class BookChapters {

    private String tag;
    private BookSourceBean bookSourceBean;

    BookChapters(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<List<ChapterBean>> analyzeChapters(final String s, final BookShelfBean bookShelfBean) {
        OutAnalyzer<?> analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).bookSource(bookSourceBean));
        analyzer.apply(analyzer.newConfig()
                .baseURL(bookShelfBean.getBookInfoBean().getChapterListUrl())
                .extra("noteUrl", bookShelfBean.getNoteUrl())
                .variableStore(bookShelfBean));
        return analyzer.getChapters(s);
    }

}
