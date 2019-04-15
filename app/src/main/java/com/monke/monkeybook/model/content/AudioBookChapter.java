package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;

public class AudioBookChapter {

    private final String tag;
    private final BookSourceBean bookSourceBean;

    private boolean isAJAX;
    private String suffix;

    private OutAnalyzer analyzer;

    AudioBookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;

        String ruleBookContent = bookSourceBean.getRuleBookContent();
        if (!TextUtils.equals(Constant.RuleType.JSON, bookSourceBean.getBookSourceRuleType()) && ruleBookContent.startsWith("$")) {
            isAJAX = true;
            suffix = ruleBookContent.substring(1);
        }
    }

    Observable<ChapterBean> analyzeAudioChapter(final String s, final ChapterBean chapter) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
                e.onComplete();
                return;
            }

            if (isAJAX) {
                chapter.setDurChapterPlayUrl(s);
            } else {
                if (analyzer == null) {
                    analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                            .tag(tag).bookSource(bookSourceBean));
                }
                analyzer.apply(analyzer.newConfig()
                        .baseURL(chapter.getDurChapterUrl())
                        .extra("chapter", chapter));
                chapter.setDurChapterPlayUrl(analyzer.getAudioLink(s));
            }
            e.onNext(chapter);
            e.onComplete();
        });
    }


    String getSuffix() {
        return suffix;
    }

    boolean isAJAX() {
        return isAJAX;
    }
}
