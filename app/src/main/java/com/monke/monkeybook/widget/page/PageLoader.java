package com.monke.monkeybook.widget.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Created by newbiechen on 17-7-1.
 */

public abstract class PageLoader {
    // 当前页面的状态
    public static final int STATUS_LOADING = 1;         // 正在加载
    public static final int STATUS_FINISH = 2;          // 加载完成
    public static final int STATUS_UNKNOWN_ERROR = 3;   // 未知错误
    public static final int STATUS_NETWORK_ERROR = 4;   // 网络错误
    public static final int STATUS_EMPTY = 5;           // 空数据
    public static final int STATUS_PARING = 6;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 7;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 8;  // 获取到的目录为空
    public static final int STATUS_HY = 9;              // 换源
    public static final int STATUS_HY_ERROR = 10;       // 换源失败
    // 默认的显示参数配置
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TOP_MARGIN = 8;

    // 书本对象
    protected BookShelfBean mCollBook;
    // 监听器
    protected OnPageChangeListener mPageChangeListener;

    private Context mContext;
    // 页面显示类
    protected PageView mPageView;
    // 当前显示的页
    private TxtPage mCurPage;
    // 上一章的页面列表缓存
    private List<TxtPage> mPrePageList;
    // 当前章节的页面列表
    private List<TxtPage> mCurPageList;
    // 下一章的页面列表缓存
    private List<TxtPage> mNextPageList;

    // 绘制提示的画笔(章节名称和时间)
    private TextPaint mTipPaint;
    // 绘制标题的画笔
    private TextPaint mTitlePaint;
    // 绘制小说内容的画笔
    private TextPaint mTextPaint;
    // 阅读器的配置选项
    private ReadBookControl mSettingManager;
    // 被遮盖的页，或者认为被取消显示的页
    private TxtPage mCancelPage;


    private Disposable mPreLoadPrevDisposable;
    private Disposable mPreLoadNextDisposable;

    /*****************params**************************/
    // 当前的状态
    protected SparseIntArray mStatus = new SparseIntArray();
    // 判断章节列表是否加载完成
    protected boolean isChapterListPrepare;

    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean isFirstOpen = true;
    private boolean isClose;
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
    protected int mCurChapterPos = 0;
    //上一章的记录
    private int mLastChapterPos = 0;
    private int goPagePos = 0;

    /*****************************init params*******************************/
    public PageLoader(PageView pageView, BookShelfBean collBook) {
        mPageView = pageView;
        mContext = pageView.getContext();

        // 初始化书籍
        prepareBook(collBook);
        // 初始化数据
        initData();
        // 初始化画笔
        initPaint();
        // 初始化PageView
        initPageView();
    }

    private void initData() {
        // 获取配置管理器
        mSettingManager = ReadBookControl.getInstance();
        // 获取配置参数
        mPageMode = mSettingManager.getPageMode(mSettingManager.getPageMode());

        int topPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        int bottomPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom());
        int extraMargin = ScreenUtils.dpToPx(EXTRA_TOP_MARGIN);
        mMarginTop = mSettingManager.getHideStatusBar() ? topPadding + extraMargin
                : Math.max(ScreenUtils.getStatusBarHeight(), topPadding) + extraMargin;
        mMarginBottom = ScreenUtils.getStatusBarHeight() + bottomPadding;
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());
        // 配置文字有关的参数
        setUpTextParams();
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private void setUpTextParams() {
        // 文字大小
        mTextSize = ScreenUtils.spToPx(mSettingManager.getTextSize());
        // 行间距
        mTextInterval = ScreenUtils.dpToPx(mSettingManager.getLineSpacing());
        // 段落间距
        mTextPara = ScreenUtils.dpToPx(mSettingManager.getParagraphSpacing());
    }

    public void initPaint() {
        Typeface typeface;
        try {
            if (mSettingManager.getFontPath() != null) {
                typeface = Typeface.createFromFile(mSettingManager.getFontPath());
            } else {
                typeface = Typeface.SANS_SERIF;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "字体文件未找,到恢复默认字体", Toast.LENGTH_SHORT).show();
            mSettingManager.setReadBookFont(null);
            typeface = Typeface.SANS_SERIF;
        }

        // 绘制提示的画笔
        mTipPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        mTipPaint.setTextSize(ScreenUtils.spToPx(DEFAULT_TIP_SIZE)); // Tip默认的字体大小
        mTipPaint.setSubpixelText(true);

        // 绘制标题的画笔
        mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTextSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(typeface);
        mTitlePaint.setFakeBoldText(true);

        // 绘制页面内容的画笔
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setFakeBoldText(mSettingManager.getTextBold());

        setPageStyle(true);
    }

    private void initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode);
    }

    /**
     * 刷新界面
     */
    public void refreshUi() {
        initData();
        initPaint();
        setPageStyle(false);
        initPageView();

        prepareDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    public void refreshDurChapter() {
        int status = getCurPageStatus();
        if (status == STATUS_LOADING
                || status == STATUS_HY) {
            return;
        }

        if (!isChapterListPrepare) {
            changePageStatus(STATUS_LOADING);
            refreshChapterList();
            return;
        }

        changePageStatus(STATUS_LOADING);

        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(mCollBook.getBookInfoBean()),
                BookshelfHelp.getCacheFileName(mCurChapterPos, mCollBook.getChapter(mCurChapterPos).getDurChapterName()));
        skipToChapter(mCurChapterPos);
    }

    /**
     * 换源结束
     */
    public void changeSourceFinish(BookShelfBean bookShelfBean) {
        if (bookShelfBean == null) {
            return;
        }
        mCollBook = bookShelfBean;
        mPageChangeListener.onCategoryFinish(mCollBook.getChapterList());
        if (!isChapterListPrepare) {
            isChapterListPrepare = true;
        }
        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
    }

    /**
     * 跳转到上一章
     */
    public boolean skipPreChapter() {
        goPagePos = 0;
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        pagingEnd();
        mPageView.drawCurPage();
        return true;
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        goPagePos = 0;
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            mCurPage = getCurPage(0);
        } else {
            mCurPage = new TxtPage();
        }
        pagingEnd();
        mPageView.drawCurPage();
        return true;
    }

    /**
     * 跳转到指定章节页
     */
    public void skipToChapter(int chapterPos, int pagePos) {
        goPagePos = pagePos;
        // 设置参数
        mCurChapterPos = chapterPos;

        cancelPreload();

        // 将上一章的缓存设置为null
        mPrePageList = null;

        // 将下一章缓存设置为null
        mNextPageList = null;

        mStatus.clear();

        // 打开指定章节
        openChapter(pagePos);
    }

    /**
     * 跳转到指定章节
     *
     * @param chapterPos:从 0 开始。
     */
    public void skipToChapter(int chapterPos) {
        skipToChapter(chapterPos, 0);
    }

    /**
     * 跳转到指定的页
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare) {
            return false;
        }
        mCurPage = getCurPage(pos);
        mPageView.drawCurPage();
        pagingEnd();
        return true;
    }

    /**
     * 翻到上一页
     */
    public boolean skipToPrePage() {
        return mPageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public boolean skipToNextPage() {
        return mPageView.autoNextPage();
    }

    /**
     * 翻到下一页,无动画
     */
    public boolean noAnimationToNextPage() {
        if (getPagePos() < getPageSize() - 1) {
            skipToPage(getPagePos() + 1);
            return true;
        }
        return skipNextChapter();
    }

    /**
     * 翻页完成
     */
    public void pagingEnd() {
        mPageView.setContentDescription(getContent(getPagePos()));
        mPageView.upPagePos(mCurChapterPos, getPagePos());
        mCollBook.setDurChapter(mCurChapterPos);
        mCollBook.setDurChapterPage(getPagePos());
        mPageChangeListener.onPageChange(mCurChapterPos, getPagePos(), getPageSize());
    }

    /**
     * 设置文字相关参数
     */
    public void setTextSize() {
        // 设置文字相关参数
        setUpTextParams();

        // 设置画笔的字体大小
        mTextPaint.setTextSize(mTextSize);
        // 设置标题的字体大小
        mTitlePaint.setTextSize(mTextSize);
        // 取消缓存
        mPrePageList = null;
        mNextPageList = null;

        reloadCurPageData();

        mPageView.drawCurPage();
    }

    /**
     * 设置页面样式
     */
    public void setPageStyle(boolean isPreview) {
        mSettingManager.initPageConfiguration();
        // 设置当前颜色样式
        mTextColor = mSettingManager.getTextColor();

        mTipPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTextColor);
        mTextPaint.setColor(mTextColor);

        mPageView.drawCurPage(isPreview);
    }

    /**
     * 设置翻页动画
     */
    public void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;

        mPageView.setPageMode(mPageMode);

        // 重新绘制当前页
        mPageView.drawCurPage();
    }

    /**
     * 设置内容与屏幕的间距 单位为 px
     */
    public void upMargin() {
        int topPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingTop());
        int bottomPadding = ScreenUtils.dpToPx(mSettingManager.getPaddingBottom());
        int extraMargin = ScreenUtils.dpToPx(EXTRA_TOP_MARGIN);
        mMarginTop = mSettingManager.getHideStatusBar() ? topPadding + extraMargin
                : Math.max(ScreenUtils.getStatusBarHeight(), topPadding) + extraMargin;
        mMarginBottom = ScreenUtils.getStatusBarHeight() + bottomPadding;
        mMarginLeft = ScreenUtils.dpToPx(mSettingManager.getPaddingLeft());
        mMarginRight = ScreenUtils.dpToPx(mSettingManager.getPaddingRight());

        prepareDisplay(mDisplayWidth, mDisplayHeight);
    }

    /**
     * 屏幕大小变化处理
     */
    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginLeft - mMarginRight;
        mVisibleHeight = mDisplayHeight - mMarginTop - mMarginBottom;

        // 重置 PageMode
        mPageView.setPageMode(mPageMode);

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(isChapterListPrepare);
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter(mCollBook.getDurChapterPage());
            }
        } else {
            reloadCurPageData();

            mPageView.drawCurPage();
        }
    }

    /**
     * 初始化书籍
     */
    private void prepareBook(BookShelfBean collBook) {
        mCollBook = collBook;
        mCurChapterPos = mCollBook.getDurChapter();
        mLastChapterPos = mCurChapterPos;
        isChapterListPrepare = !mCollBook.realChapterListEmpty();
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

    /**
     * 重新加载当前页面数据
     */
    private void reloadCurPageData() {
        // 如果当前已经显示数据
        if (isChapterListPrepare && getCurPageStatus() == STATUS_FINISH) {
            cancelPreload();

            // 将上一章的缓存设置为null
            mPrePageList = null;

            // 将下一章缓存设置为null
            mNextPageList = null;

            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurPageList.size()) {
                mCurPage.position = mCurPageList.size() - 1;
            }

            // 重新获取指定页面
            mCurPage = mCurPageList.get(mCurPage.position);

            mPageChangeListener.onPageChange(mCurChapterPos, mCurPage.position, mCurPageList.size());
        }
    }

    /**
     * 打开指定章节
     */
    public synchronized void openChapter(int pagePos) {

        isFirstOpen = false;

        if (!mPageView.isLayoutPrepared()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            setCurPageStatus(STATUS_LOADING);
            mPageView.drawCurPage();
            return;
        }

        // 如果获取到的章节目录为空
        if (mCollBook.realChapterListEmpty()) {
            setCurPageStatus(STATUS_CATEGORY_EMPTY);
            mPageView.drawCurPage();
            return;
        }

        if (parseCurChapter()) {
            if (goPagePos != 0) {
                pagePos = goPagePos;
                goPagePos = 0;
                isChapterOpen = false;
            }
            // 如果章节从未打开
            if (!isChapterOpen) {
                mCurPage = getCurPage(pagePos);
                mCancelPage = mCurPage;
                // 切换状态
                isChapterOpen = true;
            } else {
                mCurPage = getCurPage(0);
            }
            setCurPageStatus(STATUS_FINISH);
        } else {
            mCurPage = new TxtPage();
        }
        pagingEnd();
        mPageView.drawCurPage();
    }

    private String getTipByStatus(int status) {
        String tip = "";
        switch (status) {
            case STATUS_LOADING:
                tip = isChapterListPrepare ? "正在拼命加载中..." : "正在准备目录...";
                break;
            case STATUS_UNKNOWN_ERROR:
                tip = String.format("加载失败\n%s", "出现未知错误");
                break;
            case STATUS_NETWORK_ERROR:
                tip = String.format("加载失败\n%s", "网络连接不可用");
                break;
            case STATUS_HY_ERROR:
                tip = String.format("换源失败\n%s", "请重新选择书源");
                break;
            case STATUS_PARSE_ERROR:
                tip = String.format("排版失败\n%s", "文件解析错误");
                break;
            case STATUS_EMPTY:
                tip = "章节内容为空";
                break;
            case STATUS_PARING:
                tip = "正在排版请等待...";
                break;
            case STATUS_CATEGORY_EMPTY:
                tip = "目录列表为空";
                break;
            case STATUS_HY:
                tip = "正在换源请等待...";
        }
        return tip;
    }

    public int getPageStatus(int chapter) {
        return mStatus.get(chapter);
    }

    public int getCurPageStatus() {
        return mStatus.get(mCurChapterPos, STATUS_LOADING);
    }

    public void setPageStatus(int chapter, int status) {
        mStatus.put(chapter, status);
    }

    public void setCurPageStatus(int status) {
        mStatus.put(mCurChapterPos, status);
    }

    /**
     * 更新状态
     */
    public void changePageStatus(int status) {
        changePageStatus(mCurChapterPos, status);
    }

    public void changePageStatus(int chapter, int status) {
        if (getPageStatus(chapter) != status) {
            setPageStatus(chapter, status);
            if (chapter == mCurChapterPos) {
                mPageView.drawCurPage();
            }
        }
    }

    public boolean isPageFrozen() {
        return !isChapterListPrepare
                || getCurPageStatus() == STATUS_CATEGORY_EMPTY
                || getCurPageStatus() == STATUS_PARING
                || getCurPageStatus() == STATUS_PARSE_ERROR
                || getCurPageStatus() == STATUS_HY;
    }

    /**
     * 关闭书本
     */
    public void closeBook() {
        isChapterListPrepare = false;
        isClose = true;

        cancelPreload();

        clearList(mCurPageList);
        clearList(mNextPageList);
        mStatus.clear();

        mCurPageList = null;
        mNextPageList = null;
        mPageView = null;
        mCurPage = null;
    }

    private void clearList(List list) {
        if (list != null) {
            list.clear();
        }
    }

    public boolean isClose() {
        return isClose;
    }

    public boolean hasCurChapterData() {
        return !mCollBook.realChapterListEmpty() && getPageSize() != 0;
    }

    public boolean isChapterListPrepare() {
        return isChapterListPrepare;
    }

    public int getChapterPos() {
        return mCurChapterPos;
    }

    /**
     * 获取当前页的页码
     */
    public int getPagePos() {
        return mCurPage == null ? 0 : mCurPage.position;
    }

    /**
     * 获取正文
     */
    public String getContent(int pagePos) {
        if (mCurPageList == null || mCurPageList.size() <= pagePos) {
            return null;
        }
        TxtPage txtPage = mCurPageList.get(pagePos);
        StringBuilder s = new StringBuilder();
        for (int i = 0, lines = txtPage.lines.size(); i < lines; i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * 加载页面列表
     */
    private List<TxtPage> loadPageList(int chapterPos) throws Exception {
        // 获取章节
        ChapterListBean chapter = mCollBook.getChapter(chapterPos);
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null;
        }
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        return loadPageList(chapter, reader);
    }

    /**
     * 刷新章节列表
     */
    public abstract void refreshChapterList();

    /**
     * 获取章节的文本流
     */
    protected abstract BufferedReader getChapterReader(ChapterListBean chapter) throws Exception;

    /**
     * 章节数据是否存在
     */
    protected abstract boolean hasChapterData(ChapterListBean chapter);


    /**
     * 绘制页面
     */
    public void drawPage(Bitmap bitmap) {
        drawPage(bitmap, false);
    }

    /**
     * 绘制页面
     *
     * @param isPreview true 只绘制背景
     */
    public void drawPage(Bitmap bitmap, boolean isPreview) {
        drawBackground(mPageView.getBgBitmap());
        if (!isPreview) {
            drawContent(bitmap);
        }
        //更新绘制
        mPageView.postInvalidateOnAnimation();
    }

    /**
     * 绘制背景
     */
    @SuppressLint("DefaultLocale")
    protected void drawBackground(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        if (mSettingManager.bgIsColor()) {
            canvas.drawColor(mSettingManager.getBgColor());
        } else {
            Rect mDestRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(mSettingManager.getBgBitmap(), null, mDestRect, null);
        }
    }


    /**
     * 绘制内容
     */
    protected void drawContent(Bitmap bitmap) {

        Canvas canvas = new Canvas(bitmap);


        float interval = mTextInterval + mTextPaint.getTextSize();
        float para = mTextPara + mTextPaint.getTextSize();
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        if (getCurPageStatus() != STATUS_FINISH) {
            //绘制字体
            String tip = getTipByStatus(getCurPageStatus());

            //将提示语句放到正中间
            Layout tempLayout = new StaticLayout(tip, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
            List<String> linesData = new ArrayList<>();
            for (int i = 0; i < tempLayout.getLineCount(); i++) {
                linesData.add(tip.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
            }

            float textHeight = fontMetrics.bottom - fontMetrics.top;
            float pivotY = (mDisplayHeight - (textHeight + interval) * linesData.size()) / 3;
            for (String str : linesData) {
                float textWidth = mTextPaint.measureText(str);
                float pivotX = (mDisplayWidth - textWidth) / 2;
                canvas.drawText(str, pivotX, pivotY, mTextPaint);
                pivotY += interval;
            }
        } else {//绘制内容
            if (mCurPage.lines == null) {
                return;
            }

            float top = mMarginTop - fontMetrics.ascent;

            String str;

            //对标题进行绘制
            for (int i = 0; i < mCurPage.titleLines; ++i) {
                str = mCurPage.lines.get(i);

                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, top, mTitlePaint);

                //设置尾部间距
                if (i == mCurPage.titleLines - 1) {
                    top += para;
                } else {
                    //行间距
                    top += interval;
                }
            }

            //对内容进行绘制
            for (int i = mCurPage.titleLines, size = mCurPage.lines.size(); i < size; ++i) {
                str = mCurPage.lines.get(i);
//                if()
                Layout tempLayout = new StaticLayout(str, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                float width = StaticLayout.getDesiredWidth(str, tempLayout.getLineStart(0), tempLayout.getLineEnd(0), mTextPaint);
                if (needScale(str)) {
                    drawScaledText(canvas, str, width, mTextPaint, top);
                } else {
                    canvas.drawText(str, mMarginLeft, top, mTextPaint);
                }

                //设置尾部间距
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
                }
            }

            if (mPageChangeListener != null) {
                mPageChangeListener.onPageDrawFinish();
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

    private float drawBitmap(Canvas canvas, String line, float top){
        return 0f;
    }

    /**
     * 翻阅上一页
     */
    boolean prev() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (getCurPageStatus() == STATUS_FINISH) {
            // 先查看是否存在上一页
            TxtPage prevPage = getPrevPage();
            if (prevPage != null) {
                mCancelPage = mCurPage;
                mCurPage = prevPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasPrevChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        if (parsePrevChapter()) {
            mCurPage = getPrevLastPage();
        } else {
            mCurPage = new TxtPage();
        }
        mPageView.drawNextPage();
        return true;
    }

    /**
     * 判断是否上一章节为空
     */
    private boolean hasPrevChapter() {
        return mCurChapterPos - 1 >= 0;
    }

    /**
     * 翻到下一页
     */
    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (getCurPageStatus() == STATUS_FINISH) {
            // 先查看是否存在下一页
            TxtPage nextPage = getNextPage();
            if (nextPage != null) {
                mCancelPage = mCurPage;
                mCurPage = nextPage;
                mPageView.drawNextPage();
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mCancelPage = mCurPage;
        // 解析下一章数据
        if (parseNextChapter()) {
            mCurPage = getNextFirstPage();
        } else {
            mCurPage = new TxtPage();
        }

        mPageView.drawNextPage();
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
    boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = mCurChapterPos - 1;

        if (!hasPrevChapter()) {
            return false;
        }

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = prevChapter;

        // 当前章缓存为下一章
        mNextPageList = mCurPageList;

        // 判断是否具有上一章缓存
        if (mPrePageList != null) {
            mCurPageList = mPrePageList;
            mPrePageList = null;
            setCurPageStatus(STATUS_FINISH);
            // 回调
            chapterChangeCallback();
        } else {
            dealLoadPageList(prevChapter);
            goPagePos = 999;
        }
        //预加载上一页
        preloadPrevChapter();
        return getPageSize() > 0;
    }

    /**
     * 解析数据
     */
    boolean parseCurChapter() {
        dealLoadPageList(mCurChapterPos);
        // 预加载上一页面
        preloadPrevChapter();
        // 预加载下一页面
        preloadNextChapter();
        return getPageSize() > 0;
    }

    /**
     * 解析下一章数据
     */
    boolean parseNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        if (!hasNextChapter()) {
            return false;
        }

        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        mPrePageList = mCurPageList;

        // 是否下一章数据已经预加载了
        if (mNextPageList != null) {
            mCurPageList = mNextPageList;
            mNextPageList = null;
            setCurPageStatus(STATUS_FINISH);
            // 回调
            chapterChangeCallback();
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter);
            goPagePos = 0;
        }
        // 预加载下一页面
        preloadNextChapter();
        return getPageSize() > 0;
    }

    private void dealLoadPageList(int chapterPos) {
        try {
            mCurPageList = loadPageList(chapterPos);
            if (mCurPageList != null) {
                if (mCurPageList.isEmpty()) {
                    setPageStatus(chapterPos, STATUS_EMPTY);
                } else {
                    setPageStatus(chapterPos, STATUS_FINISH);
                }
            } else {
                if (!NetworkUtil.isNetworkAvailable()) {
                    setPageStatus(chapterPos, STATUS_NETWORK_ERROR);
                } else if (getPageStatus(chapterPos) != STATUS_EMPTY
                        && getPageStatus(chapterPos) != STATUS_LOADING) {
                    setPageStatus(chapterPos, STATUS_LOADING);
                    mPageChangeListener.requestChapter(chapterPos);
                }
            }
        } catch (Exception e) {
            mCurPageList = null;
            setPageStatus(chapterPos, STATUS_UNKNOWN_ERROR);
        }
        // 回调
        chapterChangeCallback();
    }

    private void chapterChangeCallback() {
        if (mPageChangeListener != null) {
            mPageChangeListener.onChapterChange(mCurChapterPos);
            mPageChangeListener.onPageCountChange(getPageSize());
        }
    }

    /**
     * 预加载下一章
     */
    private void preloadNextChapter() {
        int nextChapter = mCurChapterPos + 1;

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (nextChapter >= mCollBook.getChapterListSize()
                || !hasChapterData(mCollBook.getChapter(nextChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadNextDisposable != null) {
            mPreLoadNextDisposable.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<List<TxtPage>>) e -> e.onSuccess(loadPageList(nextChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<TxtPage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadNextDisposable = d;
                    }

                    @Override
                    public void onSuccess(List<TxtPage> pages) {
                        if (mCurChapterPos + 1 == nextChapter) {
                            mNextPageList = pages;
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

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (prevChapter < 0
                || !hasChapterData(mCollBook.getChapter(prevChapter))) {
            return;
        }

        //如果之前正在加载则取消
        if (mPreLoadPrevDisposable != null) {
            mPreLoadPrevDisposable.dispose();
        }

        //调用异步进行预加载加载
        Single.create((SingleOnSubscribe<List<TxtPage>>) e -> e.onSuccess(loadPageList(prevChapter)))
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<TxtPage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreLoadPrevDisposable = d;
                    }

                    @Override
                    public void onSuccess(List<TxtPage> pages) {
                        if (mCurChapterPos - 1 == prevChapter) {
                            mPrePageList = pages;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //无视错误
                    }
                });
    }

    private void cancelPreload() {
        //如果之前正在加载则取消
        if (mPreLoadPrevDisposable != null) {
            mPreLoadPrevDisposable.dispose();
        }

        //如果之前正在加载则取消
        if (mPreLoadNextDisposable != null) {
            mPreLoadNextDisposable.dispose();
        }

    }

    /**
     * 取消翻页
     */
    public void pageCancel() {
        if (mCurPage.position == 0 && mCurChapterPos > mLastChapterPos) { // 加载到下一章取消了
            if (mPrePageList != null) {
                cancelNextChapter();
            } else {
                if (parsePrevChapter()) {
                    mCurPage = getPrevLastPage();
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else if (mCurPageList == null
                || (mCurPage.position == mCurPageList.size() - 1
                && mCurChapterPos < mLastChapterPos)) {  // 加载上一章取消了

            if (mNextPageList != null) {
                cancelPreChapter();
            } else {
                if (parseNextChapter()) {
                    mCurPage = mCurPageList.get(0);
                } else {
                    mCurPage = new TxtPage();
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage;
        }
    }

    private void cancelNextChapter() {
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;

        mNextPageList = mCurPageList;
        mCurPageList = mPrePageList;
        mPrePageList = null;

        chapterChangeCallback();

        mCurPage = getPrevLastPage();
        mCancelPage = null;
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = mLastChapterPos;
        mLastChapterPos = mCurChapterPos;
        mCurChapterPos = temp;
        // 重置页面列表
        mPrePageList = mCurPageList;
        mCurPageList = mNextPageList;
        mNextPageList = null;

        chapterChangeCallback();

        mCurPage = getCurPage(0);
        mCancelPage = null;
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private List<TxtPage> loadPageList(ChapterListBean chapter, BufferedReader br) {
        //生成的页面
        List<TxtPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = mVisibleHeight;
        int titleLinesCount = 0;
        try {
            boolean showTitle = true; // 是否展示标题
            String paragraph = chapter.getDurChapterName() + "\n"; //默认展示标题
            if (mCollBook.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                br.readLine();
            }
            while (showTitle || (paragraph = br.readLine()) != null) {
                paragraph = ChapterContentHelp.replaceContent(mCollBook, paragraph);
                paragraph = ChapterContentHelp.toTraditional(mSettingManager, paragraph);
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
                        rHeight -= mTitlePaint.getTextSize();
                    } else {
                        rHeight -= mTextPaint.getTextSize();
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
                        rHeight = mVisibleHeight;
                        titleLinesCount = 0;
                        continue;
                    }

                    //测量一行占用的字节数
                    if (showTitle) {
                        Layout tempLayout = new StaticLayout(paragraph, mTitlePaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                        wordCount = tempLayout.getLineEnd(0);
                    } else {
                        Layout tempLayout = new StaticLayout(paragraph, mTextPaint, mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
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
                        rHeight -= mTextInterval;
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount);
                }

                //增加段落的间距
                if (lines.size() != 0) {
                    rHeight = rHeight - mTextPara + mTextInterval;
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
        return pages;
    }

    //判断是不是d'hou
    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == (char) 12288 && line.charAt(1) == (char) 12288;
    }

    private boolean needScale(String line) {//判断不是空行
        return line != null && line.length() != 0 && line.charAt(line.length() - 1) != '\n';
    }

    private boolean isBitmapLine(String line){
        return line != null;
    }

    /**
     * 获取初始显示的页面
     */
    private TxtPage getCurPage(int pos) {
        if (getPageSize() > 0) {
            pos = Math.max(pos, 0);
            pos = Math.min(pos, mCurPageList.size() - 1);
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取上一个页面
     */
    private TxtPage getPrevPage() {
        int pos = getPagePos() - 1;
        if (pos >= 0) {
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取下一的页面
     */
    private TxtPage getNextPage() {
        int pos = getPagePos() + 1;
        if (getPageSize() > 0 && pos < mCurPageList.size()) {
            return mCurPageList.get(pos);
        }
        return null;
    }

    /**
     * 获取上一个章节的最后一页
     */
    private TxtPage getPrevLastPage() {
        if (getPageSize() > 0) {
            int pos = mCurPageList.size() - 1;
            return mCurPageList.get(pos);
        }
        return new TxtPage();
    }

    /**
     * 获取下一个章节的第一页
     */
    private TxtPage getNextFirstPage() {
        if (getPageSize() > 0) {
            return mCurPageList.get(0);
        }
        return new TxtPage();
    }

    /**
     * 获取当前章节页数
     */
    private int getPageSize() {
        return mCurPageList == null ? 0 : mCurPageList.size();
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private boolean canTurnPage() {
        if (!isChapterListPrepare) {
            return false;
        }

        return getCurPageStatus() != STATUS_PARSE_ERROR && getCurPageStatus() != STATUS_PARING;
    }

    /*****************************************interface*****************************************/

    public interface OnPageChangeListener {
        /**
         * 作用：章节切换的时候进行回调
         *
         * @param chapterIndex:切换章节的序号
         */
        void onChapterChange(int chapterIndex);

        /**
         * 作用：请求加载章节内容
         *
         * @param chapterIndex 章节
         */
        void requestChapter(int chapterIndex);

        /**
         * 作用：章节目录加载完成时候回调
         *
         * @param chapters：返回章节目录
         */
        void onCategoryFinish(List<ChapterListBean> chapters);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         */
        void onPageChange(int chapterIndex, int pageIndex, int pageSize);

        void onPageDrawFinish();
    }
}