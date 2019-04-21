package com.monke.monkeybook.model.analyzeRule;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class JsonContentDelegate implements ContentDelegate {

    private static final String TAG = JsonContentDelegate.class.getSimpleName();

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
        mAnalyzer.beginExecute();
        mAnalyzer.setContent(source);

        AnalyzeCollection collection = mAnalyzer.getRawCollection(mBookSource.getRealRuleSearchList());
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
            }catch (Exception e){
                Logger.e(TAG, "getSearchBooks", e);
            }
        }
        mAnalyzer.endExecute();

        if(mBookSource.reverseSearchList()){
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

        String chapterListUrl = mAnalyzer.getResultUrl(mBookSource.getRuleChapterUrl());
        if (isEmpty(chapterListUrl)) {
            bookInfoBean.setChapterListUrl(mConfig.getBaseURL());
        } else {
            bookInfoBean.setChapterListUrl(chapterListUrl);
        }

        if (isEmpty(book.getLastChapterName())) {
            book.setLastChapterName(mAnalyzer.getResultContent(mBookSource.getRuleLastChapter()));
        }

        book.putVariableMap(mAnalyzer.getVariableMap(mBookSource.getRulePersistedVariables()));

        bookInfoBean.setBookType(mBookSource.getBookSourceType());
        bookInfoBean.setOrigin(mConfig.getName());
        bookInfoBean.setNoteUrl(mConfig.getBaseURL()); //id
        bookInfoBean.setTag(mConfig.getTag());
        mAnalyzer.endExecute();
        return book;
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        final String ruleChapterList = mBookSource.getRealRuleChapterList();

        String noteUrl = mConfig.getExtras().getString("noteUrl");

        mAnalyzer.beginExecute();
        mAnalyzer.setContent(source);
        AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        List<ChapterBean> chapterList = new ArrayList<>();
        while (collection.hasNext()) {
            String url = collection.mutable().getResultUrl(mBookSource.getRuleContentUrl());   //id
            String name = collection.mutable().getResultContent(mBookSource.getRuleChapterName());
            if (!isEmpty(url) && !isEmpty(name)) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(mConfig.getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterList.add(chapterBean);
            }
        }
        mAnalyzer.endExecute();
        if (!mBookSource.reverseChapterList()) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<ChapterBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        return chapterList;
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
        bookContentBean.setNoteUrl(chapter.getNoteUrl());

        String content;
        mAnalyzer.beginExecute();
        try {
            mAnalyzer.setContent(source);
            content = mAnalyzer.getResultContent(mBookSource.getRuleBookContent());
        } catch (Exception ex) {
            Logger.e(TAG, "getBookContent", ex);
            String chapterUrl = bookContentBean.getDurChapterUrl();
            content = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + " : " + ex.getMessage();
        }
        mAnalyzer.endExecute();

        bookContentBean.setDurChapterContent(content);

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


}
