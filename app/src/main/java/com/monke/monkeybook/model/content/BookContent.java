package com.monke.monkeybook.model.content;

import android.os.Parcelable;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;

final class BookContent {

    private String tag;
    private BookSourceBean bookSourceBean;

    private boolean isAJAX;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;

        isAJAX = bookSourceBean.ajaxRuleBookContent();
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final ChapterBean chapter) {
        OutAnalyzer<?> analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(),
                new AnalyzeConfig().tag(tag)
                        .bookSource(bookSourceBean)
                        .baseURL(chapter.getDurChapterUrl())
                        .extra("chapter", chapter));
        return analyzer.getContent(s);
    }


    boolean isAJAX() {
        return isAJAX;
    }
}
