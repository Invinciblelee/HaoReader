package com.monke.monkeybook.model.content;

import android.os.Bundle;
import android.text.TextUtils;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;

class BookContent {
    private boolean isAJAX;

    private OutAnalyzer analyzer;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        String ruleBookContent = bookSourceBean.getRuleBookContent();
        if (!TextUtils.equals(Constant.RuleType.JSON, bookSourceBean.getBookSourceRuleType()) && ruleBookContent.startsWith("$")) {
            isAJAX = true;
        }

        analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).bookSource(bookSourceBean));
    }

    Observable<BookContentBean> analyzeBookContent(final String s, final ChapterListBean chapter) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
                e.onComplete();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable("chapter", chapter);
            analyzer.apply(analyzer.newConfig().baseURL(chapter.getDurChapterUrl()).extras(bundle));

            e.onNext(analyzer.getDelegate().getContent(s));
            e.onComplete();
        });
    }


    boolean isAJAX() {
        return isAJAX;
    }
}
