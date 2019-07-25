package com.monke.monkeybook.widget.page;

import android.text.Layout;
import android.text.StaticLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.NetworkUtil;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.WebBookModel;
import com.monke.monkeybook.model.content.exception.BookSourceException;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.ObjectsCompat;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class ChapterProvider {

    private static final int THREAD_NUM = 6;

    private final DownloadList mDownloadingChapterList = new DownloadList(THREAD_NUM);

    private PageLoader mPageLoader;

    private Scheduler mScheduler;

    ChapterProvider(PageLoader pageLoader) {
        this.mPageLoader = pageLoader;
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
        final List<TxtPage> pages = new ArrayList<>();
        final TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex(), PageStatus.STATUS_FINISH);
        //使用流的方式加载
        final List<String> lines = new ArrayList<>();
        final BookShelfBean collBook = mPageLoader.getCollBook();
        final BookInfoBean bookInfo = collBook.getBookInfoBean();

        int rHeight = mPageLoader.getVisibleHeight();
        int titleLinesCount = 0;
        try {
            boolean showTitle = ReadBookControl.getInstance().getShowTitle(); // 是否展示标题
            String paragraph = chapter.getDisplayDurChapterName() + "\n"; //默认展示标题
            if (collBook.isLocalBook()) {
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
                        page.title = chapter.getDisplayDurChapterName();
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
                page.title = chapter.getDisplayDurChapterName();
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
            if (mPageLoader.chapterNotCached(chapter) && !contains(chapter.getDurChapterUrl())) {
                WebBookModel.getInstance().getBookContent(bookShelf.getBookInfoBean(), chapter)
                        .subscribeOn(getScheduler())
                        .doAfterNext(bookContentBean -> RxBus.get().post(RxBusTag.CHAPTER_CHANGE, bookContentBean))
                        .timeout(30, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<BookContentBean>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                                addDownloading(chapter.getDurChapterUrl(), d);
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
                                    } else if (e instanceof TimeoutException) {
                                        mPageLoader.setCurrentStatus(PageStatus.STATUS_CONTENT_TIMEOUT);
                                    } else if (e instanceof BookSourceException) {
                                        mPageLoader.setCurrentStatus(PageStatus.STATUS_SOURCE_NOT_FIND);
                                    } else {
                                        mPageLoader.setCurrentStatus(PageStatus.STATUS_CONTENT_ERROR);
                                    }
                                }
                            }
                        });
            }
        }
    }

    private synchronized void removeDownloading(String chapterUrl) {
        mDownloadingChapterList.remove(new ChapterDownloading(chapterUrl));
    }

    private synchronized boolean contains(String chapterUrl) {
        return mDownloadingChapterList.contains(new ChapterDownloading(chapterUrl));
    }

    private synchronized void addDownloading(String chapterUrl, Disposable disposable) {
        mDownloadingChapterList.add(new ChapterDownloading(chapterUrl, disposable));
    }

    private Scheduler getScheduler() {
        if (mScheduler == null) {
            mScheduler = RxExecutors.newScheduler(THREAD_NUM);
        }
        return mScheduler;
    }

    void stop() {
        for (ChapterDownloading downloading : mDownloadingChapterList) {
            downloading.dispose();
        }
        mDownloadingChapterList.clear();
        if (mScheduler != null) {
            mScheduler.shutdown();
            mScheduler = null;
        }
    }

    private static class ChapterDownloading {
        private String chapterUrl;
        private Disposable disposable;

        ChapterDownloading(String chapterUrl) {
            this.chapterUrl = chapterUrl;
        }

        ChapterDownloading(String chapterUrl, Disposable disposable) {
            this.chapterUrl = chapterUrl;
            this.disposable = disposable;
        }

        private void dispose() {
            if (disposable != null) {
                disposable.dispose();
            }
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof ChapterDownloading) {
                return Objects.equals(chapterUrl, ((ChapterDownloading) obj).chapterUrl);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return ObjectsCompat.hashCode(chapterUrl);
        }
    }


    private static class DownloadList extends LinkedList<ChapterDownloading> {
        //容量
        private int capacity;

        DownloadList(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public boolean add(ChapterDownloading e) {
            if (size() + 1 > capacity) {
                ChapterDownloading downloading = super.removeFirst();
                if (downloading != null) {
                    downloading.dispose();
                }
            }
            return super.add(e);
        }
    }
}