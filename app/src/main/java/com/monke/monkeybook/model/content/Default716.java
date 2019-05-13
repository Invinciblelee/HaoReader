package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.model.impl.IStationBookModel;
import com.monke.monkeybook.utils.StringUtils;

import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import retrofit2.Response;

public class Default716 extends BaseModelImpl implements IStationBookModel {
    public static final String TAG = "My716";

    public static Default716 newInstance() {
        return new Default716();
    }

    /**
     * 发现书籍
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        return null;
    }

    /**
     * 搜索书籍
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("query", content);
        return OkHttpHelper.getInstance().createService("http://api.zhuishushenqi.com", IHttpGetApi.class)
                .searchBook("http://api.zhuishushenqi.com/book/fuzzy-search",
                        queryMap,
                        AnalyzeHeaders.getMap(null))
                .flatMap(this::analyzeSearchBook);
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBookList = new ArrayList<>();
            JsonObject root = new JsonParser().parse(Objects.requireNonNull(response.body())).getAsJsonObject();
            if (root.get("ok").getAsBoolean()) {
                JsonArray bookArray = root.get("books").getAsJsonArray();
                for (int i = 0, size = bookArray.size(); i < size; i++) {
                    JsonObject book = bookArray.get(i).getAsJsonObject();
                    SearchBookBean searchBookBean = new SearchBookBean();
                    searchBookBean.setTag(TAG);
                    searchBookBean.setOrigin(TAG);
                    searchBookBean.setBookType(BookType.TEXT);
                    searchBookBean.setWeight(Integer.MAX_VALUE);
                    searchBookBean.setKind(book.get("cat").getAsString());
                    searchBookBean.setName(book.get("title").getAsString());
                    searchBookBean.setAuthor(book.get("author").getAsString());
                    searchBookBean.setNoteUrl("@716:http://api.zhuishushenqi.com/atoc?view=summary&book=" + book.get("_id").getAsString());
                    searchBookBean.setLastChapter(book.get("lastChapter").getAsString().replaceAll("^\\s*正文[卷：\\s]+", ""));
                    searchBookBean.setCoverUrl("http://statics.zhuishushenqi.com" + book.get("cover").getAsString());
                    searchBookBean.setIntroduce(book.get("shortIntro").getAsString());
                    searchBookList.add(searchBookBean);
                }
            }
            e.onNext(searchBookList);
            e.onComplete();
        });
    }

    /**
     * 网络请求并解析书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return OkHttpHelper.getInstance().createService("http://api.zhuishushenqi.com", IHttpGetApi.class)
                .getWebContent(bookShelfBean.getNoteUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeBookInfo(response.body(), bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败"));
                e.onComplete();
                return;
            }

            JsonArray sourceArray = new JsonParser().parse(s).getAsJsonArray();
            HashMap<String, JsonObject> sourceMap = new HashMap<>();
            for (int i = 0, size = sourceArray.size(); i < size; i++) {
                JsonObject source = sourceArray.get(i).getAsJsonObject();
                String name = source.get("source").getAsString();
                sourceMap.put(name, source);
            }

            JsonObject targetSource = null;
            if (sourceMap.containsKey("xbiquge")) {
                targetSource = sourceMap.get("xbiquge");
            } else if (sourceMap.containsKey("my176")) {
                targetSource = sourceMap.get("my176");
            } else if (sourceMap.containsKey("hunhun")) {
                targetSource = sourceMap.get("hunhun");
            } else if (sourceMap.containsKey("sanjiangge")) {
                targetSource = sourceMap.get("sanjiangge");
            } else if (sourceMap.containsKey("xiaoxiaoshuwu")) {
                targetSource = sourceMap.get("xiaoxiaoshuwu");
            } else if (sourceMap.containsKey("luoqiu")) {
                targetSource = sourceMap.get("luoqiu");
            } else if (sourceMap.containsKey("snwx")) {
                targetSource = sourceMap.get("snwx");
            } else if (sourceMap.containsKey("tianyibook")) {
                targetSource = sourceMap.get("tianyibook");
            } else if (sourceMap.containsKey("shuhaha")) {
                targetSource = sourceMap.get("shuhaha");
            } else if (sourceMap.containsKey("lewenwu")) {
                targetSource = sourceMap.get("lewenwu");
            } else if (sourceMap.containsKey("zhuishuvip")) {
                targetSource = sourceMap.get("zhuishuvip");
            } else if (sourceMap.size() > 0) {
                Iterator<String> it = sourceMap.keySet().iterator();
                targetSource = sourceMap.get(it.next());
            }

            if (targetSource != null) {
                bookShelfBean.setLastChapterName(targetSource.get("lastChapter").getAsString());
                BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
                bookInfoBean.setBookType(BookType.TEXT);
                bookInfoBean.setTag(TAG);
                bookInfoBean.setOrigin(TAG);
                bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());
                bookInfoBean.setChapterListUrl("http://api.zhuishushenqi.com/atoc/" + targetSource.get("_id").getAsString() + "?view=chapters");
                e.onNext(bookShelfBean);
            } else {
                e.onError(new Throwable("书籍信息获取失败"));
            }
            e.onComplete();
        });
    }

    /**
     * 网络解析图书目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(BookShelfBean bookShelfBean) {
        return OkHttpHelper.getInstance().createService("http://api.zhuishushenqi.com", IHttpGetApi.class)
                .getWebContent(bookShelfBean.getBookInfoBean().getChapterListUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeChapterList(response.body(), bookShelfBean));
    }

    private Observable<List<ChapterBean>> analyzeChapterList(String s, BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            List<ChapterBean> chapterList = new ArrayList<>();
            JsonObject root = new JsonParser().parse(s).getAsJsonObject();
            JsonArray chapterArray = root.get("chapters").getAsJsonArray();
            for (int i = 0, size = chapterArray.size(); i < size; i++) {
                JsonObject chapter = chapterArray.get(i).getAsJsonObject();
                ChapterBean chapterBean = new ChapterBean();
                chapterBean.setDurChapterIndex(i);
                chapterBean.setDurChapterName(chapter.get("title").getAsString());
                final String link = chapter.get("link").getAsString();
                if (link.contains("vip.zhuishushenqi")
                        || link.contains("xbiquge")) {
                    chapterBean.setDurChapterUrl("http://chapterup.zhuishushenqi.com/chapter/" + URLEncoder.encode(link, "UTF-8"));
                } else {
                    chapterBean.setDurChapterUrl(link);
                }
                chapterList.add(chapterBean);
            }
            e.onNext(chapterList);
            e.onComplete();
        });
    }

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(ChapterBean chapterBean) {
        return OkHttpHelper.getInstance().createService(StringUtils.getBaseUrl(chapterBean.getDurChapterUrl()), IHttpGetApi.class)
                .getWebContent(chapterBean.getDurChapterUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeBookContent(response.body(), chapterBean));
    }

    private Observable<BookContentBean> analyzeBookContent(String s, ChapterBean chapterBean) {
        return Observable.create(e -> {
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
            bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
            bookContentBean.setDurChapterName(chapterBean.getDurChapterName());
            bookContentBean.setNoteUrl(chapterBean.getNoteUrl());

            if (chapterBean.getDurChapterUrl().contains("zhuishushenqi")) {
                JsonObject root = new JsonParser().parse(s).getAsJsonObject();
                if (root.get("ok").getAsBoolean()) {
                    JsonObject chapterJson = root.get("chapter").getAsJsonObject();
                    if (chapterJson.has("isVip")) {
                        if (chapterJson.get("isVip").getAsBoolean()) {
                            bookContentBean.setDurChapterContent("当前章节为VIP章节，无法阅读，请换源。");
                        } else {
                            bookContentBean.setDurChapterContent(chapterJson.get("cpContent").getAsString());
                        }
                    } else {
                        bookContentBean.setDurChapterContent(chapterJson.get("body").getAsString());
                    }
                }
            } else {
                JXDocument document = JXDocument.create(s);
                Object object = document.selOne("//div[@name=\"content\"] or @id=\"content\" or @class=\"txt_tcontent\" or @id=\"htmlContent\"");
                if (object instanceof Element) {
                    bookContentBean.setDurChapterContent(StringUtils.formatHtml(((Element) object).html()));
                } else {
                    bookContentBean.setDurChapterContent(StringUtils.formatHtml(StringUtils.valueOf(object)));
                }
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

}
