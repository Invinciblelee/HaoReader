package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;

final class BookContent {
    private final OutAnalyzer<?> analyzer;

    private boolean isAJAX;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).bookSource(bookSourceBean));

        isAJAX = bookSourceBean.ajaxRuleBookContent();
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final ChapterBean chapter) {
        analyzer.apply(analyzer.newConfig()
                .baseURL(chapter.getDurChapterUrl())
                .extra("chapter", chapter));
        return analyzer.getContent(s);
    }


    boolean isAJAX() {
        return isAJAX;
    }
}
