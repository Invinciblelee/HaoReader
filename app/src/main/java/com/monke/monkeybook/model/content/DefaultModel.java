package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.basemvplib.AjaxWebView;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.ContextHolder;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.impl.IAudioBookChapterModel;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

/**
 * 默认检索规则
 */
public class DefaultModel extends BaseModelImpl implements IStationBookModel, IAudioBookChapterModel {

    private static final String TAG = DefaultModel.class.getSimpleName();

    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap;

    private DefaultModel(String tag) {
        this.tag = tag;
        bookSourceBean = BookSourceManager.getByUrl(tag);
        if (bookSourceBean != null) {
            name = bookSourceBean.getBookSourceName();
            headerMap = AnalyzeHeaders.getMap(bookSourceBean);
        }
    }

    public static DefaultModel newInstance(String tag) {
        return new DefaultModel(tag);
    }

    private Map<String, String> headerMap(boolean withCookie) {
        if (headerMap == null) {
            return null;
        }

        final Map<String, String> map = new HashMap<>(headerMap);
        if (!withCookie) {
            map.remove("Cookie");
        }
        return map;
    }

    /**
     * 发现
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        return Observable.create((ObservableOnSubscribe<BookList>) emitter -> {
            if (bookSourceBean == null || isEmpty(url)) {
                emitter.onError(new IllegalArgumentException("find Book failed for: " + url));
            } else {
                emitter.onNext(new BookList(tag, name, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(bookList -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, url, page, headerMap(false));
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()));
        }).doOnError(throwable -> Logger.e(TAG, "findBook", throwable))
                .onErrorReturnItem(new ArrayList<>());

    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return Observable.create((ObservableOnSubscribe<BookList>) emitter -> {
            if (bookSourceBean == null || isEmpty(bookSourceBean.getRuleSearchUrl())) {
                emitter.onError(new IllegalArgumentException("search Book failed for: " + content));
            } else {
                emitter.onNext(new BookList(tag, name, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(bookList -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookSourceBean.getRealRuleSearchUrl(), content, page, headerMap(false));
            if (bookList.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .postData(analyzeUrl.getPostData())
                        .headerMap(analyzeUrl.getHeaderMap())
                        .cookieStore(CookieHelper.get());
                switch (analyzeUrl.getRequestMethod()) {
                    case DEFAULT:
                    case POST:
                        params.url(analyzeUrl.getUrl());
                        break;
                    case GET:
                        params.url(analyzeUrl.getQueryUrl());
                }
                return ajax(params)
                        .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()));
            }
        }).doOnError(throwable -> Logger.e(TAG, "searchBook", throwable))
                .onErrorReturnItem(new ArrayList<>());

    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return Observable.create((ObservableOnSubscribe<BookInfo>) emitter -> {
            if (bookSourceBean == null) {
                emitter.onError(new Exception("没有找到当前书源"));
            } else {
                emitter.onNext(new BookInfo(tag, name, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(bookInfo -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookShelfBean.getNoteUrl(), headerMap(true));
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookInfo.analyzeBookInfo(response, bookShelfBean));
        }).doOnError(throwable -> Logger.e(TAG, "bookInfo", throwable));

    }

    /**
     * 获取目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        return Observable.create((ObservableOnSubscribe<BookChapters>) emitter -> {
            if (bookSourceBean == null) {
                emitter.onError(new Exception("没有找到当前书源"));
            } else {
                emitter.onNext(new BookChapters(tag, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(bookChapters -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getNoteUrl(), bookShelfBean.getBookInfoBean().getChapterListUrl(), headerMap(true));
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookChapters.analyzeChapters(response, bookShelfBean));
        }).doOnError(throwable -> Logger.e(TAG, "chapterList", throwable));
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final String chapterUrl, final ChapterBean chapter) {
        return Observable.create((ObservableOnSubscribe<BookContent>) emitter -> {
            if (bookSourceBean == null) {
                emitter.onError(new Exception("没有找到当前书源"));
            } else {
                emitter.onNext(new BookContent(tag, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(bookContent -> {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrl, chapter.getDurChapterUrl(), headerMap(true));
            if (bookContent.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .postData(analyzeUrl.getPostData())
                        .headerMap(analyzeUrl.getHeaderMap())
                        .cookieStore(CookieHelper.get());
                switch (analyzeUrl.getRequestMethod()) {
                    case DEFAULT:
                    case POST:
                        params.url(analyzeUrl.getUrl());
                        break;
                    case GET:
                        params.url(analyzeUrl.getQueryUrl());
                }
                return ajax(params)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
            }
        }).doOnError(throwable -> Logger.e(TAG, "bookContent", throwable));
    }

    @Override
    public Observable<ChapterBean> getAudioBookContent(final String chapterUrl, final ChapterBean chapter) {
        return Observable.create((ObservableOnSubscribe<AudioBookChapter>) emitter -> {
            if (bookSourceBean == null) {
                emitter.onError(new Exception("没有找到书源"));
            } else {
                emitter.onNext(new AudioBookChapter(tag, bookSourceBean));
                emitter.onComplete();
            }
        }).flatMap(audioBookChapter -> {
            if (audioBookChapter.isDirect()) {
                chapter.setDurChapterPlayUrl(chapter.getDurChapterUrl());
                return Observable.just(chapter);
            }

            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrl, chapter.getDurChapterUrl(), headerMap(true));
            if (audioBookChapter.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .suffix(audioBookChapter.getSuffix())
                        .cookieStore(CookieHelper.get())
                        .postData(analyzeUrl.getPostData())
                        .headerMap(analyzeUrl.getHeaderMap())
                        .javaScript(audioBookChapter.getJavaScript());
                switch (analyzeUrl.getRequestMethod()) {
                    case DEFAULT:
                    case POST:
                        params.url(analyzeUrl.getUrl());
                        break;
                    case GET:
                        params.url(analyzeUrl.getQueryUrl());
                }
                return sniff(params)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, chapter));
            }
        }).doOnError(throwable -> Logger.e(TAG, "audioBookContent", throwable));
    }

    private Observable<String> toObservable(AnalyzeUrl analyzeUrl) {
        return SimpleModel.getResponse(analyzeUrl)
                .flatMap(response -> setCookie(response, tag))
                .doOnNext(response -> {
                    final String requestUrl;
                    okhttp3.Response networkResponse = response.raw().networkResponse();
                    if (networkResponse != null) {
                        requestUrl = networkResponse.request().url().toString();
                    } else {
                        requestUrl = response.raw().request().url().toString();
                    }
                    analyzeUrl.setRequestUrl(requestUrl);
                })
                .map(Response::body);
    }


    private Observable<Response<String>> setCookie(Response<String> response, String tag) {
        return Observable.create(e -> {
            if (!response.raw().headers("Set-Cookie").isEmpty()) {
                final StringBuilder cookieBuilder = new StringBuilder();
                for (String s : response.raw().headers("Set-Cookie")) {
                    String[] x = s.split(";");
                    for (String y : x) {
                        if (!TextUtils.isEmpty(y)) {
                            cookieBuilder.append(y).append(";");
                        }
                    }
                }
                String cookie = cookieBuilder.toString();
                if (!TextUtils.isEmpty(cookie)) {
                    CookieHelper.get().replaceCookie(tag, cookie);
                }
            }
            e.onNext(response);
            e.onComplete();
        });
    }
}
