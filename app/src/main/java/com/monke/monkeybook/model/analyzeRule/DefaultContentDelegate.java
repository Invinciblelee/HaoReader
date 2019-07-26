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
import com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal;
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

    private OutAnalyzer<?> mAnalyzer;

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
            final String ruleSearchList = getBookSource().getRealRuleSearchList();
            final List<SearchBookBean> books;

            if (getBookSource().searchListInRegex()) {
                books = getSearchListInRegex(source, ruleSearchList);
            } else if (getBookSource().searchListInWhole()) {
                books = getSearchListInWhole(mAnalyzer.setContent(source).getRawCollection(ruleSearchList));
            } else {
                books = getSearchListInDefault(mAnalyzer.setContent(source).getRawCollection(ruleSearchList));
            }

            if (getBookSource().searchListReverse()) {
                Collections.reverse(books);
            }
            emitter.onNext(books);
            emitter.onComplete();
        });
    }

    private List<SearchBookBean> getSearchListInWhole(AnalyzeCollection collection) {
        final List<SearchBookBean> searchBookBeans = new ArrayList<>();
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            Map<String, String> variableMap = mAnalyzer.putVariableMapDirectly(getBookSource().getRulePersistedVariables(), 0);
            String name = TextProcessor.formatBookName(mAnalyzer.getTextDirectly(getBookSource().getRuleSearchName()));
            String author = TextProcessor.formatAuthorName(mAnalyzer.getTextDirectly(getBookSource().getRuleSearchAuthor()));
            String kind = StringUtils.join(",", mAnalyzer.getTextDirectly(getBookSource().getRuleSearchKind()));
            String lastChapter = mAnalyzer.getTextDirectly(getBookSource().getRuleSearchLastChapter());
            String introduce = mAnalyzer.getTextDirectly(getBookSource().getRuleSearchIntroduce());
            String coverUrl = mAnalyzer.getRawUrlDirectly(getBookSource().getRuleSearchCoverUrl());
            String noteUrl = mAnalyzer.getRawUrlDirectly(getBookSource().getRuleSearchNoteUrl());
            addSearchBook(searchBookBeans, name, author, kind, lastChapter, introduce, coverUrl, noteUrl, variableMap);
        }
        return searchBookBeans;
    }

    private List<SearchBookBean> getSearchListInRegex(String source, String ruleSearchList) {
        final List<SearchBookBean> searchBookBeans = new ArrayList<>();
        matchSearchListRegex(searchBookBeans, source, ruleSearchList.split("&&"), 0);
        return searchBookBeans;
    }

    private List<SearchBookBean> getSearchListInDefault(AnalyzeCollection collection) {
        final List<SearchBookBean> searchBookBeans = new ArrayList<>();
        while (collection.hasNext()) {
            mAnalyzer.setContent(collection.next());
            Map<String, String> variableMap = mAnalyzer.putVariableMap(getBookSource().getRulePersistedVariables(), 0);
            String name = TextProcessor.formatBookName(mAnalyzer.getText(getBookSource().getRuleSearchName()));
            String author = TextProcessor.formatAuthorName(mAnalyzer.getText(getBookSource().getRuleSearchAuthor()));
            String kind = StringUtils.join(",", mAnalyzer.getTextList(getBookSource().getRuleSearchKind()));
            String lastChapter = mAnalyzer.getText(getBookSource().getRuleSearchLastChapter());
            String introduce = mAnalyzer.getText(getBookSource().getRuleSearchIntroduce());
            String coverUrl = mAnalyzer.getRawUrl(getBookSource().getRuleSearchCoverUrl());
            String noteUrl = mAnalyzer.getRawUrl(getBookSource().getRuleSearchNoteUrl());
            addSearchBook(searchBookBeans, name, author, kind, lastChapter, introduce, coverUrl, noteUrl, variableMap);
        }
        return searchBookBeans;
    }

    private void matchSearchListRegex(List<SearchBookBean> searchBooks, String res, String[] regs, int index) {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 获取规则列表
            String[] ruleList = new String[]{
                    getBookSource().getRuleSearchName(),       // 获取书名规则
                    getBookSource().getRuleSearchAuthor(),     // 获取作者规则
                    getBookSource().getRuleSearchKind(),       // 获取分类规则
                    getBookSource().getRuleSearchLastChapter(),// 获取终章规则
                    getBookSource().getRuleSearchIntroduce(),  // 获取简介规则
                    getBookSource().getRuleSearchCoverUrl(),   // 获取封面规则
                    getBookSource().getRuleSearchNoteUrl()     // 获取详情规则
            };
            // 创建拆分规则容器
            List<String[]> ruleGroups = new ArrayList<>();
            // 提取规则信息
            for (String rule : ruleList) {
                ruleGroups.add(splitRegexRule(rule));
            }
            // 提取书籍列表信息
            while (resM.find()) {
                // 获取列表规则分组数
                int resCount = resM.groupCount();
                // 新建规则结果容器
                String[] infoList = new String[ruleList.length];
                // 合并规则结果内容
                for (int i = 0; i < infoList.length; i++) {
                    StringBuilder infoVal = new StringBuilder();
                    for (String ruleGroup : ruleGroups.get(i)) {
                        if (ruleGroup.startsWith("$")) {
                            int groupIndex = StringUtils.parseInt(ruleGroup);
                            if (groupIndex <= resCount) {
                                infoVal.append(StringUtils.trim(resM.group(groupIndex)));
                                continue;
                            }
                        }
                        infoVal.append(ruleGroup);
                    }
                    infoList[i] = infoVal.toString();
                }
                // 保存当前节点的书籍信息
                addSearchBook(searchBooks, infoList[0], infoList[1], infoList[2], infoList[3], infoList[4], infoList[5], infoList[6], getConfig().getVariableStore().getVariableMap());
            }
        } else {
            StringBuilder result = new StringBuilder();
            while (resM.find()) result.append(resM.group());
            matchSearchListRegex(searchBooks, result.toString(), regs, ++index);
        }
    }

    private static String[] splitRegexRule(String str) {
        int start = 0, index = 0, len = str.length();
        List<String> list = new ArrayList<>();
        while (start < len) {
            if ((str.charAt(start) == '$') && (str.charAt(start + 1) >= '0') && (str.charAt(start + 1) <= '9')) {
                if (start > index) list.add(str.substring(index, start));
                if ((start + 2 < len) && (str.charAt(start + 2) >= '0') && (str.charAt(start + 2) <= '9')) {
                    list.add(str.substring(start, start + 3));
                    index = start += 3;
                } else {
                    list.add(str.substring(start, start + 2));
                    index = start += 2;
                }
            } else {
                ++start;
            }
        }
        if (start > index) list.add(str.substring(index, start));
        return list.toArray(new String[0]);
    }

    private void addSearchBook(List<SearchBookBean> searchBookBeans, String name, String author, String kind, String lastChapter, String introduce, String coverUrl, String noteUrl, Map<String, String> variableMap) {
        if (StringUtils.isBlank(name)) return;
        SearchBookBean item = new SearchBookBean();
        item.setTag(getConfig().getTag());
        item.setOrigin(getConfig().getName());
        item.setBookType(getBookSource().getBookSourceType());
        item.setName(name);
        item.setAuthor(author);
        item.setIntroduce(introduce);
        item.setKind(kind);
        item.setLastChapter(lastChapter);
        item.setCoverUrl(mAnalyzer.processUrl(coverUrl));
        item.putVariableMap(variableMap);
        if (StringUtils.isBlank(noteUrl)) {
            item.setNoteUrl(getConfig().getBaseURL());
        } else {
            item.setNoteUrl(mAnalyzer.processUrl(noteUrl));
        }
        searchBookBeans.add(item);
    }

    @Override
    public Observable<BookShelfBean> getBook(String source) {
        return Observable.create(emitter -> {
            BookShelfBean book = (BookShelfBean) getConfig().getVariableStore();
            BookInfoBean bookInfoBean = book.getBookInfoBean();

            mAnalyzer.setContent(source);

            book.putVariableMap(mAnalyzer.putVariableMap(getBookSource().getRulePersistedVariables(), 1));

            if (isEmpty(bookInfoBean.getCoverUrl())) {
                bookInfoBean.setCoverUrl(mAnalyzer.getAbsUrl(getBookSource().getRuleCoverUrl()));
            }
            if (isEmpty(bookInfoBean.getName())) {
                bookInfoBean.setName(TextProcessor.formatBookName(mAnalyzer.getText(getBookSource().getRuleBookName())));
            }
            if (isEmpty(bookInfoBean.getAuthor())) {
                bookInfoBean.setAuthor(TextProcessor.formatAuthorName(mAnalyzer.getText(getBookSource().getRuleBookAuthor())));
            }

            if (isEmpty(bookInfoBean.getIntroduce())) {
                bookInfoBean.setIntroduce(mAnalyzer.getText(getBookSource().getRuleIntroduce()));
            }


            if (isEmpty(book.getLastChapterName())) {
                book.setLastChapterName(mAnalyzer.getText(getBookSource().getRuleBookLastChapter()));
            }

            String chapterUrl = mAnalyzer.getAbsUrl(getBookSource().getRuleChapterUrl());
            if (isEmpty(chapterUrl)) {
                bookInfoBean.setChapterListUrl(getConfig().getBaseURL());
            } else {
                bookInfoBean.setChapterListUrl(chapterUrl);
            }

            bookInfoBean.setNoteUrl(getConfig().getBaseURL());   //id
            bookInfoBean.setTag(getConfig().getTag());
            bookInfoBean.setOrigin(getConfig().getName());
            bookInfoBean.setBookType(getBookSource().getBookSourceType());
            book.setNoteUrl(bookInfoBean.getNoteUrl());
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
                } else if (webChapter.nextUrls.size() == 1) {
                    final List<String> usedUrls = new ArrayList<>();
                    usedUrls.add(getConfig().getBaseURL());
                    String nextUrl = webChapter.nextUrls.get(0);
                    while (!isEmpty(nextUrl) && !usedUrls.contains(nextUrl)) {
                        usedUrls.add(nextUrl);
                        webChapter = getSingleWebChapterResult(0, nextUrl, ruleChapterList, headerMap, true).blockingFirst();
                        if (webChapter.result != null && !webChapter.result.isEmpty()) {
                            chapterList.addAll(webChapter.result);
                        }
                        nextUrl = (webChapter.nextUrls == null || webChapter.nextUrls.isEmpty()) ? null : webChapter.nextUrls.get(0);
                    }
                }
                doOnChapterListFinish(chapterList, emitter);
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
            webChapter.nextUrls = mAnalyzer.getRawUrlList(getBookSource().getRuleChapterUrlNext());
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
            String name = mAnalyzer.getText(getBookSource().getRuleChapterName());
            String url = mAnalyzer.getRawUrl(getBookSource().getRuleContentUrl());   //id

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
            String name = mAnalyzer.getTextDirectly(getBookSource().getRuleChapterName());
            String url = mAnalyzer.getRawUrlDirectly(getBookSource().getRuleContentUrl());   //id

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
        matchChaptersRegex(source, noteUrl, ruleChapterList.split("&&"), 0,
                getBookSource().getRuleChapterName(),
                getBookSource().getRuleContentUrl(),
                chapterList);
        return chapterList;
    }

    /**
     * 匹配正则表达式
     */
    private void matchChaptersRegex(String string, String noteUrl, String[] regex, int index, String nameRule, String urlRule, List<ChapterBean> chapterBeans) {
        Matcher matcher = Pattern.compile(regex[index]).matcher(string);
        if (index + 1 == regex.length) {
            String baseUrl = AnalyzeGlobal.EMPTY;
            int nameGroup = 0, urlGroup = 0;
            // 分离标题正则参数
            Matcher nameMatcher = Pattern.compile("\\$(\\d$)").matcher(nameRule);
            if (nameMatcher.find()) {
                nameGroup = StringUtils.parseInt(nameMatcher.group(1));
            }
            // 分离网址正则参数
            Matcher urlMatcher = Pattern.compile("(.*?)\\$(\\d$)").matcher(urlRule);
            while (urlMatcher.find()) {
                baseUrl = VariablesPattern.fromGetterRule(urlMatcher.group(1), getConfig().getVariableStore()).rule;
                urlGroup = StringUtils.parseInt(urlMatcher.group(2));
            }
            // 提取目录信息
            while (matcher.find()) {
                addChapter(chapterBeans, noteUrl,
                        matcher.group(nameGroup),
                        baseUrl + matcher.group(urlGroup));
            }
        } else {
            StringBuilder result = new StringBuilder();
            while (matcher.find()) result.append(matcher.group());
            matchChaptersRegex(result.toString(), noteUrl, regex, ++index, nameRule, urlRule, chapterBeans);
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
    public Observable<BookContentBean> getBookContent(String source) {
        return Observable.create(emitter -> {
            final ChapterBean chapter = getConfig().getExtras().getParcelable("chapter");
            if (chapter == null) {
                emitter.onError(new NullPointerException("getBookContent can not with a null chapter"));
                return;
            }

            final BookContentBean bookContentBean = new BookContentBean();
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

                while (!isEmpty(webContent.nextUrl) && !usedUrls.contains(webContent.nextUrl)) {
                    if (webContent.nextUrl.equals(nextChapterUrl)) {
                        break;
                    }
                    usedUrls.add(webContent.nextUrl);

                    try {
                        AnalyzeUrl analyzeUrl = new AnalyzeUrl(getConfig().getBaseURL(), webContent.nextUrl, headerMap);
                        String response = SimpleModel.getResponse(analyzeUrl).subscribeOn(Schedulers.io()).blockingFirst().body();
                        webContent = getRawContentResult(response, webContent.nextUrl, ruleBookContent);
                        if (!isEmpty(webContent.result)) {
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
            webContentBean.result = mAnalyzer.getText(ruleContent);
            if (!TextUtils.isEmpty(getBookSource().getRuleContentUrlNext())) {
                webContentBean.nextUrl = mAnalyzer.getRawUrl(getBookSource().getRuleContentUrlNext());
            }
        } catch (Exception ex) {
            Logger.e(TAG, "getBookContent", ex);
            webContentBean.result = chapterUrl.substring(0, chapterUrl.indexOf('/', 8)) + " : " + ex.getMessage();
        }
        return webContentBean;
    }

    @Override
    public Observable<String> getAudioContent(String source) {
        return Observable.create(emitter -> {
            final String ruleBookContent = getBookSource().getRealRuleBookContent();
            emitter.onNext(mAnalyzer.setContent(source).getAbsUrl(ruleBookContent));
            emitter.onComplete();
        });
    }


    private static class WebContentResult {

        String result;

        String nextUrl;

        private WebContentResult() {
        }
    }

    private static class WebChapterResult implements Comparable<WebChapterResult> {
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

    private static class WebChapterRequest {
        int id;
        String url;

        private WebChapterRequest(int id, String url) {
            this.id = id;
            this.url = url;
        }
    }
}
