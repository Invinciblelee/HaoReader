package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import java.util.List;

import io.reactivex.Observable;

final class BookList {

    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    private boolean isAJAX;

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
        isAJAX = bookSourceBean.ajaxSearchList();
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final String response, final String baseUrl) {
        OutAnalyzer<?> analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(),
                new AnalyzeConfig().tag(tag)
                        .name(name)
                        .bookSource(bookSourceBean)
                        .baseURL(baseUrl));
        return analyzer.getSearchBooks(response);
    }

    boolean isAJAX() {
        return isAJAX;
    }
}
