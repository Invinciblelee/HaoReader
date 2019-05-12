package com.monke.monkeybook.model.analyzeRule;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.help.TextProcessor;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
        final AnalyzeCollection collection = mAnalyzer.setContent(source).getRawCollection(getBookSource().getRealRuleSearchList());
        List<SearchBookBean> books = new ArrayList<>();
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            SearchBookBean item = new SearchBookBean();
            item.putVariableMap(mAnalyzer.getVariableMap(getBookSource().getRulePersistedVariables(), 0));
            item.setTag(getConfig().getTag());
            item.setOrigin(getConfig().getName());
            item.setBookType(getBookSource().getBookSourceType());
            item.setKind(StringUtils.join(",", mAnalyzer.getResultContents(getBookSource().getRuleSearchKind())));
            item.setLastChapter(TextProcessor.formatChapterName(mAnalyzer.getResultContent(getBookSource().getRuleSearchLastChapter())));
            item.setName(TextProcessor.formatBookName(mAnalyzer.getResultContent(getBookSource().getRuleSearchName())));
            item.setAuthor(TextProcessor.formatAuthorName(mAnalyzer.getResultContent(getBookSource().getRuleSearchAuthor())));
            item.setNoteUrl(mAnalyzer.getResultUrl(getBookSource().getRuleSearchNoteUrl()));
            item.setIntroduce(mAnalyzer.getResultContent(getBookSource().getRuleIntroduce()));
            item.setCoverUrl(mAnalyzer.getResultUrl(getBookSource().getRuleSearchCoverUrl()));
            if (isEmpty(item.getNoteUrl())) {
                item.setNoteUrl(getConfig().getBaseURL());
            }
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
        BookInfoBean bookInfoBean = book.getBookInfoBean();

        mAnalyzer.setContent(source);

        book.putVariableMap(mAnalyzer.getVariableMap(getBookSource().getRulePersistedVariables(), 1));

        if (isEmpty(bookInfoBean.getCoverUrl())) {
            bookInfoBean.setCoverUrl(mAnalyzer.getResultUrl(getBookSource().getRuleCoverUrl()));
        }
        if (isEmpty(bookInfoBean.getName())) {
            bookInfoBean.setName(TextProcessor.formatBookName(mAnalyzer.getResultContent(getBookSource().getRuleBookName())));
        }
        if (isEmpty(bookInfoBean.getAuthor())) {
            bookInfoBean.setAuthor(TextProcessor.formatAuthorName(mAnalyzer.getResultContent(getBookSource().getRuleBookAuthor())));
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
            book.setLastChapterName(TextProcessor.formatChapterName(mAnalyzer.getResultContent(getBookSource().getRuleLastChapter())));
        }

        bookInfoBean.setNoteUrl(getConfig().getBaseURL());   //id
        bookInfoBean.setTag(getConfig().getTag());
        bookInfoBean.setOrigin(getConfig().getName());
        bookInfoBean.setBookType(getBookSource().getBookSourceType());
        return book;
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        final String noteUrl = getConfig().getExtras().getString("noteUrl");

        final boolean allInOne = getBookSource().allInOneChapterList();
        final String ruleChapterList = getBookSource().getRealRuleChapterList();
        final Map<String, String> headerMap = AnalyzeHeaders.getMap(getBookSource());


        WebChapterResult webChapter = getRawChaptersResult(source, ruleChapterList, noteUrl, allInOne);
        List<ChapterBean> chapterList = webChapter.result;

        if (webChapter.nextUrls != null) {
            if (webChapter.nextUrls.size() > 1) {
                final List<String> chapterUrls = new ArrayList<>(new HashSet<>(webChapter.nextUrls));
                Collections.sort(chapterUrls, Global.STRING_COMPARATOR);
                for (String nextUrl : chapterUrls) {
                    try {
                        AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), nextUrl, headerMap);
                        String response = SimpleModel.getResponse(analyzeUrl).blockingFirst().body();
                        webChapter = getRawChaptersResult(response, ruleChapterList, noteUrl, allInOne);
                        if (!webChapter.result.isEmpty()) {
                            chapterList.addAll(webChapter.result);
                        }
                    } catch (Exception ignore) {
                    }
                }
            } else if (webChapter.nextUrls.size() == 1) {
                final List<String> usedUrls = new ArrayList<>();
                final String nextUrl = webChapter.nextUrls.get(0);
                usedUrls.add(noteUrl);
                while (!isEmpty(nextUrl) && !usedUrls.contains(nextUrl)) {
                    usedUrls.add(nextUrl);

                    try {
                        AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), nextUrl, headerMap);
                        String response = SimpleModel.getResponse(analyzeUrl).blockingFirst().body();
                        webChapter = getRawChaptersResult(response, ruleChapterList, noteUrl, allInOne);
                        if (!webChapter.result.isEmpty()) {
                            chapterList.addAll(webChapter.result);
                        }
                    } catch (Exception ignore) {
                    }

                }
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


    private WebChapterResult getRawChaptersResult(String s, String ruleChapterList, String noteUrl, boolean allInOne) {
        mAnalyzer.setContent(s);
        WebChapterResult webChapter = new WebChapterResult();
        if (!isEmpty(getBookSource().getRuleChapterUrlNext())) {
            webChapter.nextUrls = mAnalyzer.getResultUrls(getBookSource().getRuleChapterUrlNext());
        }

        final AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        final List<ChapterBean> chapterList = new ArrayList<>();

        ChapterBean chapterBean = null;
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            final String name;
            final String url;
            if (allInOne) {
                name = mAnalyzer.getResultContentInternal(getBookSource().getRuleChapterName());
                url = mAnalyzer.getResultUrlInternal(getBookSource().getRuleContentUrl());   //id
            } else {
                name = mAnalyzer.getResultContent(getBookSource().getRuleChapterName());
                url = mAnalyzer.getResultUrl(getBookSource().getRuleContentUrl());   //id
            }
            if (!isEmpty(url) && !isEmpty(name)) {
                if (chapterBean != null) {
                    chapterBean.setNextChapterUrl(url);
                }
                chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(getConfig().getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterList.add(chapterBean);
            }
        }
        webChapter.result = chapterList;
        return webChapter;
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
            content = mAnalyzer.setContent(source).getResultContent(getBookSource().getRuleBookContent());
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
        final Map<String, String> headerMap = AnalyzeHeaders.getMap(getBookSource());

        WebContentResult webContent = getRawContentResult(source, bookContentBean.getDurChapterUrl(), ruleBookContent);
        bookContentBean.appendDurChapterContent(webContent.result);

        if (!StringUtils.isBlank(webContent.nextUrl)) {
            final List<String> usedUrls = new ArrayList<>();
            final String nextChapterUrl = chapter.getNextChapterUrl();

            while (!TextUtils.isEmpty(webContent.nextUrl) && !usedUrls.contains(webContent.nextUrl)) {
                usedUrls.add(webContent.nextUrl);

                if (webContent.nextUrl.equals(nextChapterUrl)) {
                    break;
                }

                try {
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), webContent.nextUrl, headerMap);
                    String response = SimpleModel.getResponse(analyzeUrl).blockingFirst().body();
                    webContent = getRawContentResult(response, webContent.nextUrl, ruleBookContent);
                    if (!TextUtils.isEmpty(webContent.result)) {
                        bookContentBean.appendDurChapterContent(webContent.result);
                    }
                } catch (Exception ignore) {
                }
            }
        }

        return bookContentBean;
    }

    private WebContentResult getRawContentResult(String s, String chapterUrl, String ruleContent) {
        WebContentResult webContentBean = new WebContentResult();
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
        return mAnalyzer.setContent(source).getResultUrl(ruleBookContent);
    }


    private class WebContentResult {

        String result;

        String nextUrl;

        private WebContentResult() {
        }
    }

    private class WebChapterResult {
        List<ChapterBean> result;

        List<String> nextUrls;

        private WebChapterResult() {
        }
    }
}
