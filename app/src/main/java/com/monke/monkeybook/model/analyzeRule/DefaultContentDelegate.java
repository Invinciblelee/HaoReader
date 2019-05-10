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
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.utils.StringUtils;

import org.mozilla.javascript.NativeObject;

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
        final AnalyzeCollection collection = mAnalyzer.setContent(source).getRawCollection(getBookSource().getRealRuleSearchList());
        List<SearchBookBean> books = new ArrayList<>();
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            SearchBookBean item = new SearchBookBean();
            item.putVariableMap(mAnalyzer.getVariableMap(getBookSource().getRulePersistedVariables(), 0));
            item.setTag(getConfig().getTag());
            item.setOrigin(getConfig().getName());
            item.setBookType(getBookSource().getBookSourceType());
            item.setAuthor(FormatWebText.getAuthor(mAnalyzer.getResultContent(getBookSource().getRuleSearchAuthor())));
            item.setKind(StringUtils.join(",", mAnalyzer.getResultContents(getBookSource().getRuleSearchKind())));
            item.setLastChapter(FormatWebText.trim(mAnalyzer.getResultContent(getBookSource().getRuleSearchLastChapter())));
            item.setName(FormatWebText.getBookName(mAnalyzer.getResultContent(getBookSource().getRuleSearchName())));
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

        bookInfoBean.setNoteUrl(getConfig().getBaseURL());   //id
        bookInfoBean.setTag(getConfig().getTag());
        bookInfoBean.setOrigin(getConfig().getName());
        bookInfoBean.setBookType(getBookSource().getBookSourceType());
        return book;
    }

    @Override
    public List<ChapterBean> getChapters(String source) {
        final String noteUrl = getConfig().getExtras().getString("noteUrl");

        final String ruleChapterList = getBookSource().getRealRuleChapterList();

        RawResult<List<ChapterBean>> webChapterBean = getRawChaptersResult(source, ruleChapterList, noteUrl);
        List<ChapterBean> chapterList = webChapterBean.result;

        List<String> nextUrls = new ArrayList<>();
        int retryCount = 0;
        while (!isEmpty(webChapterBean.nextUrl) && !nextUrls.contains(webChapterBean.nextUrl)) {
            String response = "";
            try {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getTag(), webChapterBean.nextUrl, AnalyzeHeaders.getMap(getBookSource()));
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
        if (!isEmpty(getBookSource().getRuleChapterUrlNext())) {
            webChapterBean.nextUrl = mAnalyzer.getResultUrl(getBookSource().getRuleChapterUrlNext());
        }
        final AnalyzeCollection collection = mAnalyzer.getRawCollection(ruleChapterList);
        final List<ChapterBean> chapterList = new ArrayList<>();
        while (collection.hasNext()) {
            final Object content = collection.next();
            final String name;
            final String url;
            if (content instanceof NativeObject) {
                NativeObject object = (NativeObject) content;
                String nameKey = StringUtils.checkBlank(getBookSource().getRuleChapterName(), "name");
                String urlKey = StringUtils.checkBlank(getBookSource().getRuleContentUrl(), "url");
                name = StringUtils.valueOf(object.get(nameKey));
                url = StringUtils.valueOf(object.get(urlKey));
            } else {
                mAnalyzer.setContent(content);
                name = mAnalyzer.getResultContent(getBookSource().getRuleChapterName());
                url = mAnalyzer.getResultUrl(getBookSource().getRuleContentUrl());   //id
            }
            if (!isEmpty(url) && !isEmpty(name)) {
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterUrl(url);
                chapterBean.setDurChapterName(name);
                chapterBean.setTag(getConfig().getTag());
                chapterBean.setNoteUrl(noteUrl);
                chapterList.add(chapterBean);
            }
        }

        webChapterBean.result = chapterList;
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
        return mAnalyzer.setContent(source).getResultUrl(ruleBookContent);
    }

}
