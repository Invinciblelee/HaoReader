package com.monke.monkeybook.model.analyzeRule;

import android.support.annotation.NonNull;

import com.jayway.jsonpath.ReadContext;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class JsonContentDelegate implements ContentDelegate {

    private JsonAnalyzer mAnalyzer;
    private AnalyzeConfig mConfig;
    private BookSourceBean mBookSource;

    JsonContentDelegate(@NonNull JsonAnalyzer analyzer) {
        this.mAnalyzer = analyzer;
        this.mConfig = mAnalyzer.getConfig();
        this.mBookSource = mConfig.getBookSource();
    }

    @Override
    public List<SearchBookBean> getSearchBooks(String source) {
        List<Object> booksE = mAnalyzer.getRawList(source, mBookSource.getRuleSearchList());
        List<SearchBookBean> books = new ArrayList<>();
        if (null != booksE && booksE.size() > 0) {
            for (Object obj : booksE) {
                ReadContext element = mAnalyzer.parseSource(obj);
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
        }
        return books;
    }

    @Override
    public BookShelfBean getBook(String source) {
        ReadContext element = mAnalyzer.parseSource(source);
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
        List<ChapterListBean> chapterBeans = new ArrayList<>();
        List<Object> chapters = mAnalyzer.getRawList(source, ruleChapterList);
        for (int i = 0, size = chapters.size(); i < size; i++) {
            ReadContext element = mAnalyzer.parseSource(chapters.get(i));
            ChapterListBean temp = new ChapterListBean();
            temp.setTag(mConfig.getTag());
            temp.setDurChapterUrl(mAnalyzer.getResultUrl(element, mBookSource.getRuleContentUrl()));   //id
            temp.setDurChapterName(mAnalyzer.getResultContent(element, mBookSource.getRuleChapterName()));
            temp.setDurChapterIndex(i);
            chapterBeans.add(temp);
        }
        if (reverse) {
            Collections.reverse(chapterBeans);
        }
        return chapterBeans;
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

        String content;
        try {
            ReadContext element = mAnalyzer.parseSource(source);
            content = mAnalyzer.getResultContent(element, mBookSource.getRuleBookContent());
        } catch (Exception ex) {
            String chapterUrl = bookContentBean.getDurChapterUrl();
            content = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + ex.getMessage();
        }

        bookContentBean.setDurChapterContent(content);

        return bookContentBean;
    }


}
