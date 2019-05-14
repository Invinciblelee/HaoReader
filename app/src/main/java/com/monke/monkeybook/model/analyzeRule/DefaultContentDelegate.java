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
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

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
    public Observable<List<SearchBookBean>> getSearchBooks(String source) {
        return Observable.create(emitter -> {
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
            emitter.onNext(books);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<BookShelfBean> getBook(String source) {
        return Observable.create(emitter -> {
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
            emitter.onNext(book);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<ChapterBean>> getChapters(String source) {
        return Observable.create(emitter -> {
            final String ruleChapterList = getBookSource().getRealRuleChapterList();
            final Map<String, String> headerMap = AnalyzeHeaders.getMap(getBookSource());

            WebChapterResult webChapter = new WebChapterResult();
            toWebChaptersResult(source, ruleChapterList, webChapter, true);
            final List<ChapterBean> chapterList;
            if (webChapter.result != null) {
                chapterList = webChapter.result;
            } else {
                chapterList = new ArrayList<>();
            }

            if (webChapter.nextUrls != null) {
                if (webChapter.nextUrls.size() > 1) {
                    final List<String> chapterUrls = new ArrayList<>(new LinkedHashSet<>(webChapter.nextUrls));
                    chapterUrls.remove(getConfig().getBaseURL());
                    List<WebChapterResult> webChapterResults = getWebChapterResultList(ruleChapterList, headerMap, chapterUrls).blockingFirst();
                    Collections.sort(webChapterResults);
                    for (WebChapterResult webChapterResult : webChapterResults) {
                        if (webChapterResult.result != null) {
                            chapterList.addAll(webChapterResult.result);
                        }
                    }
                    doOnChapterListFinish(chapterList, emitter);
                } else if (webChapter.nextUrls.size() == 1) {
                    final List<String> usedUrls = new ArrayList<>();
                    String nextUrl = webChapter.nextUrls.get(0);
                    usedUrls.add(getConfig().getBaseURL());
                    while (!isEmpty(nextUrl) && !usedUrls.contains(nextUrl)) {
                        usedUrls.add(nextUrl);
                        webChapter = getSingleWebChapterResult(0, nextUrl, ruleChapterList, headerMap, true).blockingFirst();
                        if (webChapter.result != null && !webChapter.result.isEmpty()) {
                            chapterList.addAll(webChapter.result);
                        }
                        nextUrl = webChapter.nextUrls.isEmpty() ? null : webChapter.nextUrls.get(0);
                    }
                    doOnChapterListFinish(chapterList, emitter);
                }
            } else {
                doOnChapterListFinish(chapterList, emitter);
            }
        });
    }

    private Observable<List<WebChapterResult>> getWebChapterResultList(String ruleChapterList, Map<String, String> headerMap, List<String> chapterUrls) {
        final int size = chapterUrls.size();
        final List<WebChapterRequest> webRequests = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            webRequests.add(new WebChapterRequest(i, chapterUrls.get(i)));
        }
        return Observable.fromIterable(webRequests)
                .flatMap(request -> getSingleWebChapterResult(request.id, request.url, ruleChapterList, headerMap, false))
                .toList().toObservable();
    }

    private Observable<WebChapterResult> getSingleWebChapterResult(int index, String nextUrl, String ruleChapterList, Map<String, String> headerMap, boolean readUrls) {
        return Observable.create((ObservableOnSubscribe<AnalyzeUrl>) emitter -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), nextUrl, headerMap);
            emitter.onNext(analyzeUrl);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(analyzeUrl -> SimpleModel.getResponse(analyzeUrl)
                        .flatMap(response -> Observable.create((ObservableOnSubscribe<WebChapterResult>) emitter -> {
                            WebChapterResult result = new WebChapterResult(index);
                            toWebChaptersResult(response.body(), ruleChapterList, result, readUrls);
                            emitter.onNext(result);
                            emitter.onComplete();
                        }).observeOn(Schedulers.io())))
                .retry(2)
                .onErrorReturnItem(new WebChapterResult(index));
    }

    private void doOnChapterListFinish(List<ChapterBean> chapterList, ObservableEmitter<List<ChapterBean>> emitter) {
        if (!getBookSource().chapterListReverse()) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<ChapterBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        emitter.onNext(chapterList);
        emitter.onComplete();
    }

    private synchronized void toWebChaptersResult(String s, String ruleChapterList, WebChapterResult webChapter, boolean readUrls) {
        mAnalyzer.setContent(s);
        if (readUrls && !isEmpty(getBookSource().getRuleChapterUrlNext())) {
            webChapter.nextUrls = mAnalyzer.getResultUrls(getBookSource().getRuleChapterUrlNext());
        }

        final String noteUrl = getConfig().getExtras().getString("noteUrl");

        if (getBookSource().chapterListInRegex()) {
            webChapter.result = getChaptersInRegex(s, ruleChapterList, noteUrl);
        } else if (getBookSource().chapterListInWhole()) {
            webChapter.result = getChaptersInWhole(mAnalyzer.getRawCollection(ruleChapterList), noteUrl);
        } else {
            webChapter.result = getChaptersInDefault(mAnalyzer.getRawCollection(ruleChapterList), noteUrl);
        }
    }

    /**
     * 默认规则解析
     */
    private List<ChapterBean> getChaptersInDefault(AnalyzeCollection collection, String noteUrl) {
        final List<ChapterBean> chapterList = new ArrayList<>();
        ChapterBean chapterBean = null;
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            String name = mAnalyzer.getResultContent(getBookSource().getRuleChapterName());
            String url = mAnalyzer.getResultUrl(getBookSource().getRuleContentUrl());   //id

            ChapterBean chapter = addChapter(chapterList, noteUrl, name, url);
            if (chapter != null) {
                if (chapterBean != null) {
                    chapterBean.setNextChapterUrl(chapter.getDurChapterUrl());
                }
                chapterBean = chapter;
            }
        }
        return chapterList;
    }

    /**
     * all in one 模式
     */
    private List<ChapterBean> getChaptersInWhole(AnalyzeCollection collection, String noteUrl) {
        final List<ChapterBean> chapterList = new ArrayList<>();
        ChapterBean chapterBean = null;
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            String name = mAnalyzer.getResultContentInternal(getBookSource().getRuleChapterName());
            String url = mAnalyzer.getResultUrlInternal(getBookSource().getRuleContentUrl());   //id

            ChapterBean chapter = addChapter(chapterList, noteUrl, name, url);
            if (chapter != null) {
                if (chapterBean != null) {
                    chapterBean.setNextChapterUrl(chapter.getDurChapterUrl());
                }
                chapterBean = chapter;
            }
        }
        return chapterList;
    }

    /**
     * 正则表达式解析
     */
    private List<ChapterBean> getChaptersInRegex(String source, String ruleChapterList, String noteUrl) {
        final List<ChapterBean> chapterList = new ArrayList<>();
        matchRegex(source, noteUrl, ruleChapterList.split("&&"), 0,
                getBookSource().getRuleChapterName(),
                getBookSource().getRuleContentUrl(),
                chapterList);
        return chapterList;
    }

    private void matchRegex(String string, String noteUrl, String[] regex, int index, String nameRule, String urlRule, List<ChapterBean> chapterBeans) {
        Matcher matcher = Pattern.compile(regex[index]).matcher(string);
        if (index + 1 == regex.length) {
            String baseUrl = "";
            int vipGroup = 0, nameGroup = 0, urlGroup = 0;
            // 分离标题正则参数
            Matcher nameMatcher = Pattern.compile("((?<=\\$)\\d)?\\$(\\d$)").matcher(nameRule);
            while (nameMatcher.find()) {
                String sVipGroup = nameMatcher.group(1);
                vipGroup = sVipGroup == null ? 0 : Integer.parseInt(sVipGroup);
                nameGroup = Integer.parseInt(nameMatcher.group(2));
            }
            // 分离网址正则参数
            Matcher urlMatcher = Pattern.compile("(.*?)\\$(\\d$)").matcher(urlRule);
            while (urlMatcher.find()) {
                baseUrl = VariablesPattern.fromGetterRule(urlMatcher.group(1), getConfig().getVariableStore()).rule;
                urlGroup = Integer.parseInt(urlMatcher.group(2));
            }
            // 提取目录信息
            if (vipGroup == 0) {
                while (matcher.find()) {
                    addChapter(chapterBeans, noteUrl,
                            matcher.group(nameGroup),
                            baseUrl + matcher.group(urlGroup));
                }
            } else {
                while (matcher.find()) {
                    addChapter(chapterBeans, noteUrl,
                            (matcher.group(vipGroup) == null ? "" : "\uD83D\uDD12") + matcher.group(nameGroup),
                            baseUrl + matcher.group(urlGroup));
                }
            }
        } else {
            StringBuilder result = new StringBuilder();
            while (matcher.find()) result.append(matcher.group());
            matchRegex(result.toString(), noteUrl, regex, ++index, nameRule, urlRule, chapterBeans);
        }
    }

    private ChapterBean addChapter(List<ChapterBean> chapterBeans, String noteUrl, String chapterName, String chapterUrl) {
        if (!TextUtils.isEmpty(chapterName)) {
            ChapterBean chapterBean = new ChapterBean(noteUrl, chapterName, StringUtils.checkNull(chapterUrl, noteUrl));
            chapterBeans.add(chapterBean);
            return chapterBean;
        }
        return null;
    }

    @Override
    public Observable<BookContentBean> getContent(String source) {
        return Observable.create(emitter -> {
            ChapterBean chapter = getConfig().getExtras().getParcelable("chapter");
            assert chapter != null;
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterName(chapter.getDurChapterName());
            bookContentBean.setDurChapterIndex(chapter.getDurChapterIndex());
            bookContentBean.setDurChapterUrl(chapter.getDurChapterUrl());
            bookContentBean.setNoteUrl(chapter.getNoteUrl());

            final String ruleBookContent = getBookSource().getRealRuleBookContent();
            final Map<String, String> headerMap = AnalyzeHeaders.getMap(getBookSource());

            WebContentResult webContent = getRawContentResult(source, bookContentBean.getDurChapterUrl(), ruleBookContent);
            bookContentBean.appendDurChapterContent(webContent.result);

            if (webContent.nextUrl != null) {
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
            emitter.onNext(bookContentBean);
            emitter.onComplete();
        });
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
    public Observable<String> getAudioLink(String source) {
        return Observable.create(emitter -> {
            final String ruleBookContent = getBookSource().getRealRuleBookContent();
            emitter.onNext(mAnalyzer.setContent(source).getResultUrl(ruleBookContent));
            emitter.onComplete();
        });
    }


    private class WebContentResult {

        String result;

        String nextUrl;

        private WebContentResult() {
        }
    }

    private class WebChapterResult implements Comparable<WebChapterResult> {
        int id;

        List<ChapterBean> result;

        List<String> nextUrls;

        private WebChapterResult() {
        }

        private WebChapterResult(int id) {
            this.id = id;
        }

        @Override
        public int compareTo(WebChapterResult o) {
            return Integer.compare(id, o.id);
        }
    }

    private class WebChapterRequest {
        int id;
        String url;

        private WebChapterRequest(int id, String url) {
            this.id = id;
            this.url = url;
        }
    }
}
