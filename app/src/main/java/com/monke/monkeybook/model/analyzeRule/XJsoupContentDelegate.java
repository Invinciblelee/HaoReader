package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

class XJsoupContentDelegate implements ContentDelegate {

    private OutAnalyzer<Element, Element> mAnalyzer;
    private AnalyzeConfig mConfig;
    private BookSourceBean mBookSource;

    XJsoupContentDelegate(@NonNull OutAnalyzer<Element, Element> mAnalyzer) {
        this.mAnalyzer = mAnalyzer;
        this.mConfig = mAnalyzer.getConfig();
        this.mBookSource = mConfig.getBookSource();
    }

    @Override
    public List<SearchBookBean> getSearchBooks(String source) {
        List<Element> booksE = mAnalyzer.getRawList(source, mBookSource.getRuleSearchList());
        if (null != booksE && booksE.size() > 0) {
            List<SearchBookBean> books = new ArrayList<>();
            for (Element element : booksE) {
                SearchBookBean item = new SearchBookBean();
                item.setTag(mConfig.getTag());
                item.setOrigin(mConfig.getName());
                item.setBookType(mBookSource.getBookSourceType());
                item.setAuthor(FormatWebText.getAuthor(mAnalyzer.getResultContent(element, mBookSource.getRuleSearchAuthor())));
                item.setKind(mAnalyzer.getResultContent(element, mBookSource.getRuleSearchKind()));
                item.setLastChapter(FormatWebText.trim(mAnalyzer.getResultContent(element, mBookSource.getRuleSearchLastChapter())));
                item.setName(FormatWebText.getBookName(mAnalyzer.getResultContent(element, mBookSource.getRuleSearchName())));
                item.setNoteUrl(mAnalyzer.getResultUrl(element, mBookSource.getRuleSearchNoteUrl()));
                item.setIntroduce(mAnalyzer.getResultContent(element, mBookSource.getRuleIntroduce()));
                if (isEmpty(item.getNoteUrl())) {
                    item.setNoteUrl(mConfig.getBaseURL());
                }
                item.setCoverUrl(mAnalyzer.getResultUrl(element, mBookSource.getRuleSearchCoverUrl()));
                if (!isEmpty(item.getName())) {
                    books.add(item);
                }
            }
            return books;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public BookShelfBean getBook(String source) {
        Element element = mAnalyzer.parseSource(source);
        BookShelfBean book = mConfig.getExtras().getParcelable("book");
        assert book != null;
        BookInfoBean bookInfoBean = book.getBookInfoBean();
        if (bookInfoBean == null) {
            bookInfoBean = new BookInfoBean();
        }
        bookInfoBean.setNoteUrl(mConfig.getBaseURL());   //id
        bookInfoBean.setTag(mConfig.getTag());
        if (isEmpty(bookInfoBean.getCoverUrl())) {
            bookInfoBean.setCoverUrl(mAnalyzer.getResultUrl(element, mBookSource.getRuleCoverUrl()));
        }
        if (isEmpty(bookInfoBean.getName())) {
            bookInfoBean.setName(mAnalyzer.getResultContent(element, mBookSource.getRuleBookName()));
        }
        if (isEmpty(bookInfoBean.getAuthor())) {
            bookInfoBean.setAuthor(FormatWebText.getAuthor(mAnalyzer.getResultContent(element, mBookSource.getRuleBookAuthor())));
        }
        if (isEmpty(bookInfoBean.getIntroduce())) {
            bookInfoBean.setIntroduce(mAnalyzer.getResultContent(element, mBookSource.getRuleIntroduce()));
        }
        String chapterUrl = mAnalyzer.getResultUrl(element, mBookSource.getRuleChapterUrl());
        if (isEmpty(chapterUrl)) {
            bookInfoBean.setChapterListUrl(mConfig.getBaseURL());
        } else {
            bookInfoBean.setChapterListUrl(chapterUrl);
        }
        bookInfoBean.setBookType(mBookSource.getBookSourceType());
        bookInfoBean.setOrigin(mConfig.getName());
        if (isEmpty(book.getLastChapterName())) {
            book.setLastChapterName(mAnalyzer.getResultContent(element, mBookSource.getRuleLastChapter()));
        }
        return book;
    }

    @Override
    public List<ChapterListBean> getChapters(String source) {
        boolean reverse = false;
        String ruleChapterList = mBookSource.getRuleChapterList();
        if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
            reverse = true;
            ruleChapterList = ruleChapterList.substring(1);
        }

        RawResult<List<ChapterListBean>> webChapterBean = getRawChaptersResult(source, ruleChapterList);
        List<ChapterListBean> chapterList = webChapterBean.result;

        List<String> nextUrls = new ArrayList<>();
        int retryCount = 0;
        while (!isEmpty(webChapterBean.nextUrl) && !nextUrls.contains(webChapterBean.nextUrl)) {
            Call<String> call = OkHttpHelper.getInstance().createService(mBookSource.getBookSourceUrl(), IHttpGetApi.class)
                    .getWebContentCall(webChapterBean.nextUrl, AnalyzeHeaders.getMap(mBookSource.getHttpUserAgent()));
            String response = "";
            try {
                response = call.execute().body();
            } catch (Exception ignore) {
            }
            if (!isEmpty(response)) {
                nextUrls.add(webChapterBean.nextUrl);
                retryCount = 0;
            } else {
                retryCount += 1;
                if (retryCount > 5) {
                    break;
                } else {//失败重试
                    continue;
                }
            }
            webChapterBean = getRawChaptersResult(response, ruleChapterList);
            chapterList.addAll(webChapterBean.result);
        }
        if (reverse) {
            Collections.reverse(chapterList);
        }
        return chapterList;
    }

    @Override
    public BookContentBean getContent(String source) {
        ChapterListBean chapter = mConfig.getExtras().getParcelable("chapter");
        assert chapter != null;
        BookContentBean bookContentBean = new BookContentBean();
        bookContentBean.setDurChapterName(chapter.getDurChapterName());
        bookContentBean.setDurChapterIndex(chapter.getDurChapterIndex());
        bookContentBean.setDurChapterUrl(chapter.getDurChapterUrl());
        bookContentBean.setTag(chapter.getTag());

        String ruleBookContent = mBookSource.getRuleBookContent();
        if (ruleBookContent.startsWith("$")) {
            ruleBookContent = ruleBookContent.substring(1);
        }

        RawResult<String> webContentBean = getRawContentResult(source, bookContentBean.getDurChapterUrl(), ruleBookContent);
        bookContentBean.setDurChapterContent(webContentBean.result);

        List<String> nextUrls = new ArrayList<>();
        int retryCount = 0;
        while (!TextUtils.isEmpty(webContentBean.nextUrl) && !nextUrls.contains(webContentBean.nextUrl)) {
            Call<String> call = OkHttpHelper.getInstance().createService(mBookSource.getBookSourceUrl(), IHttpGetApi.class)
                    .getWebContentCall(webContentBean.nextUrl, AnalyzeHeaders.getMap(mBookSource.getHttpUserAgent()));
            String response = "";
            try {
                response = call.execute().body();
            } catch (Exception ignore) {
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
            webContentBean = getRawContentResult(response, webContentBean.nextUrl, ruleBookContent);
            if (!TextUtils.isEmpty(webContentBean.result)) {
                bookContentBean.setDurChapterContent(bookContentBean.getDurChapterContent() + "\n" + webContentBean.result);
            }
        }
        return bookContentBean;
    }

    private RawResult<List<ChapterListBean>> getRawChaptersResult(String s, String ruleChapterList) {
        RawResult<List<ChapterListBean>> webChapterBean = new RawResult<>();
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        Element doc = mAnalyzer.parseSource(s);
        if (!isEmpty(mBookSource.getRuleChapterUrlNext())) {
            webChapterBean.nextUrl = mAnalyzer.getResultUrl(doc, mBookSource.getRuleChapterUrlNext());
        }
        List<Element> elements = mAnalyzer.getRawList(doc, ruleChapterList);
        int chapterIndex = 0;
        for (Element element : elements) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(mAnalyzer.getResultUrl(element, mBookSource.getRuleContentUrl()));   //id
            temp.setDurChapterName(mAnalyzer.getResultContent(element, mBookSource.getRuleChapterName()));
            temp.setTag(mConfig.getTag());
            if (!isEmpty(temp.getDurChapterUrl())
                    && !isEmpty(temp.getDurChapterName())
                    && !chapterBeans.contains(temp)) {
                temp.setDurChapterIndex(chapterIndex);
                chapterBeans.add(temp);
                chapterIndex++;
            }
        }
        webChapterBean.result = chapterBeans;
        return webChapterBean;
    }


    private RawResult<String> getRawContentResult(String s, String chapterUrl, String ruleContent) {
        RawResult<String> webContentBean = new RawResult<>();
        try {
            Element element = mAnalyzer.parseSource(s);
            webContentBean.result = mAnalyzer.getResultContent(element, ruleContent);
            if (!TextUtils.isEmpty(mBookSource.getRuleContentUrlNext())) {
                webContentBean.nextUrl = mAnalyzer.getResultUrl(element, mBookSource.getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            webContentBean.result = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + ex.getMessage();
        }
        return webContentBean;
    }

}
