package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import java.util.List;

import io.reactivex.Observable;

final class BookChapters {

    private final OutAnalyzer<?> analyzer;

    BookChapters(String tag, BookSourceBean bookSourceBean) {
        analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).bookSource(bookSourceBean));
    }

    Observable<List<ChapterBean>> analyzeChapters(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("目录获取失败"));
                e.onComplete();
                return;
            }
            analyzer.apply(analyzer.newConfig()
                    .baseURL(bookShelfBean.getBookInfoBean().getChapterListUrl())
                    .variableStore(bookShelfBean)
                    .extra("noteUrl", bookShelfBean.getNoteUrl()));
            List<ChapterBean> chapters = analyzer.getChapters(s);
            e.onNext(chapters);
            e.onComplete();
        });
    }

}
