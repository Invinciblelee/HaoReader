package com.monke.monkeybook.model.content;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.monke.monkeybook.utils.MD5Utils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

public class Defaultsq extends BaseModelImpl implements IStationBookModel {
    public static final String TAG = "Shuqi";

    public static Defaultsq newInstance() {
        return new Defaultsq();
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
        return OkHttpHelper.getInstance().createService("http://read.xiaoshuo1-sm.com", IHttpGetApi.class)
                .getsearchBook("is_serchpay", "3", content, page, "30")
                .flatMap(this::analyzeSearchBook);
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBookList = new ArrayList<>();
            SqBook book, book1;
            List<SearchBookBean> books = new ArrayList<>();
            SearchBookBean item ;
            JSONObject object = JSONObject.parseObject(response.body());
            JSONObject booksObject1 = object.getJSONObject("info");
            book1 = JSON.parseObject(booksObject1.toString(), SqBook.class);
            int pageI = book1.getPage();
            int pages = book1.getCount();
            if(pageI == 1 && pageI <= pages) {
                JSONObject booksObject = object.getJSONObject("aladdin");
                book = JSON.parseObject(booksObject.toString(), SqBook.class);
                JSONObject jsonx = (JSONObject) book.getLatest_chapter();
                String cname = jsonx.getString("cname");
                item = new SearchBookBean();
                item.setTag(TAG);
                item.setOrigin(TAG);
                item.setBookType(BookType.TEXT);
                item.setWeight(Integer.MAX_VALUE);
                item.setAuthor(book.getAuthor());
                item.setKind(book.getCategory());
                item.setLastChapter(cname);
                item.setName(book.getTitle());
                item.setNoteUrl(book.getBid());
                item.setCoverUrl(book.getCover().replace("\\/", "/"));
                item.setIntroduce(book.getDesc());
                books.add(item);
            }
            if(pageI <= pages) {
                JSONArray booksArray = object.getJSONArray("data");
                for (Object jsonObject : booksArray) {
                    book = JSON.parseObject(jsonObject.toString(), SqBook.class);
                    item = new SearchBookBean();
                    item.setTag(TAG);
                    item.setOrigin(TAG);
                    item.setBookType(BookType.TEXT);
                    item.setWeight(Integer.MAX_VALUE);
                    item.setAuthor(book.getAuthor());
                    item.setKind(book.getCategory());
                    item.setLastChapter(book.getFirst_chapter());
                    item.setName(book.getTitle());
                    item.setNoteUrl(book.getBid());
                    item.setCoverUrl(book.getCover().replace("\\/", "/"));
                    item.setIntroduce(book.getDesc());
                    books.add(item);
                }
            }
            e.onNext(books);
            e.onComplete();
        });
    }

    /**
     * 网络请求并解析书籍信息
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        String bid = bookShelfBean.getNoteUrl();
        String Data = bid + "1514984538213800000037e81a9d8f02596e1b895d07c171d5c9";
        String Sign = MD5Utils.strToMd5By32(Data);
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("timestamp", "1514984538213");
        fieldMap.put("user_id", "8000000");
        fieldMap.put("bookId", bid);
        fieldMap.put("sign", Sign);
        return OkHttpHelper.getInstance().createService("http://walden1.shuqireader.com", IHttpGetApi.class)
                .getbookdetail(fieldMap)
                .flatMap(response -> analyzeBookInfo(response.body(), bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable("书籍信息获取失败"));
                e.onComplete();
                return;
            }
            JSONObject object = JSONObject.parseObject(s);
            JSONObject data = (JSONObject) object.get("data");
            SqBookDetail bd = JSON.parseObject(data.toString(),SqBookDetail.class);
            JSONObject jsonx = (JSONObject) bd.getLastChapter();
            String chapterName = jsonx.getString("chapterName");
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            bookShelfBean.setLastChapterName(chapterName);
            bookInfoBean.setBookType(BookType.TEXT);
            bookInfoBean.setTag(TAG);
            bookInfoBean.setOrigin(TAG);
            bookInfoBean.setCoverUrl(bd.getImgUrl());
            bookInfoBean.setName(bd.getBookName());
            bookInfoBean.setAuthor(bd.getAuthorName());
            bookInfoBean.setIntroduce(bd.getDesc());
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setChapterListUrl(bookShelfBean.getNoteUrl());

            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /**
     * 网络解析图书目录
     */
    @Override
    public Observable<List<ChapterBean>> getChapterList(BookShelfBean bookShelfBean) {
        String bid = bookShelfBean.getNoteUrl();
        String Data = bid + "1514984538213800000037e81a9d8f02596e1b895d07c171d5c9";
        String Sign = MD5Utils.strToMd5By32(Data);
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("timestamp", "1514984538213");
        fieldMap.put("user_id", "8000000");
        fieldMap.put("bookId", bid);
        fieldMap.put("sign", Sign);
        return OkHttpHelper.getInstance().createService("http://walden1.shuqireader.com", IHttpGetApi.class)
                .getchapterlist(fieldMap)
                .flatMap(response -> analyzeChapterList(response.body(),bookShelfBean.getNoteUrl()));
    }

    private Observable<List<ChapterBean>> analyzeChapterList(String s, String novelUrl) {
        return Observable.create(e -> {
            List<ChapterBean> chapterBeans = new ArrayList<>();
            JSONObject object = JSONObject.parseObject(s);
            JSONObject data = (JSONObject) object.get("data");
            JSONArray chapterListArray = data.getJSONArray("chapterList");
            int i = 1;
            for (Object chapterListsObject:chapterListArray) {
                JSONArray volumeListsArray = JSONObject.parseObject(chapterListsObject.toString()).getJSONArray("volumeList");
                for (Object volumeListObject:volumeListsArray) {
                    SqChapterList cl = JSON.parseObject(volumeListObject.toString(), SqChapterList.class);
                    String ChapterId = cl.getChapterId();
                    String ChapterName= cl.getChapterName();
                    ChapterBean temp = new ChapterBean();
                    temp.setDurChapterUrl("http://c1.shuqireader.com/httpserver/filecache/get_book_content_" + novelUrl +"_"+ ChapterId +".xml");   //id
                    temp.setDurChapterIndex(i);
                    temp.setDurChapterName(ChapterName);
                    temp.setNoteUrl(novelUrl);
                    chapterBeans.add(temp);
                    i++;
                }
            }
            e.onNext(chapterBeans);
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
            bookContentBean.setDurChapterContent(DecodeChapterContent(getSubString(s,"[CDATA[","]]>")));

            e.onNext(bookContentBean);
            e.onComplete();
        });
    }
    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right  后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }
    public String DecodeChapterContent(String code)
    {
        String ss = "";
        try {
            byte[] bytes = code.getBytes("UTF-8");

            for (int i = 0; i < bytes.length; i++)
            {
                int charAt = bytes[i];
                if (65 <= charAt && charAt <= 90)
                {
                    charAt = charAt + 13;
                    if (charAt > 90)
                    {
                        charAt = ((charAt % 90) + 65) - 1;
                    }
                }
                else if (97 <= charAt && charAt <= 122)
                {
                    charAt = charAt + 13;
                    if (charAt > 122)
                    {
                        charAt = ((charAt % 122) + 97) - 1;
                    }
                }
                bytes[i] = (byte)charAt;
            }
            code = new String(bytes,"UTF-8");
            byte[] bbb = Base64Decoder.decodeToBytes2(bytes);
            ss = new String(bbb,"UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return ss.replace("<br/>", "\n");
    }
}
