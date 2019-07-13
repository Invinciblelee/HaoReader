//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.model.content.DefaultModel;
import com.monke.monkeybook.model.content.DefaultShuqi;
import com.monke.monkeybook.model.impl.IAudioBookChapterModel;
import com.monke.monkeybook.model.impl.IStationBookModel;
import com.monke.monkeybook.model.impl.IWebBookModel;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.Observable;

public class WebBookModel implements IWebBookModel {

    private volatile static WebBookModel sInstance;

    private WebBookModel() {
    }

    public static WebBookModel getInstance() {
        if (sInstance == null) {
            synchronized (WebBookModel.class) {
                if (sInstance == null) {
                    sInstance = new WebBookModel();
                }
            }
        }
        return sInstance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        IStationBookModel bookModel = getBookSourceModel(bookShelfBean.getTag());
        if (bookModel != null) {
            return bookModel.getBookInfo(bookShelfBean);
        } else {
            return Observable.error(new Throwable(bookShelfBean.getBookInfoBean().getName() + "没有书源"));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getChapterList(final BookShelfBean bookShelfBean) {
        IStationBookModel bookModel = getBookSourceModel(bookShelfBean.getTag());
        if (bookModel != null) {
            return bookModel.getChapterList(bookShelfBean)
                    .flatMap((chapterList) -> updateChapterList(bookShelfBean, chapterList));
        } else {
            return Observable.error(new Throwable(bookShelfBean.getBookInfoBean().getName() + "没有书源"));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(BookInfoBean bookInfo, ChapterBean chapter) {
        IStationBookModel bookModel = getBookSourceModel(bookInfo.getTag());
        if (bookModel != null) {
            return bookModel.getBookContent(bookInfo.getChapterListUrl(), chapter)
                    .flatMap(bookContentBean -> saveChapterInfo(bookInfo, bookContentBean));
        } else {
            return Observable.error(new Exception("can not find book source."));
        }
    }

    @Override
    public Observable<ChapterBean> getAudioBookContent(BookInfoBean bookInfo, ChapterBean chapter) {
        IStationBookModel bookModel = getBookSourceModel(bookInfo.getTag());
        if (bookModel instanceof IAudioBookChapterModel) {
            return ((IAudioBookChapterModel) bookModel).getAudioBookContent(bookInfo.getChapterListUrl(), chapter);
        } else {
            return Observable.error(new IllegalAccessException("the model is not IAudioBookChapterModel."));
        }
    }

    /**
     * 其他站点集合搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchBook(String tag, String content, int page) {
        //获取所有书源类
        IStationBookModel bookModel = getBookSourceModel(tag);
        if (bookModel != null) {
            return bookModel.searchBook(content, page);
        } else {
            return Observable.error(new Exception("can not find book source."));
        }
    }

    /**
     * 发现页
     */
    @Override
    public Observable<List<SearchBookBean>> findBook(String tag, String url, int page) {
        IStationBookModel bookModel = getBookSourceModel(tag);
        if (bookModel != null) {
            return bookModel.findBook(url, page);
        } else {
            return Observable.error(new Exception("can not find book source."));
        }
    }

    //获取book source class
    private IStationBookModel getBookSourceModel(String tag) {
        if (BookShelfBean.LOCAL_TAG.equals(tag)) {
            return null;
        } else if (TextUtils.equals(tag, DefaultShuqi.TAG)) {
            return DefaultShuqi.getInstance();
        } else {
            return DefaultModel.newInstance(tag);
        }
    }

    private Observable<BookShelfBean> updateChapterList(BookShelfBean bookShelfBean, List<ChapterBean> chapterList) {
        return Observable.create(e -> {
            for (ListIterator<ChapterBean> it = chapterList.listIterator(); it.hasNext(); ) {
                ChapterBean chapter = it.next();
                chapter.setDurChapterIndex(it.previousIndex());
                if (Objects.equals(chapter.getDurChapterName(), bookShelfBean.getDurChapterName())) {
                    bookShelfBean.setDurChapter(chapter.getDurChapterIndex());
                }
            }

            if (bookShelfBean.getChapterListSize() < chapterList.size()) {
                final int newChapters;
                if (bookShelfBean.getChapterListSize() > 0) {
                    newChapters = bookShelfBean.getNewChapters() + (chapterList.size() - bookShelfBean.getChapterListSize());
                } else {
                    newChapters = 0;
                }
                bookShelfBean.setNewChapters(Math.min(newChapters, chapterList.size()));
                bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.getBookInfoBean().setFinalRefreshData(System.currentTimeMillis());
                bookShelfBean.setHasUpdate(true);
            }

            if (!chapterList.isEmpty()) {
                BookshelfHelp.delChapterList(bookShelfBean.getNoteUrl(), bookShelfBean.getChapterList());
                bookShelfBean.setChapterList(chapterList, true);
                bookShelfBean.upLastChapterName();
                if (!TextUtils.isEmpty(bookShelfBean.getDurChapterName())) {
                    bookShelfBean.upDurChapterName();
                }
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    private Observable<BookContentBean> saveChapterInfo(BookInfoBean bookInfo, BookContentBean bookContentBean) {
        return Observable.create(e -> {
            if (bookContentBean.getRight()) {
                if (ChapterContentHelp.saveChapterInfo(ChapterContentHelp.getCacheFolderPath(bookInfo),
                        ChapterContentHelp.getCacheFileName(bookContentBean),
                        bookContentBean.getDurChapterContent())) {
                    e.onNext(bookContentBean);
                    e.onComplete();
                    return;
                }
            }
            e.onError(new Throwable("保存章节出错"));
            e.onComplete();
        });
    }
}
