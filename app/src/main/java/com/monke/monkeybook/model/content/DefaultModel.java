package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.basemvplib.AjaxWebView;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.content.exception.BookSourceException;
import com.monke.monkeybook.model.impl.IAudioBookChapterModel;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Response;

/**
 * 默认检索规则
 */
public class DefaultModel extends BaseModelImpl implements IStationBookModel, IAudioBookChapterModel {

    private static final String TAG = DefaultModel.class.getSimpleName();

    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap;

    private DefaultModel(String tag) throws BookSourceException {
        this.tag = tag;
        bookSourceBean = BookSourceManager.getByUrl(tag);
        if (bookSourceBean != null) {
            name = bookSourceBean.getBookSourceName();
            headerMap = AnalyzeHeaders.getMap(bookSourceBean);
        }
        if (bookSourceBean == null) {
            throw new BookSourceException("没有找到当前书源");
        }
    }

    public static DefaultModel newInstance(String tag) throws BookSourceException {
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
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, url, page, headerMap(false));
            final BookList bookList = new BookList(tag, name, bookSourceBean);
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()))
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof IOException) {
                            return Observable.error(throwable);
                        }
                        return Observable.just(new ArrayList<>());
                    });
        } catch (Exception e) {
            Logger.e(TAG, "findBook", e);
            return Observable.just(new ArrayList<>());
        }
    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookSourceBean.getRealRuleSearchUrl(), content, page, headerMap(false));
            final BookList bookList = new BookList(tag, name, bookSourceBean);
            if (bookList.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .postData(analyzeUrl.getPostData())
                        .headerMap(analyzeUrl.getHeaderMap())
                        .cookieStore(CookieHelper.getInstance());
                switch (analyzeUrl.getRequestMethod()) {
                    case DEFAULT:
                    case POST:
                        params.url(analyzeUrl.getUrl());
                        break;
                    case GET:
                        params.url(analyzeUrl.getQueryUrl());
                }
                return ajax(params)
                        .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()))
                        .onErrorReturnItem(new ArrayList<>());
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()))
                        .onErrorReturnItem(new ArrayList<>());
            }
        } catch (Exception e) {
            Logger.e(TAG, "searchBook", e);
            return Observable.just(new ArrayList<>());
        }
    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookShelfBean.getNoteUrl(), headerMap(true));
            final BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookInfo.analyzeBookInfo(response, analyzeUrl.getQueryUrl(), bookShelfBean));
        } catch (Exception e) {
            Logger.e(TAG, "bookInfo", e);
            return Observable.error(e);
        }
    }

    /**
     * 获取目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getNoteUrl(), bookShelfBean.getBookInfoBean().getChapterListUrl(), headerMap(true));
            final BookChapters bookChapters = new BookChapters(tag, bookSourceBean);
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookChapters.analyzeChapters(response, analyzeUrl.getQueryUrl(), bookShelfBean));
        } catch (Exception e) {
            Logger.e(TAG, "chapterList", e);
            return Observable.error(e);
        }
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final String chapterUrl, final ChapterBean chapter) {
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrl, chapter.getDurChapterUrl(), headerMap(true));
            final BookContent bookContent = new BookContent(tag, bookSourceBean);
            if (bookContent.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .postData(analyzeUrl.getPostData())
                        .headerMap(analyzeUrl.getHeaderMap())
                        .cookieStore(CookieHelper.getInstance());
                switch (analyzeUrl.getRequestMethod()) {
                    case DEFAULT:
                    case POST:
                        params.url(analyzeUrl.getUrl());
                        break;
                    case GET:
                        params.url(analyzeUrl.getQueryUrl());
                }
                return ajax(params)
                        .flatMap(response -> bookContent.analyzeBookContent(response, analyzeUrl.getQueryUrl(), chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookContent.analyzeBookContent(response, analyzeUrl.getQueryUrl(), chapter));
            }
        } catch (Exception e) {
            Logger.e(TAG, "getBookContent", e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ChapterBean> getAudioBookContent(final String chapterUrl, final ChapterBean chapter) {
        try {
            final AudioBookChapter audioBookChapter = new AudioBookChapter(tag, bookSourceBean);
            if (audioBookChapter.isDirect()) {
                chapter.setDurChapterPlayUrl(chapter.getDurChapterUrl());
                return Observable.just(chapter);
            }

            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrl, chapter.getDurChapterUrl(), headerMap(true));
            if (audioBookChapter.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(ContextHolder.getContext(), tag)
                        .requestMethod(analyzeUrl.getRequestMethod())
                        .suffix(audioBookChapter.getSuffix())
                        .cookieStore(CookieHelper.getInstance())
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
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, analyzeUrl.getQueryUrl(), chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, analyzeUrl.getQueryUrl(), chapter));
            }
        } catch (Exception e) {
            Logger.e(TAG, "audioBookContent", e);
            return Observable.error(e);
        }
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
        return Observable.create((ObservableOnSubscribe<Response<String>>) e -> {
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
                    CookieHelper.getInstance().replaceCookie(tag, cookie);
                }
            }
            e.onNext(response);
            e.onComplete();
        }).onErrorReturnItem(response);
    }
}
