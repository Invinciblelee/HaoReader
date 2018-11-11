package com.monke.monkeybook.widget.page;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.utils.NetworkUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CATEGORY_EMPTY;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CATEGORY_ERROR;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CONTENT_EMPTY;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_FINISH;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_LOADING;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_NETWORK_ERROR;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {

    private Disposable mChapterDisp;

    NetPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
    }

    @Override
    public void closeBook() {
        super.closeBook();
        if (mChapterDisp != null) {
            mChapterDisp.dispose();
            mChapterDisp = null;
        }
    }

    @Override
    public void refreshChapterList() {
        if (!getCollBook().realChapterListEmpty()) {
            setChapterListPrepared();

            // 打开章节
            skipToChapter(getCollBook().getDurChapter(), getCollBook().getDurChapterPage());

            // 目录加载完成，执行回调操作。
            dispatchCategoryFinishEvent(getCollBook().getChapterList());
        } else {
            if (mChapterDisp != null) {
                mChapterDisp.dispose();
            }
            WebBookModelImpl.getInstance().getChapterList(getCollBook())
                    .subscribeOn(Schedulers.newThread())
                    .doOnNext(bookShelfBean -> {
                        // 存储章节到数据库
                        bookShelfBean.setHasUpdate(false);
                        bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                        if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                            BookshelfHelp.saveBookToShelf(bookShelfBean);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(getActivity().bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<BookShelfBean>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            mChapterDisp = d;
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            if (bookShelfBean.realChapterListEmpty()) {
                                setCurrentStatus(STATUS_CATEGORY_EMPTY);
                            } else {
                                setChapterListPrepared();

                                // 加载并显示当前章节
                                skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());

                                // 提示目录加载完成
                                dispatchCategoryFinishEvent(bookShelfBean.getChapterList());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (NetworkUtil.isNetworkAvailable()) {
                                setCurrentStatus(STATUS_CATEGORY_ERROR);
                            } else {
                                setCurrentStatus(STATUS_NETWORK_ERROR);
                            }
                        }
                    });
        }
    }

    @Override
    BufferedReader getChapterReader(ChapterListBean chapter) throws Exception {
        File file = BookshelfHelp.getBookFile(BookshelfHelp.getCachePathName(getCollBook().getBookInfoBean()),
                BookshelfHelp.getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()));
        if (!file.exists()) return null;
        Reader reader = new FileReader(file);
        return new BufferedReader(reader);
    }

    @Override
    boolean hasChapterData(ChapterListBean chapter) {
        return BookshelfHelp.isChapterCached(BookshelfHelp.getCachePathName(getCollBook().getBookInfoBean()),
                BookshelfHelp.getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()));
    }

    @Override
    void dealLoadChapter(int chapterPos) {
        super.dealLoadChapter(chapterPos);
        if (getCurrentChapter().isEmpty()) {
            if (!NetworkUtil.isNetworkAvailable()) {
                setCurrentStatus(STATUS_NETWORK_ERROR, false);
            } else if (getCurrentStatus() != STATUS_FINISH && getCurrentStatus() != STATUS_CONTENT_EMPTY) {
                setCurrentStatus(STATUS_LOADING, false);
                getChapterProvider().loadChapterContent(chapterPos);
            }
        }
    }

    @Override
    void parsePrevChapter() {
        super.parsePrevChapter();
        if (getChapterPosition() >= 1 && shouldRequestChapter(getChapterPosition() - 1)) {
            getChapterProvider().loadChapterContent(getChapterPosition() - 1);
        }
    }

    @Override
    void parseCurChapter() {
        super.parseCurChapter();
        for (int i = getChapterPosition() >= 1 ? getChapterPosition() - 1 : getChapterPosition(); i < getChapterPosition() + 5; i++) {
            if (i < getCollBook().getChapterListSize() && shouldRequestChapter(i)) {
                getChapterProvider().loadChapterContent(i);
            }
        }
    }

    @Override
    void parseNextChapter() {
        super.parseNextChapter();
        for (int i = getChapterPosition() + 1; i < getChapterPosition() + 5; i++) {
            if (i < getCollBook().getChapterListSize() && shouldRequestChapter(i)) {
                getChapterProvider().loadChapterContent(i);
            }
        }
    }

    private boolean shouldRequestChapter(Integer chapterIndex) {
        return !hasChapterData(getCollBook().getChapter(chapterIndex));
    }
}

