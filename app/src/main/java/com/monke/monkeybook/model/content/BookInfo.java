package com.monke.monkeybook.model.content;

import android.os.Bundle;
import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;

class BookInfo {
    private OutAnalyzer analyzer;

    BookInfo(String tag, String name, BookSourceBean bookSourceBean) {
        this.analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                .tag(tag).name(name).bookSource(bookSourceBean));
    }

    Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败"));
                e.onComplete();
                return;
            }
            bookShelfBean.setTag(analyzer.getConfig().getTag());
            Bundle bundle = new Bundle();
            bundle.putParcelable("book", bookShelfBean);
            analyzer.apply(analyzer.newConfig().baseURL(bookShelfBean.getNoteUrl()).extras(bundle));
            e.onNext(analyzer.getDelegate().getBook(s));
            e.onComplete();
        });
    }
}
