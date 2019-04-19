package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

class XJsoupContentDelegate implements ContentDelegate {

    private OutAnalyzer<?, Element> mAnalyzer;
    private AnalyzeConfig mConfig;
    private BookSourceBean mBookSource;

    XJsoupContentDelegate(@NonNull OutAnalyzer<?, Element> mAnalyzer) {
        this.mAnalyzer = mAnalyzer;
        this.mConfig = mAnalyzer.getConfig();
        this.mBookSource = mConfig.getBookSource();
    }

    @Override
    public List<SearchBookBean> getSearchBooks(String source) {
        mAnalyzer.beginExecute();
        mAnalyzer.setContent(source);

        boolean reverse = false;
        String ruleChapterList = mBookSource.getRuleSearchList();
        if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
            reverse = true;
            ruleChapterList = ruleChapterList.substring(1);
        }


        AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        List<SearchBookBean> books = new ArrayList<>();
        while (collection.hasNext()) {
            try {
                SearchBookBean item = new SearchBookBean();
                item.setTag(mConfig.getTag());
                item.setOrigin(mConfig.getName());
                item.setBookType(mBookSource.getBookSourceType());
                item.setAuthor(FormatWebText.getAuthor(collection.mutable().getResultContent(mBookSource.getRuleSearchAuthor())));
                item.setKind(StringUtils.join(",", collection.mutable().getResultContents(mBookSource.getRuleSearchKind())));
                item.setLastChapter(FormatWebText.trim(collection.mutable().getResultContent(mBookSource.getRuleSearchLastChapter())));
                item.setName(FormatWebText.getBookName(collection.mutable().getResultContent(mBookSource.getRuleSearchName())));
                item.setNoteUrl(collection.mutable().getResultUrl(mBookSource.getRuleSearchNoteUrl()));
                item.setIntroduce(collection.mutable().getResultContent(mBookSource.getRuleIntroduce()));
                item.putVariableMap(collection.mutable().getVariableMap(mBookSource.getRulePersistedVariables()));
                if (isEmpty(item.getNoteUrl())) {
                    item.setNoteUrl(mConfig.getBaseURL());
                }
                item.setCoverUrl(collection.mutable().getResultUrl(mBookSource.getRuleSearchCoverUrl()));
                if (!isEmpty(item.getName())) {
                    books.add(item);
                }
            } catch (Exception ignore) {
            }
        }
        mAnalyzer.endExecute();

        if (reverse) {
            Collections.reverse(books);
        }

        return books;
    }

    @Override
    public BookShelfBean getBook(String source) {
        BookShelfBean book = (BookShelfBean) mConfig.getVariableStore();
        assert book != null;
        BookInfoBean bookInfoBean = book.getBookInfoBean();
        if (bookInfoBean == null) {
            bookInfoBean = new BookInfoBean();
        }

        mAnalyzer.beginExecute();
        mAnalyzer.setContent(source);

        if (isEmpty(bookInfoBean.getCoverUrl())) {
            bookInfoBean.setCoverUrl(mAnalyzer.getResultUrl(mBookSource.getRuleCoverUrl()));
        }
        if (isEmpty(bookInfoBean.getName())) {
            bookInfoBean.setName(mAnalyzer.getResultContent(mBookSource.getRuleBookName()));
        }
        if (isEmpty(bookInfoBean.getAuthor())) {
            bookInfoBean.setAuthor(FormatWebText.getAuthor(mAnalyzer.getResultContent(mBookSource.getRuleBookAuthor())));
        }

        if (isEmpty(bookInfoBean.getIntroduce())) {
            bookInfoBean.setIntroduce(mAnalyzer.getResultContent(mBookSource.getRuleIntroduce()));
        }

        String chapterUrl = mAnalyzer.getResultUrl(mBookSource.getRuleChapterUrl());
        if (isEmpty(chapterUrl)) {
            bookInfoBean.setChapterListUrl(mConfig.getBaseURL());
        } else {
            bookInfoBean.setChapterListUrl(chapterUrl);
        }

        if (isEmpty(book.getLastChapterName())) {
            book.setLastChapterName(mAnalyzer.getResultContent(mBookSource.getRuleLastChapter()));
        }

        book.putVariableMap(mAnalyzer.getVariableMap(mBookSource.getRulePersistedVariables()));

        bookInfoBean.setNoteUrl(mConfig.getBaseURL());   //id
        bookInfoBean.setTag(mConfig.getTag());
        bookInfoBean.setOrigin(mConfig.getName());
        bookInfoBean.setBookType(mBookSource.getBookSourceType());
        mAnalyzer.endExecute();
        return book;
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        String noteUrl = mConfig.getExtras().getString("noteUrl");

        boolean reverse = false;
        String ruleChapterList = mBookSource.getRuleChapterList();
        if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
            reverse = true;
            ruleChapterList = ruleChapterList.substring(1);
        }

        mAnalyzer.beginExecute();

        RawResult<List<ChapterBean>> webChapterBean = getRawChaptersResult(source, ruleChapterList, noteUrl);
        List<ChapterBean> chapterList = webChapterBean.result;

        List<String> nextUrls = new ArrayList<>();
        int retryCount = 0;
        while (!isEmpty(webChapterBean.nextUrl) && !nextUrls.contains(webChapterBean.nextUrl)) {
            Call<String> call = OkHttpHelper.getInstance().createService(mBookSource.getBookSourceUrl(), IHttpGetApi.class)
                    .getWebContentCall(webChapterBean.nextUrl, AnalyzeHeaders.getMap(mBookSource));
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
            webChapterBean = getRawChaptersResult(response, ruleChapterList, noteUrl);
            chapterList.addAll(webChapterBean.result);
        }

        mAnalyzer.endExecute();
        if (!reverse) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<ChapterBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        return chapterList;
    }

    private RawResult<List<ChapterBean>> getRawChaptersResult(String s, String ruleChapterList, String noteUrl) {
        mAnalyzer.setContent(s);
        RawResult<List<ChapterBean>> webChapterBean = new RawResult<>();
        List<ChapterBean> chapterBeans = new ArrayList<>();
        if (!isEmpty(mBookSource.getRuleChapterUrlNext())) {
            webChapterBean.nextUrl = mAnalyzer.getResultUrl(mBookSource.getRuleChapterUrlNext());
        }
        AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        while (collection.hasNext()) {
            String url = collection.mutable().getResultUrl(mBookSource.getRuleContentUrl());   //id
            String name = collection.mutable().getResultContent(mBookSource.getRuleChapterName());
            if (!isEmpty(url) && !isEmpty(name)) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(mConfig.getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterBeans.add(chapterBean);
            }
        }

        webChapterBean.result = chapterBeans;
        return webChapterBean;
    }

    @Override
    public BookContentBean getContent(String source) {
        ChapterBean chapter = mConfig.getExtras().getParcelable("chapter");
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

        mAnalyzer.beginExecute();

        RawResult<String> webContentBean = getRawContentResult(source, bookContentBean.getDurChapterUrl(), ruleBookContent);
        bookContentBean.setDurChapterContent(webContentBean.result);

        if (!StringUtils.isEmpty(webContentBean.nextUrl)) {
            List<String> nextUrls = new ArrayList<>();
            int retryCount = 0;

            final String nextChapterUrl = chapter.getNextChapterUrl();

            while (!TextUtils.isEmpty(webContentBean.nextUrl) && !nextUrls.contains(webContentBean.nextUrl)) {
                if (webContentBean.nextUrl.equals(nextChapterUrl)) {
                    break;
                }
                Call<String> call = OkHttpHelper.getInstance().createService(mBookSource.getBookSourceUrl(), IHttpGetApi.class)
                        .getWebContentCall(webContentBean.nextUrl, AnalyzeHeaders.getMap(mBookSource));
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
        }

        mAnalyzer.endExecute();
        return bookContentBean;
    }

    @Override
    public String getAudioLink(String source) {
        String ruleBookContent = mBookSource.getRuleBookContent();
        mAnalyzer.beginExecute();
        mAnalyzer.setContent(source);
        String url = mAnalyzer.getResultUrl(ruleBookContent);
        mAnalyzer.endExecute();
        return url;
    }


    private RawResult<String> getRawContentResult(String s, String chapterUrl, String ruleContent) {
        RawResult<String> webContentBean = new RawResult<>();
        try {
            mAnalyzer.setContent(s);
            webContentBean.result = mAnalyzer.getResultContent(ruleContent);
            if (!TextUtils.isEmpty(mBookSource.getRuleContentUrlNext())) {
                webContentBean.nextUrl = mAnalyzer.getResultUrl(mBookSource.getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            webContentBean.result = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + " : " + ex.getMessage();
        }
        return webContentBean;
    }

}
