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
import io.reactivex.android.schedulers.AndroidSchedulers;
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(url, page, headerMap(false), tag);
            if (analyzeUrl.getHost() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(ListUtils.mutableList());
                    emitter.onComplete();
                });
            }
            return toObservable(analyzeUrl)
                    .flatMap(bookList::analyzeSearchBook);
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
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookSourceBean.getRuleSearchUrl(), content, page, headerMap(false), tag);
            if (analyzeUrl.getHost() == null) {
                return Observable.just(ListUtils.mutableList());
            }
            return toObservable(analyzeUrl)
                    .flatMap(bookList::analyzeSearchBook);
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
            return Observable.error(new BookException("没有找到当前书源"));
        }
        final BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getNoteUrl(), headerMap(false), tag);
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), bookShelfBean));
        } catch (Exception e) {
            return Observable.error(new BookException("书籍信息获取失败"));
        }
    }

    /**
     * 获取目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.error(new BookException("没有找到当前书源"));
        }
        final BookChapters bookChapter = new BookChapters(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getBookInfoBean().getChapterListUrl(), headerMap(false), tag);
            return toObservable(analyzeUrl)
                    .flatMap(response -> bookChapter.analyzeChapters(response.body(), bookShelfBean, headerMap(false)));
        } catch (Exception e) {
            Logger.e(TAG, "目录获取失败", e);
            return Observable.error(new BookException("目录获取失败"));
        }
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final ChapterBean chapter) {
        if (!initBookSourceBean()) {
            return Observable.error(new BookException("没有找到当前书源"));
        }

        final BookContent bookContent = new BookContent(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapter.getDurChapterUrl(), headerMap(true), tag);
            if (bookContent.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(MApplication.getInstance(), tag)
                        .cookieStore(CookieHelper.get())
                        .userAgent(analyzeUrl.getUserAgent());
                switch (analyzeUrl.getUrlMode()) {
                    case POST:
                        params.url(analyzeUrl.getUrl()).postData(analyzeUrl.getPostData());
                        break;
                    case GET:
                        params.url(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr())).headerMap(analyzeUrl.getHeaderMap());
                        break;
                    default:
                        params.url(analyzeUrl.getUrl()).headerMap(analyzeUrl.getHeaderMap());
                }
                return ajax(params).subscribeOn(AndroidSchedulers.mainThread())
                        .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> bookContent.analyzeBookContent(response.body(), chapter));
            }
        } catch (Exception e) {
            Logger.e(TAG, "正文获取失败", e);
            return Observable.error(new BookException("正文获取失败"));
        }

    }

    @Override
    public Observable<ChapterBean> processAudioChapter(ChapterBean chapter) {
        if (!initBookSourceBean()) {
            return Observable.error(new BookException("没有找到书源"));
        }

        if (isEmpty(bookSourceBean.getRuleBookContent())) {
            chapter.setDurChapterPlayUrl(chapter.getDurChapterUrl());
            return Observable.just(chapter);
        }

        final AudioBookChapter audioBookChapter = new AudioBookChapter(tag, bookSourceBean);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapter.getDurChapterUrl(), headerMap(true), tag);
            if (audioBookChapter.isAJAX()) {
                final AjaxWebView.AjaxParams params = new AjaxWebView.AjaxParams(MApplication.getInstance(), tag)
                        .suffix(audioBookChapter.getSuffix())
                        .cookieStore(CookieHelper.get())
                        .userAgent(analyzeUrl.getUserAgent())
                        .javaScript(audioBookChapter.getJavaScript());
                switch (analyzeUrl.getUrlMode()) {
                    case POST:
                        params.url(analyzeUrl.getUrl()).postData(analyzeUrl.getPostData());
                        break;
                    case GET:
                        params.url(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr())).headerMap(analyzeUrl.getHeaderMap());
                        break;
                    default:
                        params.url(analyzeUrl.getUrl()).headerMap(analyzeUrl.getHeaderMap());
                }
                return sniff(params).subscribeOn(AndroidSchedulers.mainThread())
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response, chapter));
            } else {
                return toObservable(analyzeUrl)
                        .flatMap(response -> audioBookChapter.analyzeAudioChapter(response.body(), chapter));
            }
        } catch (Exception e) {
            return Observable.error(new BookException("播放链接获取失败"));
        }
    }

    private Observable<Response<String>> toObservable(AnalyzeUrl analyzeUrl) {
        return SimpleModel.getResponse(analyzeUrl)
                .flatMap(response -> setCookie(response, tag));
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
