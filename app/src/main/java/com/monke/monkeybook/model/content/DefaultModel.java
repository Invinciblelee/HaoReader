package com.monke.monkeybook.model.content;

import android.util.Log;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.CookieHelper;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.analyzeRule.AnalyzeSearchUrl;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IHttpPostApi;
import com.monke.monkeybook.model.impl.IStationBookModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.text.TextUtils.isEmpty;

/**
 * 默认检索规则
 */
public class DefaultModel extends BaseModelImpl implements IStationBookModel {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap;

    private DefaultModel(String tag) {
        this.tag = tag;
        try {
            URL url = new URL(tag);
            name = url.getHost();
        } catch (MalformedURLException e) {
            name = tag;
        }
    }

    public static DefaultModel newInstance(String tag) {
        return new DefaultModel(tag);
    }

    private Boolean initBookSourceBean() {
        if (bookSourceBean == null) {
            bookSourceBean = DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
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

    /**
     * 发现
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        if (!initBookSourceBean() || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeSearchUrl analyzeSearchUrl = new AnalyzeSearchUrl(url, "", page);
            if (analyzeSearchUrl.getSearchUrl() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(new ArrayList<>());
                    emitter.onComplete();
                });
            }
            if (url.contains("@")) {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpPostApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else if (url.contains("?")) {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpGetApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpGetApi.class)
                        .getWebContent(analyzeSearchUrl.getSearchPath(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
    }

    /**
     * 搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        if (!initBookSourceBean() || isEmpty(bookSourceBean.getRuleSearchUrl())) {
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
        BookList bookList = new BookList(tag, name, bookSourceBean);
        try {
            AnalyzeSearchUrl analyzeSearchUrl = new AnalyzeSearchUrl(bookSourceBean.getRuleSearchUrl(), content, page);
            if (analyzeSearchUrl.getSearchUrl() == null) {
                return Observable.create(emitter -> {
                    emitter.onNext(new ArrayList<>());
                    emitter.onComplete();
                });
            }
            if (bookSourceBean.getRuleSearchUrl().contains("@")) {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpPostApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else if (bookSourceBean.getRuleSearchUrl().contains("?")) {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpGetApi.class)
                        .searchBook(analyzeSearchUrl.getSearchPath(),
                                analyzeSearchUrl.getQueryMap(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            } else {
                return createService(analyzeSearchUrl.getSearchUrl(), IHttpGetApi.class)
                        .getWebContent(analyzeSearchUrl.getSearchPath(),
                                headerMap)
                        .flatMap(bookList::analyzeSearchBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.create(emitter -> {
                emitter.onNext(new ArrayList<>());
                emitter.onComplete();
            });
        }
    }

    /**
     * 获取书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.error(new Throwable(String.format("无法找到源%s", tag)));
        }
        BookInfo bookInfo = new BookInfo(tag, name, bookSourceBean);
        return createService(tag, IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl(), headerMap)
                .flatMap(response -> bookInfo.analyzeBookInfo(response.body(), bookShelfBean));
    }

    /**
     * 获取目录
     */
    @Override
    public Observable<List<ChapterListBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (!initBookSourceBean()) {
            return Observable.create(emitter -> {
                emitter.onError(new Throwable(String.format("%s没有找到书源配置", bookShelfBean.getBookInfoBean().getName())));
                emitter.onComplete();
            });
        }
        BookChapters bookChapter = new BookChapters(tag, bookSourceBean);
        return createService(tag, IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterListUrl(), headerMap)
                .flatMap(response -> bookChapter.analyzeChapters(response.body(), bookShelfBean.getBookInfoBean().getChapterListUrl()));
    }

    /**
     * 获取正文
     */
    @Override
    public Observable<BookContentBean> getBookContent(final Scheduler scheduler, final ChapterListBean chapter) {
        if (!initBookSourceBean()) {
            return Observable.create(emitter -> {
                emitter.onNext(new BookContentBean());
                emitter.onComplete();
            });
        }
        BookContent bookContent = new BookContent(tag, bookSourceBean);
        if (bookContent.isAJAX()) {
            return getAjaxHtml(MApplication.getInstance(), chapter.getDurChapterUrl(), AnalyzeHeaders.getUserAgent(bookSourceBean.getHttpUserAgent()))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(scheduler)
                    .flatMap(response -> bookContent.analyzeBookContent(response, chapter));
        } else {
            return createService(tag, IHttpGetApi.class)
                    .getWebContent(chapter.getDurChapterUrl(), headerMap)
                    .subscribeOn(scheduler)
                    .flatMap(response -> bookContent.analyzeBookContent(response.body(), chapter));
        }
    }

}
