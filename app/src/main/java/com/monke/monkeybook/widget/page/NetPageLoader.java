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

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {

    private Disposable mChapterDisp;

    public NetPageLoader(PageView pageView, BookShelfBean collBook) {
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
        if (mCollBook == null) return;

        if (mCollBook.getChapterList().size() > 0) {
            isChapterListPrepare = true;

            // 打开章节
            skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());

            // 目录加载完成，执行回调操作。
            if (mPageChangeListener != null) {
                mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
            }
        } else {
            if (mChapterDisp != null) {
                mChapterDisp.dispose();
            }
            WebBookModelImpl.getInstance().getChapterList(mCollBook)
                    .subscribeOn(Schedulers.single())
                    .doOnNext(bookShelfBean -> {
                        // 存储章节到数据库
                        bookShelfBean.setHasUpdate(false);
                        bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                        if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                            BookshelfHelp.saveBookToShelf(bookShelfBean);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(mPageView.getActivity().bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<BookShelfBean>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            mChapterDisp = d;
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            isChapterListPrepare = true;

                            // 加载并显示当前章节
                            skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());

                            // 提示目录加载完成
                            if (mPageChangeListener != null) {
                                mPageChangeListener.onCategoryFinish(bookShelfBean.getChapterList());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (NetworkUtil.isNetworkAvailable()) {
                                changePageStatus(STATUS_CATEGORY_EMPTY);
                            } else {
                                changePageStatus(STATUS_NETWORK_ERROR);
                            }
                        }
                    });
        }
    }

    @Override
    protected BufferedReader getChapterReader(ChapterListBean chapter) throws Exception {
        File file = BookshelfHelp.getBookFile(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                BookshelfHelp.getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()));
        if (!file.exists()) return null;
        Reader reader = new FileReader(file);
        return new BufferedReader(reader);
    }

    @Override
    protected boolean hasChapterData(ChapterListBean chapter) {
        return BookshelfHelp.isChapterCached(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                BookshelfHelp.getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()));
    }

    @Override
    boolean parsePrevChapter() {
        boolean isRight = super.parsePrevChapter();
        if (mPageChangeListener != null && mCurChapterPos >= 1 && shouldRequestChapter(mCurChapterPos - 1)) {
            mPageChangeListener.requestChapter(mCurChapterPos - 1);
        }
        return isRight;
    }

    @Override
    boolean parseCurChapter() {
        boolean isRight = super.parseCurChapter();
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos; i < mCurChapterPos + 5; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    mPageChangeListener.requestChapter(i);
                }
            }
        }
        return isRight;
    }

    @Override
    boolean parseNextChapter() {
        boolean isRight = super.parseNextChapter();
        if (mPageChangeListener != null) {
            for (int i = mCurChapterPos + 1; i < mCurChapterPos + 5; i++) {
                if (i < mCollBook.getChapterListSize() && shouldRequestChapter(i)) {
                    mPageChangeListener.requestChapter(i);
                }
            }
        }
        return isRight;
    }

    private boolean shouldRequestChapter(Integer chapterIndex) {
        return !hasChapterData(mCollBook.getChapter(chapterIndex));
    }
}

