package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import java.util.List;

import io.reactivex.Observable;

public class BookChapters {

    private OutAnalyzer analyzer;

    BookChapters(String tag, BookSourceBean bookSourceBean) {
        analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).bookSource(bookSourceBean));
    }

    public Observable<List<ChapterListBean>> analyzeChapters(final String s, final String chapterListUrl) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("目录获取失败"));
                e.onComplete();
                return;
            }
            analyzer.apply(analyzer.newConfig().baseURL(chapterListUrl));
            e.onNext(analyzer.getDelegate().getChapters(s));
            e.onComplete();
        });
    }

}
