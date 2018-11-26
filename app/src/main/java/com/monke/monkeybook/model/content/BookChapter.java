package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeElement;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

public class BookChapter {
    private String tag;
    private BookSourceBean bookSourceBean;

    BookChapter(String tag, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<List<ChapterListBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("目录获取失败"));
                e.onComplete();
                return;
            }
            bookShelfBean.setTag(tag);
            boolean dx = false;
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }

            BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
            WebChapterBean<List<ChapterListBean>> webChapterBean = analyzeChapterList(s, bookInfo.getName(), bookInfo.getChapterUrl(), ruleChapterList);
            List<ChapterListBean> chapterList = webChapterBean.chapters;

            List<String> nextUrls = new ArrayList<>();
            int retryCount = 0;
            while (!TextUtils.isEmpty(webChapterBean.nextUrl) && !nextUrls.contains(webChapterBean.nextUrl)) {
                Call<String> call = OkHttpHelper.getInstance().createService(bookSourceBean.getBookSourceUrl(), IHttpGetApi.class)
                        .getWebContentCall(webChapterBean.nextUrl, AnalyzeHeaders.getMap(bookSourceBean.getHttpUserAgent()));
                String response = "";
                try {
                    response = call.execute().body();
                } catch (Exception exception) {
                    if (!e.isDisposed()) {
                        e.onError(exception);
                    }
                }
                if (!TextUtils.isEmpty(response)) {
                    nextUrls.add(webChapterBean.nextUrl);
                    retryCount = 0;
                } else {
                    retryCount += 1;
                    if(retryCount > 5){
                        break;
                    }else {//失败重试
                        continue;
                    }
                }
                webChapterBean = analyzeChapterList(response, bookInfo.getName(), webChapterBean.nextUrl, ruleChapterList);
                chapterList.addAll(webChapterBean.chapters);
            }
            if (dx) {
                Collections.reverse(chapterList);
            }
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyzeChapterList(String s, String bookName, String chapterUrl, String ruleChapterList) {
        WebChapterBean<List<ChapterListBean>> webChapterBean = new WebChapterBean<>();
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        Document doc = Jsoup.parse(s);
        AnalyzeElement analyzeElement;
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext())) {
            analyzeElement = new AnalyzeElement(doc, chapterUrl);
            webChapterBean.nextUrl = analyzeElement.getResultUrl(bookSourceBean.getRuleChapterUrlNext());
        }
        Elements elements = AnalyzeElement.getElements(doc, ruleChapterList);
        int chapterIndex = 0;
        for (Element element : elements) {
            analyzeElement = new AnalyzeElement(element, chapterUrl);
            ChapterListBean temp = new ChapterListBean();
            temp.setBookName(bookName);
            temp.setDurChapterUrl(analyzeElement.getResultUrl(bookSourceBean.getRuleContentUrl()));   //id
            temp.setDurChapterName(analyzeElement.getResultContent(bookSourceBean.getRuleChapterName()));
            temp.setTag(tag);
            if (!isEmpty(temp.getDurChapterUrl())
                    && !isEmpty(temp.getDurChapterName())
                    && !chapterBeans.contains(temp)) {
                temp.setDurChapterIndex(chapterIndex);
                chapterBeans.add(temp);
                chapterIndex++;
            }
        }
        webChapterBean.chapters = chapterBeans;
        return webChapterBean;
    }

    private static class WebChapterBean<T> {
        private T chapters;

        private String nextUrl;

        private WebChapterBean() {
        }

    }
}
