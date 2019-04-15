package com.monke.monkeybook.widget.page;

import android.text.Layout;
import android.text.StaticLayout;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.content.BookException;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class ChapterProvider {

    private final List<String> mDownloadingChapterList = new ArrayList<>();

    private CompositeDisposable mCompositeDisposable;

    private PageLoader mPageLoader;

    private ExecutorService mExecutor;
    private Scheduler mScheduler;

    ChapterProvider(PageLoader pageLoader) {
        this.mPageLoader = pageLoader;

        mExecutor = Executors.newFixedThreadPool(4);
        mScheduler = Schedulers.from(mExecutor);
    }

    /**
     * 加载章节
     */
    @NonNull
    TxtChapter provideChapter(int chapterPos) {
        // 获取章节
        ChapterBean chapter = mPageLoader.getCollBook().getChapter(chapterPos);
        // 判断章节是否存在
        if (mPageLoader.chapterNotCached(chapter)) {
            return new TxtChapter(chapterPos, !NetworkUtil.isNetworkAvailable() ? PageStatus.STATUS_NETWORK_ERROR : PageStatus.STATUS_LOADING);
        }

        // 获取章节的文本流
        try {
            BufferedReader reader = mPageLoader.getChapterReader(chapter);
            return loadChapter(chapter, reader);
        } catch (Exception e) {
            return new TxtChapter(chapterPos, PageStatus.STATUS_UNKNOWN_ERROR);
        }
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private TxtChapter loadChapter(ChapterBean chapter, BufferedReader br) {
        //生成的页面
        List<TxtPage> pages = new ArrayList<>();
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex(), PageStatus.STATUS_FINISH);
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        BookShelfBean collBook = mPageLoader.getCollBook();
        BookInfoBean bookInfo = collBook.getBookInfoBean();
        int rHeight = mPageLoader.getVisibleHeight();
        int titleLinesCount = 0;
        try {
            boolean showTitle = true; // 是否展示标题
            String paragraph = chapter.getDurChapterName() + "\n"; //默认展示标题
            if (collBook.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                br.readLine();
            }
            while (showTitle || (paragraph = br.readLine()) != null) {
                paragraph = ChapterContentHelp.replaceContent(bookInfo.getName(), bookInfo.getTag(), paragraph);
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", " ").trim();
                    // 如果只有换行符，那么就不执行
                    if (paragraph.equals("")) continue;
                    paragraph = StringUtils.halfToFull("  ") + paragraph + "\n";
                }
                int wordCount;
                String subStr;
                while (paragraph.length() > 0) {
                    //当前空间，是否容得下一行文字
                    if (showTitle) {
                        rHeight -= mPageLoader.getTitleTextSize();
                    } else {
                        rHeight -= mPageLoader.getTextSize();
                    }
                    // 一页已经填充满了，创建 TextPage
                    if (rHeight <= 0) {
                        // 创建Page
                        TxtPage page = new TxtPage();
                        page.position = pages.size();
                        page.title = chapter.getDurChapterName();
                        page.lines = new ArrayList<>(lines);
                        page.titleLines = titleLinesCount;
                        pages.add(page);
                        // 重置Lines
                        lines.clear();
                        rHeight = mPageLoader.getVisibleHeight();
                        titleLinesCount = 0;
                        continue;
                    }

                    //测量一行占用的字节数
                    if (showTitle) {
                        Layout tempLayout = new StaticLayout(paragraph, mPageLoader.getTitlePaint(), mPageLoader.getVisibleWidth(), Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                        wordCount = tempLayout.getLineEnd(0);
                    } else {
                        Layout tempLayout = new StaticLayout(paragraph, mPageLoader.getTextPaint(), mPageLoader.getVisibleWidth(), Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                        wordCount = tempLayout.getLineEnd(0);
                    }

                    subStr = paragraph.substring(0, wordCount);
                    if (!subStr.equals("\n")) {
                        //将一行字节，存储到lines中
                        lines.add(subStr);

                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount += 1;
                        }
                        rHeight -= mPageLoader.getTextInterval();
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount);
                }

                //增加段落的间距
                if (lines.size() != 0) {
                    rHeight = rHeight - mPageLoader.getTextPara() + mPageLoader.getTextInterval();
                }

                if (showTitle) {
                    showTitle = false;
                }
            }

            if (lines.size() != 0) {
                //创建Page
                TxtPage page = new TxtPage();
                page.position = pages.size();
                page.title = chapter.getDurChapterName();
                page.lines = new ArrayList<>(lines);
                page.titleLines = titleLinesCount;
                pages.add(page);
                //重置Lines
                lines.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
        }
        txtChapter.setTxtPages(pages);
        return txtChapter;
    }

    void loadChapterContent(int chapterIndex) {
        final BookShelfBean bookShelf = mPageLoader.getCollBook();
        if (NetworkUtil.isNetworkAvailable() && null != bookShelf && !bookShelf.realChapterListEmpty()) {
            final ChapterBean chapter = bookShelf.getChapter(chapterIndex);
            final ChapterBean nextChapter;
            if (chapterIndex < bookShelf.getChapterList().size() - 1) {
                nextChapter = bookShelf.getChapter(chapterIndex + 1);
            } else {
                nextChapter = null;
            }
            chapter.setNextChapterUrl(nextChapter == null ? null : nextChapter.getDurChapterUrl());
            if (mPageLoader.chapterNotCached(chapter) && addDownloading(chapter.getDurChapterUrl())) {
                WebBookModelImpl.getInstance().getBookContent(bookShelf.getBookInfoBean(), chapter)
                        .subscribeOn(mScheduler)
                        .doAfterNext(bookContentBean -> RxBus.get().post(RxBusTag.CHAPTER_CHANGE, bookContentBean))
                        .timeout(30, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<BookContentBean>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                                if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
                                    mCompositeDisposable = new CompositeDisposable();
                                }
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(BookContentBean bookContentBean) {
                                removeDownloading(bookContentBean.getDurChapterUrl());
                                if (chapterIndex == mPageLoader.getChapterPosition()) {
                                    mPageLoader.openChapterPage(bookShelf.getDurChapterPage());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                removeDownloading(chapter.getDurChapterUrl());
                                if (chapterIndex == mPageLoader.getChapterPosition()) {
                                    if (!NetworkUtil.isNetworkAvailable()) {
                                        mPageLoader.setCurrentStatus(PageStatus.STATUS_NETWORK_ERROR);
                                    } else if (e instanceof BookException) {
                                        mPageLoader.setCurrentErrorMsg(e.getMessage());
                                    } else {
                                        mPageLoader.setCurrentStatus(PageStatus.STATUS_UNKNOWN_ERROR);
                                    }
                                }
                            }
                        });
            }
        }
    }

    private synchronized void removeDownloading(String chapterUrl) {
        mDownloadingChapterList.remove(chapterUrl);
    }

    private synchronized boolean addDownloading(String chapterUrl) {
        if (!mDownloadingChapterList.contains(chapterUrl)) {
            return mDownloadingChapterList.add(chapterUrl);
        }
        return false;
    }

    void close() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
        mExecutor.shutdown();
    }
}