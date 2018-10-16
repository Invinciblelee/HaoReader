package com.monke.monkeybook.widget.page;

import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {

    public NetPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
    }

    @Override
    public void refreshChapterList() {
        if (mCollBook == null) return;

        if (mCollBook.getChapterList().size() > 0) {
            isChapterListPrepare = true;

            // 如果章节未打开
            if (!isChapterOpen()) {
                // 打开章节
                skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());
            }
            // 目录加载完成，执行回调操作。
            if (mPageChangeListener != null) {
                mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
            }
        } else {
            WebBookModelImpl.getInstance().getChapterList(mCollBook)
                    .subscribeOn(Schedulers.io())
                    .compose(mPageView.getActivity().bindUntilEvent(ActivityEvent.DESTROY))
                    .doOnComplete(() -> {
                        // 存储章节到数据库
                        mCollBook.setHasUpdate(false);
                        mCollBook.setFinalRefreshData(System.currentTimeMillis());
                        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(mCollBook.getChapterList());
                        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplaceInTx(mCollBook);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            if (!isChapterListPrepare || getCurPageStatus() == STATUS_HY) {
                                isChapterListPrepare = true;

                                // 加载并显示当前章节
                                skipToChapter(mCollBook.getDurChapter(), mCollBook.getDurChapterPage());

                                // 提示目录加载完成
                                if (mPageChangeListener != null) {
                                    mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!isChapterListPrepare || getCurPageStatus() == STATUS_HY) {
                                setStatus(STATUS_CATEGORY_EMPTY);
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
            for (int i = mCurChapterPos + 1; i < mCurChapterPos + 6; i++) {
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

