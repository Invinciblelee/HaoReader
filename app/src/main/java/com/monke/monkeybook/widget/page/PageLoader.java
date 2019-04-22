package com.monke.monkeybook.widget.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.widget.page.animation.Direction;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CATEGORY_EMPTY;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_FINISH;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_HY;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_HY_ERROR;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_LOADING;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_NETWORK_ERROR;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_PARING;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_PARSE_ERROR;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_PREPARE_CATEGORY;

/**
 * Created by newbiechen on 17-7-1.
 */

public abstract class PageLoader {
    // 默认的显示参数配置
    private static final int DEFAULT_TIP_SIZE = 18;
    private static final int DEFAULT_TIP_INTERVAL = 8;
    private static final int EXTRA_VERTICAL_SPACING = 8;
    private static final int BOTTOM_STATUS_HEIGHT = 24;

    private final Context mContext;
    // 页面显示类
    private final PageView mPageView;
    // 阅读器的配置选项
    private final ReadBookControl mSettingManager;
    // 章节提供者
    private final ChapterProvider mChapterProvider;

    // 书本对象
    private BookShelfBean mCollBook;
    // 监听器
    private OnPageChangeListener mPageChangeListener;

    // 上一章的页面列表缓存
    private TxtChapter mPreChapter;
    // 当前章节的页面列表
    private TxtChapter mCurChapter;
    // 下一章的页面列表缓存
    private TxtChapter mNextChapter;
    private TxtChapter.Intent mTargetIntent;

    // 绘制提示的画笔(章节名称和时间)
    private TextPaint mTipPaint;
    // 绘制标题的画笔
    private TextPaint mTitlePaint;
    // 绘制小说内容的画笔
    private TextPaint mTextPaint;


    private Disposable mPreLoadPrevDisposable;
    private Disposable mPreLoadNextDisposable;
    private Disposable mCurLoadDisposable;

    /*****************params**************************/
    // 判断章节列表是否加载完成
    private boolean isChapterListPrepare;

    // 是否已关闭书籍
    private boolean isClosed;
    // 页面的翻页效果模式
    private PageMode mPageMode;
    //书籍绘制区域的宽高
    private int mVisibleWidth;
    private int mVisibleHeight;
    //应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    //字体的颜色
    private int mTextColor;
    //字体的大小
    private int mTextSize;
    //行间距
    private int mTextInterval;
    //段落距离(基于行间距的额外距离)
    private int mTextPara;
    // 当前章
    private int mCurChapterPos = 0;
    //上一章的记录
    private int mLastChapterPos = 0;

    /*****************************init params*******************************/
    PageLoader(PageView pageView, BookShelfBean collBook) {
        mPageView = pageView;
        mContext = pageView.getContext();
        // 获取配置管理器
        mSettingManager = ReadBookControl.getInstance();
        // 章节提供者
        mChapterProvider = new ChapterProvider(this);

        // 初始化书籍
        initBook(collBook);
        // 初始化数据
        setData();
        //页面样式
        setPageStyle();
    }

    /**
     * 初始化书籍
     */
    private void initBook(BookShelfBean collBook) {
        mCollBook = collBook;
        mCurChapterPos = mCollBook.getDurChapter();
        mLastChapterPos = mCurChapterPos;
        mCurChapter = BitIntentDataManager.getInstance().getData("chapter", null);
        if (mCurChapter != null) {
            BitIntentDataManager.getInstance().cleanData("chapter");
            mCurChapter.rewind();
        } else {
            mCurChapter = new TxtChapter(mCurChapterPos, STATUS_LOADING);
        }
        if (mCollBook.realChapterListEmpty()) {
            mCurChapter.setStatus(STATUS_PREPARE_CATEGORY);
        }
    }

    private void setData() {
        // 配置文字有关的参数
        setTextParams();

        int topPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        int bottomPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom());
        int extraMargin = ScreenUtils.dpToPx(EXTRA_VERTICAL_SPACING);
        mMarginTop = mSettingManager.getHideStatusBar() ? topPadding + extraMargin
                : ScreenUtils.getStatusBarHeight() + topPadding + extraMargin;
        mMarginBottom = ScreenUtils.dpToPx(BOTTOM_STATUS_HEIGHT) + bottomPadding + extraMargin;
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());


        // 获取配置参数
        mPageMode = mSettingManager.getPageMode(mSettingManager.getPageMode());
        mPageView.resetPageAnim(mPageMode);
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private void setTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(mSettingManager.getTextSize());
        // 行间距
        mTextInterval = ScreenUtils.dpToPx(mSettingManager.getLineSpacing());
        // 段落间距
        mTextPara = ScreenUtils.dpToPx(mSettingManager.getParagraphSpacing());

        Typeface typeface;
        try {
            if (mSettingManager.getFontPath() != null) {
                typeface = Typeface.createFromFile(mSettingManager.getFontPath());
            } else {
                typeface = Typeface.SANS_SERIF;
            }
        } catch (Exception e) {
            ToastUtils.toast(mContext, "字体文件未找,到恢复默认字体");
            mSettingManager.setReadBookFont(null);
            typeface = Typeface.SANS_SERIF;
        }

        // 绘制提示的画笔
        mTipPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setTypeface(typeface);
        mTipPaint.setFakeBoldText(mSettingManager.getTextBold());
        mTipPaint.setSubpixelText(true);
        mTipPaint.setDither(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTextSize * 1.2f);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(typeface);
        mTitlePaint.setFakeBoldText(true);
        mTitlePaint.setSubpixelText(true);
        mTitlePaint.setDither(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setFakeBoldText(mSettingManager.getTextBold());
        mTextPaint.setSubpixelText(true);
        mTextPaint.setDither(true);
    }


    /**
     * 设置文字相关参数
     */
    public void setTextSize() {
        // 设置文字相关参数
        setTextParams();

        // 设置画笔的字体大小
        mTextPaint.setTextSize(mTextSize);
        // 设置标题的字体大小
        mTitlePaint.setTextSize(mTextSize * 1.2f);
        // 取消缓存
        mPreChapter = null;
        mNextChapter = null;

        reloadCurrentChapter();
    }

    /**
     * 设置页面样式
     */
    private void setPageStyle() {
        mSettingManager.initPageConfiguration();
        // 设置当前颜色样式
        mTextColor = mSettingManager.getTextColor();

        mTipPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTextColor);
        mTextPaint.setColor(mTextColor);
    }

    /**
     * 设置翻页动画
     */
    public void setPageMode(PageMode pageMode) {
        if (mPageMode != pageMode) {
            mPageMode = pageMode;
            mPageView.resetPageAnim(mPageMode);
            // 重新绘制当前页
            mPageView.drawPage();
        }
    }

    /**
     * 设置内容与屏幕的间距 单位为 px
     */
    public void setMargin() {
        // 行间距
        mTextInterval = ScreenUtils.dpToPx(mSettingManager.getLineSpacing());
        // 段落间距
        mTextPara = ScreenUtils.dpToPx(mSettingManager.getParagraphSpacing());

        int topPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        int bottomPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom());
        int extraMargin = ScreenUtils.dpToPx(EXTRA_VERTICAL_SPACING);
        mMarginTop = mSettingManager.getHideStatusBar() ? topPadding + extraMargin
                : ScreenUtils.getStatusBarHeight() + topPadding + extraMargin;
        mMarginBottom = ScreenUtils.dpToPx(BOTTOM_STATUS_HEIGHT) + bottomPadding + extraMargin;
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());

        setDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 设置背景
     */
    public void setBackground() {
        setPageStyle();
        mPageView.drawPage();
    }

    /**
     * 屏幕大小变化处理
     */
    void setDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginLeft - mMarginRight;
        mVisibleHeight = mDisplayHeight - mMarginTop - mMarginBottom;

        mPageView.resetPageAnim(mPageMode);

        if (mCurChapter.isNotOpened()) {
            mPageView.drawPage();
        } else {
            reloadCurrentChapter();
        }
    }

    /**
     * 刷新界面
     */
    public void refreshUI() {
        setData();
        setPageStyle();

        setDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 刷新当前章节
     */
    public void refreshDurChapter() {
        int status = getCurrentStatus();
        if (status == STATUS_LOADING
                || status == STATUS_HY) {
            return;
        }

        if (!isChapterListPrepare) {
            setCurrentStatus(STATUS_PREPARE_CATEGORY);
            refreshChapterList();
            return;
        }

        mCurChapter.reset();

        BookshelfHelp.delChapter(BookshelfHelp.getCacheFolderPath(mCollBook.getBookInfoBean()),
                BookshelfHelp.getCacheFileName(mCollBook.getChapter(mCurChapterPos)));
        skipToChapter(mCurChapterPos);
    }

    /**
     * 换源结束
     */
    public void changeSourceFinish(BookShelfBean bookShelfBean) {
        if (bookShelfBean == null) {
            setCurrentStatus(STATUS_HY_ERROR);
            return;
        }
        mCollBook = bookShelfBean;
        mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
        if (!isChapterListPrepare) {
            isChapterListPrepare = true;
        }
        mCurChapter.reset();
        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
    }

    /**
     * 跳转到上一章
     */
    public void skipPreChapter() {
        if (isFirstChapter()) {
            return;
        }

        // 载入上一章。
        parsePrevChapter(() -> {
            mCurChapter.setPosition(0);
            mPageView.drawPage();
            dispatchPagingEndEvent();
        });
    }

    /**
     * 跳转到下一章
     */
    public void skipNextChapter() {
        if (!hasNextChapter()) {
            return;
        }

        parseNextChapter(() -> {
            mPageView.drawPage();
            dispatchPagingEndEvent();
        });
    }

    /**
     * 跳转到指定章节页
     */
    public void skipToChapter(int chapterPos, int pagePos) {
        // 设置参数
        mCurChapterPos = chapterPos;

        cancelPreload();

        // 将上一章的缓存设置为null
        mPreChapter = null;

        // 将下一章缓存设置为null
        mNextChapter = null;

        mTargetIntent = null;

        // 打开指定章节
        openChapterPage(pagePos);
    }

    /**
     * 跳转到指定章节
     *
     * @param chapterPos:从 0 开始。
     */
    private void skipToChapter(int chapterPos) {
        skipToChapter(chapterPos, 0);
    }

    /**
     * 跳转到指定的页
     */
    public void skipToPage(int pos) {
        if (!isChapterListPrepare || mCurChapter.isEmpty()) {
            return;
        }
        mCurChapter.setPosition(pos);
        mPageView.drawPage();
        dispatchPagingEndEvent();
    }

    /**
     * 翻到上一页
     */
    public void skipToPrePage() {
        mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public void skipToNextPage() {
        mPageView.autoNextPage();
    }

    /**
     * 翻到下一页,无动画
     */
    public boolean noAnimationToNextPage() {
        if (mCurChapter.hasNext()) {
            skipToPage(mCurChapter.getPosition() + 1);
            return true;
        }
        if (hasNextChapter()) {
            skipNextChapter();
            return true;
        }
        return false;
    }

    /**
     * 设置页面切换监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
        }
    }

    private void dispatchChapterChangeEvent() {
        mCollBook.setDurChapter(mCurChapterPos);
        if (mPageChangeListener != null) {
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(mCurChapter.size());
        }
    }

    /**
     * 翻页完成
     */
    void dispatchPagingEndEvent() {
        mPageView.setContentDescription(getCurrentContent());
        mPageView.upPagePos(mCurChapterPos, mCurChapter.getPosition());
        mCollBook.setDurChapter(mCurChapterPos);
        mCollBook.setDurChapterPage(mCurChapter.getPosition());
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(mCurChapterPos, mCurChapter.getPosition(), mCurChapter.size());
        }
    }

    void dispatchCategoryFinishEvent(List<ChapterBean> chapterList) {
        if (mPageChangeListener != null) {
            mPageChangeListener.onCategoryFinish(chapterList);
        }
    }

    /**
     * 重新加载当前页面数据
     */
    private void reloadCurrentChapter() {
        if (mCurChapter.isEmpty()) {
            mPageView.drawPage();

            preload();
        } else {
            cancelPreload();
            // 将上一章的缓存设置为null
            mPreChapter = null;
            // 将下一章缓存设置为null
            mNextChapter = null;

            final int temp = mCurChapter.getPosition();
            // 重新计算当前页面
            dealLoadChapter(mCurChapterPos, () -> {
                mCurChapter.setPosition(temp);
                mPageView.drawPage();
                dispatchPagingEndEvent();

                preload();
            });
        }
    }

    /**
     * 打开指定章节
     */
    synchronized void openChapterPage(int pagePos) {
        if (!mPageView.isLayoutPrepared()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            setCurrentStatus(STATUS_LOADING);
            return;
        }

        // 如果获取到的章节目录为空
        if (mCollBook.realChapterListEmpty()) {
            setCurrentStatus(STATUS_CATEGORY_EMPTY);
            return;
        }

        dealCurPage(pagePos);
    }

    private void dealCurPage(int pagePos) {
        parseCurChapter(() -> {
            if (!mCurChapter.isEmpty()) {
                if (mTargetIntent != null) {
                    mCurChapter.setIntent(mTargetIntent);
                    mTargetIntent = null;
                } else {
                    mCurChapter.setPosition(pagePos);
                }
            }

            mPageView.drawPage();
            if (!mPageView.isRunning()) {
                dispatchPagingEndEvent();
            }
        });
    }

    public boolean isPageScrollable() {
        int status = getCurrentStatus();
        return isChapterListPrepare
                && status != STATUS_CATEGORY_EMPTY
                && status != STATUS_PARING
                && status != STATUS_PARSE_ERROR
                && status != STATUS_HY;
    }


    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    public void stopRefreshChapterList() {

    }

    /**
     * 获取章节的文本流
     */
    abstract BufferedReader getChapterReader(ChapterBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     */
    abstract boolean chapterNotCached(ChapterBean chapter);

    /**
     * 绘制页面
     */
    void drawPage(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        drawContent(bitmap);
        //更新绘制
        mPageView.postInvalidate();
    }

    private void preDrawNextPage() {
        Bitmap bitmap = mPageView.getNextBitmap();
        if (bitmap != null && !bitmap.isRecycled()) {
            Canvas canvas = new Canvas(bitmap);
            drawBackground(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (mSettingManager.bgIsColor()) {
            canvas.drawColor(mSettingManager.getBgColor());
        } else {
            Bitmap bgBitmap = mSettingManager.getBgBitmap();
            if (bgBitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postScale((float) canvas.getWidth() / bgBitmap.getWidth(),
                        (float) canvas.getHeight() / bgBitmap.getHeight());
                canvas.drawBitmap(bgBitmap, matrix, null);
            } else {
                canvas.drawColor(mSettingManager.getDefaultBgColor());
            }
        }
    }


    /**
     * 绘制内容
     */
    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        drawBackground(canvas);

        if (getCurrentStatus() != STATUS_FINISH) {
            //绘制字体
            String tip = mCurChapter.getErrorMsg();
            //将提示语句放到正中间
            Layout tempLayout = new StaticLayout(tip, mTipPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesData = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesData.add(tip.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }

            final Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
            final float interval = ScreenUtils.dpToPx(DEFAULT_TIP_INTERVAL) + mTipPaint.getTextSize();
            final float textHeight = fontMetrics.bottom - fontMetrics.top;
            float pivotY = (mDisplayHeight - (textHeight + interval) * linesData.size()) / 3;
            for (String str : linesData) {
                float textWidth = mTipPaint.measureText(str);
                float pivotX = (mDisplayWidth - textWidth) / 2;
                canvas.drawText(str, pivotX, pivotY, mTipPaint);
                pivotY += interval;
            }
        } else {//绘制内容
            mCurChapter.open();

            TxtPage txtPage = mCurChapter.getCurrentPage();
            if (txtPage == null || txtPage.lines == null) {
                return;
            }

            final float interval = mTextInterval + mTextPaint.getTextSize();
            final float para = mTextPara + mTextPaint.getTextSize();
            final Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

            float top = mMarginTop - fontMetrics.ascent;

            //对标题进行绘制
            for (int i = 0; i < txtPage.titleLines; i++) {
                String line = txtPage.lines.get(i);

                float start = (mDisplayWidth - mTitlePaint.measureText(line)) / 2;
                //进行绘制
                canvas.drawText(line, start, top, mTitlePaint);

                //设置尾部间距
                top += i == txtPage.titleLines - 1 ? para : interval;
            }

            //对内容进行绘制
            for (int i = txtPage.titleLines, size = txtPage.lines.size(); i < size; i++) {
                String line = txtPage.lines.get(i);
                if (needScale(line)) {
                    Layout tempLayout = new StaticLayout(line, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    float width = StaticLayout.getDesiredWidth(line, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                    drawScaledText(canvas, line, width, mTextPaint, top);
                } else {
                    canvas.drawText(line, mMarginLeft, top, mTextPaint);
                }
                //设置尾部间距
                top += line.endsWith("\n") ? para : interval;
            }
        }
    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth, TextPaint paint, float top) {
        float x = mMarginLeft;
        if (isFirstLineOfParagraph(line)) {
            String blanks = StringUtils.halfToFull("  ");
            canvas.drawText(blanks, x, top, paint);
            float bw = StaticLayout.getDesiredWidth(blanks, paint);
            x += bw;
            line = line.substring(2);
        }
        int lineLength = line.length();
        int gapCount = lineLength - 1;
        float d = ((mDisplayWidth - (mMarginLeft + mMarginRight)) - lineWidth) / gapCount;
        for (int i = 0; i < lineLength; i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, paint);
            canvas.drawText(c, x, top, paint);
            x += cw + d;
        }
    }

    /**
     * 翻阅上一页
     */
    boolean prevPage() {
        // 以下情况禁止翻页
        if (isPageFrozen()) {
            return false;
        }

        if (getCurrentStatus() == STATUS_FINISH) {
            // 先查看是否存在上一页
            if (mCurChapter.prevPage()) {
                mPageView.drawPage(true);
                return true;
            }
        }

        if (isFirstChapter()) {
            return false;
        }

        mPageView.changePage();

        parsePrevChapter(mPageView::drawPage);
        return true;
    }

    /**
     * 判断是否上一章节为空
     */
    private boolean isFirstChapter() {
        return mCurChapterPos - 1 < 0;
    }

    /**
     * 翻到下一页
     */
    boolean nextPage() {
        // 以下情况禁止翻页
        if (isPageFrozen()) {
            return false;
        }

        if (getCurrentStatus() == STATUS_FINISH) {
            // 先查看是否存在下一页
            if (mCurChapter.nextPage()) {
                mPageView.drawPage(true);
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mPageView.changePage();

        // 解析下一章数据
        parseNextChapter(mPageView::drawPage);
        return true;
    }

    /**
     * 判断是否到达目录最后一章
     */
    private boolean hasNextChapter() {
        return mCurChapterPos + 1 < mCollBook.getChapterListSize();
    }

    /**
     * 解析上一章数据
     */
    void parsePrevChapter(OnChapterPreparedCallback callback) {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        if (isFirstChapter()) {
            return;
        }

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        mNextChapter = mCurChapter;

        // 判断是否具有上一章缓存
        if (mPreChapter != null && mPreChapter.getStatus() == STATUS_FINISH) {
            mCurChapter = mPreChapter;
            mPreChapter = null;
            mCurChapter.setPosition(mCurChapter.size() - 1);
            // 回调
            dispatchChapterChangeEvent();
            if (callback != null) {
                callback.onChapterPrepared();
            }
        } else {
            preDrawNextPage(); //由于异步加载，先绘制一下背景。否则可能会出现黑屏
            //异步加载章节
            dealLoadChapter(prevChapter, () -> {
                if (!mCurChapter.isEmpty()) {
                    mCurChapter.setPosition(mCurChapter.size() - 1);
                } else {
                    mTargetIntent = TxtChapter.Intent.PREV;
                }

                if (callback != null) {
                    callback.onChapterPrepared();
                }
            });
        }

        //预加载上一章
        preloadPrevChapter();
    }

    /**
     * 解析数据
     */
    void parseCurChapter(OnChapterPreparedCallback callback) {
        if (mCurChapter.getId() == mCurChapterPos
                && !mCurChapter.isEmpty()
                && mCurChapter.getStatus() != STATUS_NETWORK_ERROR) {
            if (callback != null) {
                callback.onChapterPrepared();
            }
            preload();
        } else {
            setCurrentStatus(STATUS_LOADING);
            dealLoadChapter(mCurChapterPos, () -> {
                if (callback != null) {
                    callback.onChapterPrepared();
                }

                preload();
            });
        }
    }

    /**
     * 解析下一章数据
     */
    void parseNextChapter(OnChapterPreparedCallback callback) {
        int nextChapter = mCurChapterPos + 1;

        if (!hasNextChapter()) {
            return;
        }

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        mPreChapter = mCurChapter;

        // 是否下一章数据已经预加载了
        if (mNextChapter != null && mNextChapter.getStatus() == STATUS_FINISH) {
            mCurChapter = mNextChapter;
            mNextChapter = null;
            mCurChapter.setPosition(0);
            // 回调
            dispatchChapterChangeEvent();
            if (callback != null) {
                callback.onChapterPrepared();
            }
        } else {
            preDrawNextPage();//由于异步加载，先绘制一下背景。否则可能会出现黑屏
            //异步加载章节
            dealLoadChapter(nextChapter, () -> {
                if (!mCurChapter.isEmpty()) {
                    mCurChapter.setPosition(0);
                } else {
                    mTargetIntent = TxtChapter.Intent.NEXT;
                }
                if (callback != null) {
                    callback.onChapterPrepared();
                }
            });
        }

        // 预加载下一页面
        preloadNextChapter();
    }

    void dealLoadChapter(int chapterPos, OnChapterPreparedCallback callback) {
        if (mCurLoadDisposable != null) {
            mCurLoadDisposable.dispose();
        }

        Single.create((SingleOnSubscribe<TxtChapter>) emitter ->
                emitter.onSuccess(mChapterProvider.provideChapter(chapterPos))).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCurLoadDisposable = d;
                    }

                    @Override
                    public void onSuccess(TxtChapter chapter) {
                        mCurChapter = chapter;
                        // 回调
                        dispatchChapterChangeEvent();

                        if (callback != null) {
                            callback.onChapterPrepared();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    /**
     * 预加载下一章
     */
    private void preloadNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，或下一章没有数据，则不进行加载。
        if (nextChapter >= mCollBook.getChapterListSize()
                || chapterNotCached(mCollBook.getChapter(nextChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadNextDisposable != null) {
            mPreLoadNextDisposable.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<TxtChapter>) e -> e.onSuccess(mChapterProvider.provideChapter(nextChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadNextDisposable = d;
                    }

                    @Override
                    public void onSuccess(TxtChapter chapter) {
                        if (mCurChapterPos + 1 == nextChapter) {
                            mNextChapter = chapter;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                    }
                });
    }

    /**
     * 预加载上一章
     */
    private void preloadPrevChapter() {
        int prevChapter = mCurChapterPos - 1;

        // 如果不存在上一章，或上一章没有数据，则不进行加载。
        if (prevChapter < 0
                || chapterNotCached(mCollBook.getChapter(prevChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadPrevDisposable != null) {
            mPreLoadPrevDisposable.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<TxtChapter>) e -> e.onSuccess(mChapterProvider.provideChapter(prevChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<TxtChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadPrevDisposable = d;
                    }

                    @Override
                    public void onSuccess(TxtChapter chapter) {
                        if (mCurChapterPos - 1 == prevChapter) {
                            mPreChapter = chapter;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                    }
                });
    }

    private void preload() {
        // 预加载上一页面
        preloadPrevChapter();
        // 预加载下一页面
        preloadNextChapter();
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private boolean isPageFrozen() {
        if (!isChapterListPrepare) {
            return true;
        }

        return getCurrentStatus() == STATUS_PARSE_ERROR || getCurrentStatus() == STATUS_PARING;
    }

    private void cancelPreload() {
        //如果之前正在加载则取消
        if (mPreLoadPrevDisposable != null) {
            mPreLoadPrevDisposable.dispose();
            mPreLoadPrevDisposable = null;
        }

        //如果之前正在加载则取消
        if (mPreLoadNextDisposable != null) {
            mPreLoadNextDisposable.dispose();
            mPreLoadNextDisposable = null;
        }

        if (mCurLoadDisposable != null) {
            mCurLoadDisposable.dispose();
            mCurLoadDisposable = null;
        }
    }

    /**
     * 取消翻页
     */
    void pageCancel() {
        if (mCurChapterPos > mLastChapterPos
                && mPageView.getAnimDirection() == Direction.NEXT
                && mCurChapter.getLastPosition() == -1) { // 加载到下一章取消了
            if (mPreChapter != null) {
                cancelNextChapter();
                mPageView.postDrawPage();
            } else {
                parsePrevChapter(mPageView::postDrawPage);
            }
        } else if (mCurChapterPos < mLastChapterPos
                && mPageView.getAnimDirection() == Direction.PREV
                && mCurChapter.getLastPosition() == -1) {  // 加载上一章取消了
            if (mNextChapter != null) {
                cancelPreChapter();
                mPageView.postDrawPage();
            } else {
                parseNextChapter(mPageView::postDrawPage);
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            if (mCurChapter.pageCancel()) {
                mPageView.postDrawPage();
            }
        }
    }

    private void cancelNextChapter() {
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        mNextChapter = mCurChapter;
        mCurChapter = mPreChapter;
        mPreChapter = null;

        mCurChapter.setPosition(mCurChapter.size() - 1);

        dispatchChapterChangeEvent();
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        // 重置页面列表
        mPreChapter = mCurChapter;
        mCurChapter = mNextChapter;
        mNextChapter = null;

        mCurChapter.setPosition(0);

        dispatchChapterChangeEvent();
    }

    //判断是不是d'hou
    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == (char) 12288 && line.charAt(1) == (char) 12288;
    }

    private boolean needScale(String line) {//判断不是空行
        return !TextUtils.isEmpty(line) && !line.endsWith("\n");
    }

    public String getContent(int pagePos) {
        return mCurChapter.getContent(pagePos);
    }

    public String getCurrentContent() {
        return mCurChapter.getContent(mCurChapter.getPosition());
    }

    public void setCurrentStatus(int status) {
        setCurrentStatus(status, true);
    }

    void setCurrentErrorMsg(String msg) {
        mCurChapter.setErrorMsg(msg);
        mPageView.drawPage();
    }

    void setCurrentStatus(int status, boolean notify) {
        if (mCurChapter.getStatus() != status) {
            mCurChapter.setStatus(status);
            if (notify) {
                mPageView.drawPage();
            }
        }
    }

    int getCurrentStatus() {
        return mCurChapter.getStatus();
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean hasCurrentChapter() {
        return !mCollBook.realChapterListEmpty() && mCurChapter != null && mCurChapter.size() != 0;
    }

    public boolean isChapterListPrepare() {
        return isChapterListPrepare;
    }

    public int getChapterPosition() {
        return mCurChapterPos;
    }

    PageView getPageView() {
        return mPageView;
    }

    ReadBookActivity getActivity() {
        return mPageView.getActivity();
    }

    ChapterProvider getChapterProvider() {
        return mChapterProvider;
    }

    int getPagePosition() {
        return mCurChapter.getPosition();
    }

    public TxtChapter getCurrentChapter() {
        return mCurChapter;
    }

    TxtChapter getPreChapter() {
        return mPreChapter;
    }

    TxtChapter getNextChapter() {
        return mNextChapter;
    }

    void setChapterListPrepared() {
        isChapterListPrepare = true;
    }

    int getVisibleWidth() {
        return mVisibleWidth;
    }

    int getVisibleHeight() {
        return mVisibleHeight;
    }

    TextPaint getTextPaint() {
        return mTextPaint;
    }

    TextPaint getTitlePaint() {
        return mTitlePaint;
    }

    int getTextInterval() {
        return mTextInterval;
    }

    int getTextPara() {
        return mTextPara;
    }

    float getTextSize() {
        return mTextPaint.getTextSize();
    }

    float getTitleTextSize() {
        return mTitlePaint.getTextSize();
    }

    BookShelfBean getCollBook() {
        return mCollBook;
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClosed = true;

        cancelPreload();

        mPreChapter = null;
        mCurChapter = null;
        mNextChapter = null;

        mChapterProvider.close();
    }

    interface OnChapterPreparedCallback {
        void onChapterPrepared();
    }
}