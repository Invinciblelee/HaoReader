package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.monke.basemvplib.AjaxWebView;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.help.MemoryCache;
import com.monke.monkeybook.model.SimpleModel;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.analyzeRule.AnalyzeUrl;
import com.monke.monkeybook.model.impl.IAudioBookChapterModel;
import com.monke.monkeybook.model.impl.IStationBookModel;
import com.monke.monkeybook.utils.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
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
    }

    public static DefaultModel newInstance(String tag) {
        return new DefaultModel(tag);
    }

    private Boolean initBookSourceBean() {
        if (bookSourceBean == null) {
            bookSourceBean = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();
            if (bookSourceBean != null) {
                name = bookSourceBean.getBookSourceName();
                headerMap = AnalyzeHeaders.getMap(bookSourceBean);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
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
        if (!initBookSourceBean() || isEmpty(url)) {
            return Observable.create(emitter -> {
                emitter.onNext(ListUtils.mutableList());
                emitter.onComplete();
            });
        }
        final BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, url, page, headerMap(false));
            if (analyzeUrl.getHost() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(ListUtils.mutableList());
                    emitter.onComplete();
                });
            }
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()));
        } catch (Exception e) {
            Logger.e(TAG, "findBook: " + url, e);
            return Observable.just(ListUtils.mutableList());
        }
    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        if (!initBookSourceBean() || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(ListUtils.mutableList());
                emitter.onComplete();
            });
        }
        final BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookSourceBean.getRuleSearchUrl(), content, page, headerMap(false));
            if (analyzeUrl.getHost() == null) {
                return Observable.just(ListUtils.mutableList());
            }

            return toObservable(analyzeUrl)
                    .flatMap(response -> bookList.analyzeSearchBook(response, analyzeUrl.getRequestUrl()));
        } catch (Exception e) {
            Logger.e(TAG, "searchBook: " + content, e);
            return Observable.just(ListUtils.mutableList());
        }
    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.error(new Exception("没有找到当前书源"));
        }
        final BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookShelfBean.getNoteUrl(), headerMap(true));
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookInfo.analyzeBookInfo(response, bookShelfBean));
        } catch (Exception e) {
            Logger.e(TAG, "书籍信息获取失败", e);
            return Observable.error(e);
        }
    }

    /**
     * 获取目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.error(new Exception("没有找到当前书源"));
        }
        final BookChapters bookChapter = new BookChapters(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, bookShelfBean.getBookInfoBean().getChapterListUrl(), headerMap(true));
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookChapter.analyzeChapters(response, bookShelfBean));
        } catch (Exception e) {
            Logger.e(TAG, "目录获取失败", e);
            return Observable.error(e);
        }
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final ChapterBean chapter) {
        if (!initBookSourceBean()) {
            return Observable.error(new Exception("没有找到当前书源"));
        }

        final BookContent bookContent = new BookContent(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, chapter.getDurChapterUrl(), headerMap(true));
            if (bookContent.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(MApplication.getInstance(), tag)
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
                        params.url(analyzeUrl.getUrlWithQuery());
                }
                return ajax(params)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
            }
        } catch (Exception e) {
            Logger.e(TAG, "正文获取失败", e);
            return Observable.error(e);
        }

    }

    @Override
    public Observable<ChapterBean> processAudioChapter(ChapterBean chapter) {
        if (!initBookSourceBean()) {
            return Observable.error(new Exception("没有找到书源"));
        }

        if (isEmpty(bookSourceBean.getRuleBookContent())) {
            chapter.setDurChapterPlayUrl(chapter.getDurChapterUrl());
            return Observable.just(chapter);
        }

        final AudioBookChapter audioBookChapter = new AudioBookChapter(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(tag, chapter.getDurChapterUrl(), headerMap(true));
            if (audioBookChapter.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(MApplication.getInstance(), tag)
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
                        params.url(analyzeUrl.getUrlWithQuery());
                }
                return sniff(params)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, chapter));
            }
        } catch (Exception e) {
            Logger.e(TAG, "播放链接获取失败", e);
            return Observable.error(e);
        }
    }

    private Observable<String> toObservable(AnalyzeUrl analyzeUrl) {
        String cachedResponse = MemoryCache.INSTANCE.getCache(analyzeUrl.getId());
        if (cachedResponse != null) {
            return Observable.just(cachedResponse);
        }
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
                .map(Response::body)
                .doOnNext(string -> MemoryCache.INSTANCE.putCache(analyzeUrl.getId(), string));
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
