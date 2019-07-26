package com.monke.monkeybook.model.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.URLUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Debug {
    public static String SOURCE_DEBUG_TAG;
    @SuppressLint("ConstantLocale")
    private static final DateFormat DEBUG_TIME_FORMAT = new SimpleDateFormat("[mm:ss.SSS]", Locale.getDefault());
    private static long startTime;

    private static String getDoTime() {
        return StringUtils.millis2String(System.currentTimeMillis() - startTime, DEBUG_TIME_FORMAT);
    }

    public static void newDebug(String tag, String key, @NonNull CompositeDisposable compositeDisposable, @NonNull CallBack callBack) {
        if (TextUtils.isEmpty(tag)) {
            callBack.printError("书源url不能为空");
            return;
        }
        key = StringUtils.trim(key);
        if (TextUtils.isEmpty(key)) {
            callBack.printError("关键字不能为空");
            return;
        }
        new Debug(tag, key, compositeDisposable, callBack);
    }

    private CallBack callBack;
    private CompositeDisposable compositeDisposable;

    private Debug(String tag, String key, CompositeDisposable compositeDisposable, CallBack callBack) {
        startTime = System.currentTimeMillis();
        SOURCE_DEBUG_TAG = tag;
        this.callBack = callBack;
        this.compositeDisposable = compositeDisposable;
        if (URLUtils.isUrl(key)) {
            BookShelfBean bookShelfBean = new BookShelfBean();
            bookShelfBean.setTag(Debug.SOURCE_DEBUG_TAG);
            bookShelfBean.setNoteUrl(key);
            bookShelfBean.setDurChapter(0);
            bookShelfBean.setGroup(0);
            bookShelfBean.setDurChapterPage(0);
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookInfoDebug(bookShelfBean, true);
        } else {
            searchDebug(key);
        }
    }

    private void searchDebug(String key) {
        printLog(String.format("◆%s 搜索开始", getDoTime()));
        WebBookModel.getInstance().searchBook(Debug.SOURCE_DEBUG_TAG, key, 1)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.isEmpty()) {
                            printError("搜索列表为空");
                            printLog(String.format("◆%s 搜索结束", getDoTime()));
                        } else {
                            printLog("●成功获取搜索结果» 共" + searchBookBeans.size() + "个结果");
                            SearchBookBean searchBookBean = searchBookBeans.get(0);
                            printLog("●书籍名称» " + searchBookBean.getName());
                            printLog("●书籍作者» " + searchBookBean.getAuthor());
                            printLog("●书籍分类» " + searchBookBean.getKind());
                            printLog("●书籍简介» " + searchBookBean.getIntroduce());
                            printLog("●最新章节» " + searchBookBean.getDisplayLastChapter());
                            printLog("●书籍封面» " + searchBookBean.getCoverUrl());
                            printLog("●书籍网址» " + searchBookBean.getNoteUrl());
                            printLog(String.format("◆%s 搜索结束", getDoTime()));
                            if (!TextUtils.isEmpty(searchBookBean.getNoteUrl())) {
                                bookInfoDebug(BookshelfHelp.getBookFromSearchBook(searchBookBean), false);
                            } else {
                                printError("详情网址获取失败");
                                printLog(String.format("◆%s 搜索结束", getDoTime()));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                        printLog(String.format("◆%s 目录结束", getDoTime()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookInfoDebug(BookShelfBean bookShelfBean, boolean start) {
        if (!start) {
            printLog("\n");
        }
        printLog(String.format("◆%s 详情开始", getDoTime()));
        WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
                        printLog("●成功获取详情页» " + bookInfoBean.getNoteUrl());
                        printLog("●书籍名称» " + bookInfoBean.getName());
                        printLog("●书籍作者» " + bookInfoBean.getAuthor());
                        printLog("●书籍简介» " + bookInfoBean.getIntroduce());
                        printLog("●最新章节» " + bookShelfBean.getDisplayLastChapterName());
                        printLog("●书籍封面» " + bookInfoBean.getCoverUrl());
                        printLog("●目录网址» " + bookInfoBean.getChapterListUrl());
                        printLog(String.format("◆%s 详情结束", getDoTime()));
                        bookChapterListDebug(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        printError(e.getMessage());
                        printLog(String.format("◆%s 详情结束", getDoTime()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookChapterListDebug(BookShelfBean bookShelfBean) {
        printLog("\n");
        printLog(String.format("◆%s 目录开始", getDoTime()));
        WebBookModel.getInstance().getChapterList(bookShelfBean)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        int size = bookShelfBean.getChapterList().size();
                        if (size > 0) {
                            printLog("●成功获取目录列表» 共" + size + "个章节");
                            ChapterBean chapterBean = bookShelfBean.getChapter(size - 1);
                            printLog("●章节名称» " + chapterBean.getDisplayDurChapterName());
                            printLog("●章节网址» " + URLUtils.getAbsUrl(bookShelfBean.getBookInfoBean().getChapterListUrl(), chapterBean.getDurChapterUrl()));
                            printLog(String.format("◆%s 目录结束", getDoTime()));
                            bookContentDebug(bookShelfBean.getBookInfoBean(), chapterBean);
                        } else {
                            printError("获取到的目录为空");
                            printLog(String.format("◆%s 目录结束", getDoTime()));
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        printError(e.getMessage());
                        printLog(String.format("◆%s 目录结束", getDoTime()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookContentDebug(BookInfoBean bookInfoBean, ChapterBean chapterBean) {
        printLog("\n");
        printLog(String.format("◆%s 正文开始", getDoTime()));

        if (BookType.AUDIO.equals(bookInfoBean.getBookType())) {
            bookAudioDebug(bookInfoBean, chapterBean);
            return;
        }

        WebBookModel.getInstance().getBookContent(bookInfoBean, chapterBean)
                .subscribeOn(Schedulers.single())
                .timeout(30L, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookContentBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {
                        printLog("●成功获取正文页» " + URLUtils.getAbsUrl(bookInfoBean.getChapterListUrl(), bookContentBean.getDurChapterUrl()));
                        final String content = bookContentBean.getDurChapterContent();
                        if (content != null && content.length() > 6000) {
                            printLog("●章节内容» " + content.substring(0, 6000) + "\u00B7\u00B7\u00B7");
                        } else {
                            printLog("●章节内容» " + content);
                        }
                        printLog(String.format("◆%s 正文结束", getDoTime()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                        printLog(String.format("◆%s 正文结束", getDoTime()));
                    }

                    @Override
                    public void onComplete() {
                        finish();
                    }
                });
    }

    private void bookAudioDebug(BookInfoBean bookInfoBean, ChapterBean chapterBean) {
        WebBookModel.getInstance().getAudioBookContent(bookInfoBean, chapterBean)
                .subscribeOn(Schedulers.single())
                .timeout(30L, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChapterBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(ChapterBean chapterBean) {
                        printLog("●成功获取播放页» " + chapterBean.getDurChapterUrl());
                        printLog("●播放链接» " + chapterBean.getDurChapterPlayUrl());
                        printLog(String.format("◆%s 正文结束", getDoTime()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                        printLog(String.format("◆%s 正文结束", getDoTime()));
                    }

                    @Override
                    public void onComplete() {
                        finish();
                    }
                });
    }

    private void printLog(String log) {
        if (callBack != null) {
            callBack.printLog(log);
        }
    }

    private void printError(String msg) {
        if (callBack != null) {
            callBack.printError(String.format("%s%s", "●", msg));
        }
    }

    private void finish() {
        if (callBack != null) {
            callBack.finish();
        }
    }

    public interface CallBack {
        void printLog(String msg);

        void printError(String msg);

        void finish();
    }
}