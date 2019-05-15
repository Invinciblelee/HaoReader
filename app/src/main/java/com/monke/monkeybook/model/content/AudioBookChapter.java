package com.monke.monkeybook.model.content;

import android.os.Parcelable;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeConfig;
import com.monke.monkeybook.model.analyzeRule.AnalyzerFactory;
import com.monke.monkeybook.model.analyzeRule.OutAnalyzer;

import io.reactivex.Observable;


/**
 * 模拟点击网页
 * <p>
 * 方法一：$('#clickId').trigger("click");    'p' 标签选择器 ‘.class’ 类选择器 ‘#id’ id选择器
 * <p>
 * 方法二：var e = document.createEvent("MouseEvents");
 * e.initEvent("click", true, true);
 * document.getElementsByClassName("clickClass")[0].dispatchEvent(e);
 * <p>
 * 方法三：document.getElementById("clickId").click();
 */
final class AudioBookChapter {

    private final String tag;
    private final BookSourceBean bookSourceBean;

    private boolean isAJAX;
    private boolean isSniff;
    private String suffix;
    private String javaScript;

    private OutAnalyzer<?> analyzer;

    AudioBookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;

        this.isAJAX = bookSourceBean.ajaxRuleBookContent();
        this.javaScript = bookSourceBean.getAjaxJavaScript();

        if (bookSourceBean.sniffRuleBookContent()) {
            isSniff = true;
            suffix = bookSourceBean.getRealRuleBookContent();
        }
    }

    Observable<ChapterBean> analyzeAudioChapter(final String s, final ChapterBean chapter) {
        if (isSniff) {
            chapter.setDurChapterPlayUrl(s);
            return Observable.just(chapter);
        } else {
            if (analyzer == null) {
                analyzer = AnalyzerFactory.create(bookSourceBean.getBookSourceRuleType(), new AnalyzeConfig()
                        .tag(tag).bookSource(bookSourceBean));
            }
            analyzer.apply(analyzer.newConfig()
                    .baseURL(chapter.getDurChapterUrl())
                    .extra("chapter", (Parcelable) chapter));
            return analyzer.getAudioLink(s)
                    .map(url -> {
                        chapter.setDurChapterPlayUrl(url);
                        return chapter;
                    });
        }
    }


    String getSuffix() {
        return suffix;
    }


    boolean isAJAX() {
        return isAJAX;
    }

    String getJavaScript() {
        return javaScript;
    }
}
