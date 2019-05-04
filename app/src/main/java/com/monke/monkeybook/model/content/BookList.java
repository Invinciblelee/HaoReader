package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

final class BookList {

    private final OutAnalyzer<?> analyzer;

    BookList(String tag, String name, BookSourceBean bookSourceBean) {
        this.analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).name(name).bookSource(bookSourceBean));
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final String response, final String baseUrl) {
        return Observable.create(e -> {
            analyzer.apply(analyzer.newConfig().baseURL(baseUrl));

            List<SearchBookBean> searchBookBeans = analyzer.getSearchBooks(response);
            e.onNext(searchBookBeans);
            e.onComplete();
        });
    }

}
