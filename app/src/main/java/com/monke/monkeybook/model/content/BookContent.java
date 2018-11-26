package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.ErrorAnalyContentManager;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

public class BookContent {
    private String tag;
    private BookSourceBean bookSourceBean;
    private String ruleBookContent;
    private boolean isAJAX;

    BookContent(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$")) {
            isAJAX = true;
            ruleBookContent = ruleBookContent.substring(1);
        }
    }

    public Observable<BookContentBean> analyzeBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("内容获取失败"));
                e.onComplete();
                return;
            }
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterIndex(durChapterIndex);
            bookContentBean.setDurChapterUrl(durChapterUrl);
            bookContentBean.setTag(tag);

            WebContentBean webContentBean = analyzeBookContent(s, durChapterUrl);
            bookContentBean.setDurChapterContent(webContentBean.content);

            List<String> nextUrls = new ArrayList<>();
            int retryCount = 0;
            while (!TextUtils.isEmpty(webContentBean.nextUrl) && !nextUrls.contains(webContentBean.nextUrl)) {
                Call<String> call = OkHttpHelper.getInstance().createService(bookSourceBean.getBookSourceUrl(), IHttpGetApi.class)
                        .getWebContentCall(webContentBean.nextUrl, AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                String response = "";
                try {
                    response = call.execute().body();
                } catch (Exception exception) {
                    if (!e.isDisposed()) {
                        e.onError(exception);
                    }
                }
                if (!TextUtils.isEmpty(response)) {
                    nextUrls.add(webContentBean.nextUrl);
                    retryCount = 0;
                } else {
                    retryCount += 1;
                    if (retryCount > 5) {
                        break;
                    } else {
                        continue;
                    }
                }
                webContentBean = analyzeBookContent(response, webContentBean.nextUrl);
                if (!TextUtils.isEmpty(webContentBean.content)) {
                    bookContentBean.setDurChapterContent(bookContentBean.getDurChapterContent() + "\n" + webContentBean.content);
                }
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    private WebContentBean analyzeBookContent(final String s, final String chapterUrl) {
        WebContentBean webContentBean = new WebContentBean();
        try {
            Document doc = Jsoup.parse(s);
            AnalyzeElement analyzeElement = new AnalyzeElement(doc, chapterUrl);
            webContentBean.content = analyzeElement.getResultContent(ruleBookContent);
            if (!TextUtils.isEmpty(bookSourceBean.getRuleContentUrlNext())) {
                webContentBean.nextUrl = analyzeElement.getResultUrl(bookSourceBean.getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorAnalyContentManager.getInstance().writeNewErrorUrl(chapterUrl);
            webContentBean.content = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + ex.getMessage();
        }
        return webContentBean;
    }

    public boolean isAJAX() {
        return isAJAX;
    }

    private static class WebContentBean {
        private String content;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
