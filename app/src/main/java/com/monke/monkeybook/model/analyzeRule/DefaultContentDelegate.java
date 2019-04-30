package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;

import static android.text.TextUtils.isEmpty;

class DefaultContentDelegate implements ContentDelegate {

    private static final String TAG = DefaultContentDelegate.class.getSimpleName();

    private final OutAnalyzer<?> mAnalyzer;

    DefaultContentDelegate(@NonNull OutAnalyzer<?> mAnalyzer) {
        this.mAnalyzer = mAnalyzer;
    }

    private AnalyzeConfig getConfig() {
        return mAnalyzer.getConfig();
    }

    private BookSourceBean getBookSource() {
        return getConfig().getBookSource();
    }

    @Override
    public List<SearchBookBean> getSearchBooks(String source) {
        mAnalyzer.setContent(source);

        final AnalyzeCollection collection = mAnalyzer.getRawCollection(getBookSource().getRealRuleSearchList());
        List<SearchBookBean> books = new ArrayList<>();
        while (collection.hasNext()) {
            SearchBookBean item = new SearchBookBean();
            item.setTag(getConfig().getTag());
            item.setOrigin(getConfig().getName());
            item.setBookType(getBookSource().getBookSourceType());
            item.setAuthor(FormatWebText.getAuthor(collection.mutable().getResultContent(getBookSource().getRuleSearchAuthor())));
            item.setKind(StringUtils.join(",", collection.mutable().getResultContents(getBookSource().getRuleSearchKind())));
            item.setLastChapter(FormatWebText.trim(collection.mutable().getResultContent(getBookSource().getRuleSearchLastChapter())));
            item.setName(FormatWebText.getBookName(collection.mutable().getResultContent(getBookSource().getRuleSearchName())));
            item.setNoteUrl(collection.mutable().getResultUrl(getBookSource().getRuleSearchNoteUrl()));
            item.setIntroduce(collection.mutable().getResultContent(getBookSource().getRuleIntroduce()));
            item.putVariableMap(collection.mutable().getVariableMap(getBookSource().getRulePersistedVariables()));
            if (isEmpty(item.getNoteUrl())) {
                item.setNoteUrl(getConfig().getBaseURL());
            }
            item.setCoverUrl(collection.mutable().getResultUrl(getBookSource().getRuleSearchCoverUrl()));
            if (!isEmpty(item.getName())) {
                books.add(item);
            }
        }

        if (getBookSource().reverseSearchList()) {
            Collections.reverse(books);
        }

        return books;
    }

    @Override
    public BookShelfBean getBook(String source) {
        BookShelfBean book = (BookShelfBean) getConfig().getVariableStore();
        assert book != null;
        BookInfoBean bookInfoBean = book.getBookInfoBean();
        if (bookInfoBean == null) {
            bookInfoBean = new BookInfoBean();
        }

        mAnalyzer.setContent(source);

        if (isEmpty(bookInfoBean.getCoverUrl())) {
            bookInfoBean.setCoverUrl(mAnalyzer.getResultUrl(getBookSource().getRuleCoverUrl()));
        }
        if (isEmpty(bookInfoBean.getName())) {
            bookInfoBean.setName(mAnalyzer.getResultContent(getBookSource().getRuleBookName()));
        }
        if (isEmpty(bookInfoBean.getAuthor())) {
            bookInfoBean.setAuthor(FormatWebText.getAuthor(mAnalyzer.getResultContent(getBookSource().getRuleBookAuthor())));
        }

        if (isEmpty(bookInfoBean.getIntroduce())) {
            bookInfoBean.setIntroduce(mAnalyzer.getResultContent(getBookSource().getRuleIntroduce()));
        }

        String chapterUrl = mAnalyzer.getResultUrl(getBookSource().getRuleChapterUrl());
        if (isEmpty(chapterUrl)) {
            bookInfoBean.setChapterListUrl(getConfig().getBaseURL());
        } else {
            bookInfoBean.setChapterListUrl(chapterUrl);
        }

        if (isEmpty(book.getLastChapterName())) {
            book.setLastChapterName(mAnalyzer.getResultContent(getBookSource().getRuleLastChapter()));
        }

        book.putVariableMap(mAnalyzer.getVariableMap(getBookSource().getRulePersistedVariables()));

        bookInfoBean.setNoteUrl(getConfig().getBaseURL());   //id
        bookInfoBean.setTag(getConfig().getTag());
        bookInfoBean.setOrigin(getConfig().getName());
        bookInfoBean.setBookType(getBookSource().getBookSourceType());
        return book;
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        if (mAnalyzer instanceof JsonAnalyzer) {
            return getChaptersFromJson(source);
        } else {
            return getChaptersFromXJsoup(source);
        }
    }

    private List<ChapterBean> getChaptersFromJson(String source) {
        final String ruleChapterList = getBookSource().getRealRuleChapterList();

        String noteUrl = getConfig().getExtras().getString("noteUrl");

        mAnalyzer.setContent(source);
        AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        List<ChapterBean> chapterList = new ArrayList<>();
        while (collection.hasNext()) {
            String url = collection.mutable().getResultUrl(getBookSource().getRuleContentUrl());   //id
            String name = collection.mutable().getResultContent(getBookSource().getRuleChapterName());
            if (!isEmpty(url) && !isEmpty(name)) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(getConfig().getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterList.add(chapterBean);
            }
        }
        if (!getBookSource().reverseChapterList()) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<ChapterBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        return chapterList;
    }

    private List<ChapterBean> getChaptersFromXJsoup(String source) {
        final String noteUrl = getConfig().getExtras().getString("noteUrl");

        final String ruleChapterList = getBookSource().getRealRuleChapterList();

        RawResult<List<ChapterBean>> webChapterBean = getRawChaptersResult(source, ruleChapterList, noteUrl);
        List<ChapterBean> chapterList = webChapterBean.result;

        List<String> nextUrls = new ArrayList<>();
        int retryCount = 0;
        while (!isEmpty(webChapterBean.nextUrl) && !nextUrls.contains(webChapterBean.nextUrl)) {
            String response = "";
            try {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(webChapterBean.nextUrl, getConfig().getHeaderMap(), getConfig().getTag());
                response = SimpleModel.getResponse(analyzeUrl).blockingFirst().body();
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
        if (!getBookSource().reverseChapterList()) {
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
        if (!isEmpty(getBookSource().getRuleChapterUrlNext())) {
            webChapterBean.nextUrl = mAnalyzer.getResultUrl(getBookSource().getRuleChapterUrlNext());
        }
        final AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        while (collection.hasNext()) {
            String url = collection.mutable().getResultUrl(getBookSource().getRuleContentUrl());   //id
            String name = collection.mutable().getResultContent(getBookSource().getRuleChapterName());
            if (!isEmpty(url) && !isEmpty(name)) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(getConfig().getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterBeans.add(chapterBean);
            }
        }

        webChapterBean.result = chapterBeans;
        return webChapterBean;
    }

    @Override
    public BookContentBean getContent(String source) {
        if (mAnalyzer instanceof JsonAnalyzer) {
            return getContentFromJson(source);
        } else {
            return getContentFromXJsoup(source);
        }
    }

    private BookContentBean getContentFromJson(String source) {
        ChapterBean chapter = getConfig().getExtras().getParcelable("chapter");
        assert chapter != null;
        BookContentBean bookContentBean = new BookContentBean();
        bookContentBean.setDurChapterName(chapter.getDurChapterName());
        bookContentBean.setDurChapterIndex(chapter.getDurChapterIndex());
        bookContentBean.setDurChapterUrl(chapter.getDurChapterUrl());
        bookContentBean.setTag(chapter.getTag());
        bookContentBean.setNoteUrl(chapter.getNoteUrl());

        String content;
        try {
            mAnalyzer.setContent(source);
            content = mAnalyzer.getResultContent(getBookSource().getRuleBookContent());
        } catch (Exception ex) {
            Logger.e(TAG, "getBookContent", ex);
            String chapterUrl = bookContentBean.getDurChapterUrl();
            content = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + " : " + ex.getMessage();
        }
        bookContentBean.setDurChapterContent(content);

        return bookContentBean;
    }

    private BookContentBean getContentFromXJsoup(String source) {
        ChapterBean chapter = getConfig().getExtras().getParcelable("chapter");
        assert chapter != null;
        BookContentBean bookContentBean = new BookContentBean();
        bookContentBean.setDurChapterName(chapter.getDurChapterName());
        bookContentBean.setDurChapterIndex(chapter.getDurChapterIndex());
        bookContentBean.setDurChapterUrl(chapter.getDurChapterUrl());
        bookContentBean.setTag(chapter.getTag());

        final String ruleBookContent = getBookSource().getRealRuleBookContent();

        RawResult<String> webContentBean = getRawContentResult(source, bookContentBean.getDurChapterUrl(), ruleBookContent);
        bookContentBean.setDurChapterContent(webContentBean.result);

        if (!StringUtils.isBlank(webContentBean.nextUrl)) {
            List<String> nextUrls = new ArrayList<>();
            int retryCount = 0;

            final String nextChapterUrl = chapter.getNextChapterUrl();

            while (!TextUtils.isEmpty(webContentBean.nextUrl) && !nextUrls.contains(webContentBean.nextUrl)) {
                if (webContentBean.nextUrl.equals(nextChapterUrl)) {
                    break;
                }
                Call<String> call = OkHttpHelper.getInstance().createService(getBookSource().getBookSourceUrl(), IHttpGetApi.class)
                        .getWebContentCall(webContentBean.nextUrl, AnalyzeHeaders.getMap(getBookSource()));
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

        return bookContentBean;
    }

    private RawResult<String> getRawContentResult(String s, String chapterUrl, String ruleContent) {
        RawResult<String> webContentBean = new RawResult<>();
        try {
            mAnalyzer.setContent(s);
            webContentBean.result = mAnalyzer.getResultContent(ruleContent);
            if (!TextUtils.isEmpty(getBookSource().getRuleContentUrlNext())) {
                webContentBean.nextUrl = mAnalyzer.getResultUrl(getBookSource().getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            Logger.e(TAG, "getBookContent", ex);
            webContentBean.result = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + " : " + ex.getMessage();
        }
        return webContentBean;
    }

    @Override
    public String getAudioLink(String source) {
        final String ruleBookContent = getBookSource().getRealRuleBookContent();
        mAnalyzer.setContent(source);
        return mAnalyzer.getResultUrl(ruleBookContent);
    }

}
