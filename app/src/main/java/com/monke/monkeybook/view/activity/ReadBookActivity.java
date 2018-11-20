//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.BarHide;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.BookShelfDataHolder;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ReadBookContract;
import com.monke.monkeybook.service.ReadAloudService;
import com.monke.monkeybook.utils.SystemUtil;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.AppCompat;
import com.monke.monkeybook.widget.ReadBottomStatusBar;
import com.monke.monkeybook.widget.ScrimInsetsFrameLayout;
import com.monke.monkeybook.widget.modialog.EditBookmarkView;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;
import com.monke.monkeybook.widget.page.LocalPageLoader;
import com.monke.monkeybook.widget.page.OnPageChangeListener;
import com.monke.monkeybook.widget.page.PageLoader;
import com.monke.monkeybook.widget.page.PageStatus;
import com.monke.monkeybook.widget.page.PageView;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.monkeybook.service.ReadAloudService.NEXT;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;
import static com.monke.monkeybook.utils.NetworkUtil.isNetworkAvailable;
import static com.monke.monkeybook.utils.ScreenBrightnessUtil.getScreenBrightness;
import static com.monke.monkeybook.utils.ScreenBrightnessUtil.setScreenBrightness;

public class ReadBookActivity extends MBaseActivity<ReadBookContract.Presenter> implements ReadBookContract.View, OnPageChangeListener, View.OnClickListener, View.OnLongClickListener {

    private static final int CHAPTER_SKIP_RESULT = 11;

    private static final int HPB_UPDATE_INTERVAL = 100;

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.controls_frame)
    ScrimInsetsFrameLayout controlsView;
    @BindView(R.id.ll_menu_bottom)
    LinearLayout llMenuBottom;
    @BindView(R.id.tv_pre)
    TextView tvPre;
    @BindView(R.id.tv_next)
    TextView tvNext;
    @BindView(R.id.hpb_read_progress)
    MHorProgressBar hpbReadProgress;
    @BindView(R.id.btn_catalog)
    TextView btnCatalog;
    @BindView(R.id.btn_light)
    TextView btnLight;
    @BindView(R.id.btn_font)
    TextView btnFont;
    @BindView(R.id.btn_setting)
    TextView btnSetting;
    @BindView(R.id.tv_read_aloud_timer)
    TextView tvReadAloudTimer;
    @BindView(R.id.ll_read_aloud_timer)
    LinearLayout llReadAloudTimer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.atv_divider)
    View atvDivider;
    @BindView(R.id.atv_url)
    TextView atvUrl;
    @BindView(R.id.ll_menu_top)
    LinearLayout llMenuTop;
    @BindView(R.id.appBar)
    View appBar;
    @BindView(R.id.rlNavigationBar)
    View navigationBar;
    @BindView(R.id.fabReadAloud)
    FloatingActionButton fabReadAloud;
    @BindView(R.id.fab_read_aloud_timer)
    FloatingActionButton fabReadAloudTimer;
    @BindView(R.id.fabReplaceRule)
    FloatingActionButton fabReplaceRule;
    @BindView(R.id.fabNightTheme)
    FloatingActionButton fabNightTheme;
    @BindView(R.id.pageView)
    PageView pageView;
    @BindView(R.id.fabAutoPage)
    FloatingActionButton fabAutoPage;
    @BindView(R.id.hpb_next_page_progress)
    MHorProgressBar hpbNextPageProgress;
    @BindView(R.id.read_statusbar)
    ReadBottomStatusBar readStatusBar;

    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private PageLoader mPageLoader;

    private int aloudStatus;
    private int screenTimeOut;
    private int nextPageTime;

    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private MoDialogHUD moDialogHUD;
    private ThisBatInfoReceiver batInfoReceiver;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    private boolean autoPage = false;
    private boolean isOrWillShow = false;
    private boolean isWindowAnimTranslucent;

    private final Handler mHandler = new Handler();
    private Runnable keepScreenRunnable;
    private Runnable upHpbNextPage;

    public static void startThis(MBaseActivity activity, BookShelfBean bookShelf, boolean inBookShelf) {
        Intent intent = new Intent(activity, ReadBookActivity.class);
        intent.putExtra("inBookShelf", inBookShelf);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        intent.putExtra("fromDetail", activity instanceof BookDetailActivity);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    public static void startThisFromUri(MBaseActivity activity, BookShelfBean bookShelf, boolean inBookShelf) {
        Intent intent = new Intent(activity, ReadBookActivity.class);
        intent.putExtra("fromUri", true);
        intent.putExtra("inBookShelf", inBookShelf);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    @Override
    protected ReadBookContract.Presenter initInjector() {
        return new ReadBookPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            aloudStatus = savedInstanceState.getInt("aloudStatus");
            isWindowAnimTranslucent = savedInstanceState.getBoolean("isWindowAnimTranslucent");
        }
        readBookControl.initPageConfiguration();
        screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[readBookControl.getScreenTimeOut()];

        if (getIntent().getBooleanExtra("fromDetail", false)) {//解决透明activity没有动画
            getWindow().setWindowAnimations(R.style.Animation_Activity_Translucent);
            isWindowAnimTranslucent = true;
        } else {
            getWindow().setWindowAnimations(R.style.Animation_Activity);
            isWindowAnimTranslucent = false;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_read);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        mPresenter.handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("aloudStatus", aloudStatus);
        outState.putBoolean("isWindowAnimTranslucent", isWindowAnimTranslucent);
        if (mPresenter.getBookShelf() != null) {
            BookShelfDataHolder holder = BookShelfDataHolder.getInstance();
            holder.setBookShelf(mPresenter.getBookShelf());
            holder.setInBookShelf(mPresenter.inBookShelf());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            initImmersionBar();
        }
    }

    /**
     * 状态栏
     */
    @Override
    protected void initImmersionBar() {
        mImmersionBar.fullScreen(true);

        if (isImmersionBarEnabled()) {
            mImmersionBar.transparentStatusBar();
        } else {
            mImmersionBar.statusBarColor(R.color.status_bar_bag);
        }

        mImmersionBar.navigationBarColor(R.color.navigation_bar_bag);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

        if (isMenuShowing() || isPopShowing()) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            if (isMenuShowing()) {
                mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
            } else if (isPopShowing()) {
                if (readBookControl.getHideStatusBar()) {
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_STATUS_BAR);
                } else {
                    mImmersionBar.hideBar(BarHide.FLAG_SHOW_BAR);
                }
            }
        } else {
            if (!isImmersionBarEnabled()) {
                mImmersionBar.statusBarDarkFont(false);
            } else if (readBookControl.getDarkStatusIcon()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }

            if (readBookControl.getHideStatusBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_BAR);
            } else {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            }
        }

        mImmersionBar.init();
    }


    private void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void unKeepScreenOn() {
        keepScreenOn(false);
    }

    private void screenOffTimerStart() {
        int screenOffTime = screenTimeOut * 1000 - SystemUtil.getScreenOffTime(this);
        if (keepScreenRunnable == null) {
            keepScreenRunnable = this::unKeepScreenOn;
        } else {
            mHandler.removeCallbacks(keepScreenRunnable);
        }
        if (screenOffTime > 0) {
            keepScreenOn(true);
            mHandler.postDelayed(keepScreenRunnable, screenOffTime);
        } else if (screenTimeOut >= 0) {
            keepScreenOn(false);
        } else if (screenTimeOut == -1) {
            keepScreenOn(true);
        }
    }

    /**
     * 自动翻页
     */
    private void autoPage() {
        if (upHpbNextPage != null) {
            mHandler.removeCallbacks(upHpbNextPage);
        }
        if (autoPage) {
            hpbNextPageProgress.setVisibility(View.VISIBLE);
            nextPageTime = readBookControl.getClickSensitivity() * 1000;
            hpbNextPageProgress.setMaxProgress(nextPageTime);
            if (upHpbNextPage == null) {
                upHpbNextPage = this::upHpbNextPage;
            }
            mHandler.postDelayed(upHpbNextPage, HPB_UPDATE_INTERVAL);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_stop_black_24dp);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page_stop));
        } else {
            hpbNextPageProgress.setVisibility(View.INVISIBLE);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_black_24dp);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page));
        }
        AppCompat.setTint(fabAutoPage, getResources().getColor(R.color.menu_color_default));
    }

    private void upHpbNextPage() {
        nextPageTime = nextPageTime - HPB_UPDATE_INTERVAL;
        hpbNextPageProgress.setDurProgress(nextPageTime);
        mHandler.postDelayed(upHpbNextPage, HPB_UPDATE_INTERVAL);
        if (nextPageTime <= 0) {
            nextPage();
            nextPageTime = readBookControl.getClickSensitivity() * 1000;
        }
    }

    private void autoPageStop() {
        autoPage = false;
        autoPage();
    }

    private void nextPage() {
        runOnUiThread(() -> {
            screenOffTimerStart();
            if (mPageLoader != null) {
                mPageLoader.skipToNextPage();
            }
        });
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setupActionBar();

        int menuColor = getResources().getColor(R.color.menu_color_default);
        AppCompat.setTint(btnCatalog, menuColor);
        AppCompat.setTint(btnLight, menuColor);
        AppCompat.setTint(btnFont, menuColor);
        AppCompat.setTint(btnSetting, menuColor);

        if (isNightTheme()) {
            fabNightTheme.setImageResource(R.drawable.ic_day_border_bleak_24dp);
        } else {
            fabNightTheme.setImageResource(R.drawable.ic_night_border_black_24dp);
        }

        if (!readBookControl.getLightIsFollowSys()) {
            setScreenBrightness(this, readBookControl.getScreenLight(getScreenBrightness(this)));
        }
        readStatusBar.refreshUI();

        mPresenter.handleIntent(getIntent());
    }

    @Override
    public void prepareDisplay(boolean check) {
        mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());

        if (check) {
            getWindow().getDecorView().post(() -> mPresenter.checkBookInfo());
        } else {
            startLoadingBook();
        }

        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
            readStatusBar.updateChapterInfo(mPresenter.getBookShelf(), 0);
        }

        updateTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
        showHideView();
    }

    @Override
    protected View getSnackBarView() {
        return pageView;
    }

    //设置ToolBar
    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showHideView() {
        if (mPresenter.getBookShelf() == null
                || mPresenter.getBookShelf().realChapterListEmpty()
                || mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvDivider.setVisibility(View.GONE);
            atvUrl.setVisibility(View.GONE);
        } else {
            atvDivider.setVisibility(View.VISIBLE);
            atvUrl.setVisibility(View.VISIBLE);
        }

        supportInvalidateOptionsMenu();
    }

    @Override
    public void updateTitle(String title) {
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void showLoading(String msg) {
        ensureProgressHUD();
        moDialogHUD.showLoading(msg);
    }

    @Override
    public void dismissHUD() {
        if (moDialogHUD != null) {
            moDialogHUD.dismiss();
        }
    }

    /**
     * 菜单是否显示
     *
     * @return
     */
    private boolean isMenuShowing() {
        return controlsView.getVisibility() == View.VISIBLE;
    }

    private boolean isPopShowing() {
        return (readAdjustPop != null && readAdjustPop.isShowing())
                || (readInterfacePop != null && readInterfacePop.isShowing())
                || (moreSettingPop != null && moreSettingPop.isShowing());
    }

    /**
     * 显示菜单
     */
    private void ensureMenuInAnim() {
        if (menuTopIn == null) {
            menuTopIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_in);
            menuTopIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    initImmersionBar();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    initImmersionBar();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (menuBottomIn == null) {
            menuBottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_in);
        }
    }

    /**
     * 隐藏菜单
     */
    private void ensureMenuOutAnim() {
        if (menuTopOut == null) {
            menuTopOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_out);
            menuTopOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    controlsView.setVisibility(View.INVISIBLE);
                    if (!isOrWillShow) {
                        initImmersionBar();
                    }
                    isOrWillShow = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (menuBottomOut == null) {
            menuBottomOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_out);
            menuBottomOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    controlsView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    /**
     * 调节
     */
    private void ensureReadAdjustPop() {
        if (readAdjustPop != null) {
            return;
        }

        readAdjustPop = new ReadAdjustPop(this, new ReadAdjustPop.OnAdjustListener() {
            @Override
            public void changeSpeechRate(int speechRate) {
                if (ReadAloudService.running) {
                    ReadAloudService.pause(ReadBookActivity.this);
                    ReadAloudService.resume(ReadBookActivity.this);
                }
            }

            @Override
            public void speechRateFollowSys() {
                if (ReadAloudService.running) {
                    ReadAloudService.stop(ReadBookActivity.this);
                    toast("跟随系统需要重新开始朗读");
                }
            }
        });
    }

    private void ensureWindowAnimNotTranslucent(){
        if (isWindowAnimTranslucent) {
            getWindow().setWindowAnimations(R.style.Animation_Activity);
            isWindowAnimTranslucent = false;
        }
    }

    /**
     * 界面设置
     */
    private void ensureReadInterfacePop() {
        if (readInterfacePop != null) {
            return;
        }

        readInterfacePop = new ReadInterfacePop(this, new ReadInterfacePop.OnChangeProListener() {

            @Override
            public void upPageMode() {
                if (mPageLoader != null) {
                    mPageLoader.setPageMode(readBookControl.getPageMode(readBookControl.getPageMode()));
                }
            }

            @Override
            public void upTextSize() {
                if (mPageLoader != null) {
                    mPageLoader.setTextSize();
                }
            }

            @Override
            public void upMargin() {
                readStatusBar.updatePadding();

                if (mPageLoader != null) {
                    mPageLoader.upMargin();
                }
            }

            @Override
            public void bgChange() {
                initImmersionBar();
                readStatusBar.refreshUI();

                if (mPageLoader != null) {
                    mPageLoader.setPageStyle(false);
                }
            }

            @Override
            public void refresh() {
                readStatusBar.refreshUI();
                if (mPageLoader != null) {
                    mPageLoader.refreshUI();
                }
            }

        });
    }

    /**
     * 其它设置
     */
    private void ensureMoreSettingPop() {
        if (moreSettingPop != null) {
            return;
        }

        moreSettingPop = new MoreSettingPop(this, new MoreSettingPop.OnChangeProListener() {
            @Override
            public void keepScreenOnChange(int keepScreenOn) {
                screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[keepScreenOn];
                screenOffTimerStart();
            }

            @Override
            public void refresh() {
                initImmersionBar();
                readStatusBar.refreshUI();
                if (mPageLoader != null) {
                    mPageLoader.refreshUI();
                }
            }


        });
    }

    /**
     * 弹窗
     */
    private void ensureProgressHUD() {
        if (moDialogHUD != null) {
            return;
        }
        moDialogHUD = new MoDialogHUD(this);

        moDialogHUD.setOnDimissListener(this::initImmersionBar);
    }

    /**
     * 加载阅读页面
     */
    private void initPageView() {
        pageView.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                screenOffTimerStart();
                return true;
            }

            @Override
            public void center() {
                popMenuIn();
            }

        });

        mPageLoader.setOnPageChangeListener(this);

        mPageLoader.refreshChapterList();
    }

    @Override
    public void onChapterChange(int pos) {
        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
            atvUrl.setText(mPresenter.getBookShelf().getChapter(pos).getDurChapterUrl());
        } else {
            atvUrl.setText("");
        }

        if (mPresenter.getBookShelf().getChapterListSize() == 1) {
            tvPre.setEnabled(false);
            tvNext.setEnabled(false);
        } else {
            if (pos == 0) {
                tvPre.setEnabled(false);
                tvNext.setEnabled(true);
            } else if (pos == mPresenter.getBookShelf().getChapterListSize() - 1) {
                tvPre.setEnabled(true);
                tvNext.setEnabled(false);
            } else {
                tvPre.setEnabled(true);
                tvNext.setEnabled(true);
            }
        }
    }

    @Override
    public void onCategoryFinish(List<ChapterListBean> chapters) {
        updateTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
        mPresenter.getBookShelf().setChapterList(chapters);
        mPresenter.getBookShelf().upChapterListSize();
        mPresenter.getBookShelf().upDurChapterName();
        mPresenter.getBookShelf().upLastChapterName();
        showHideView();
    }

    @Override
    public void onPageCountChange(int count) {
        hpbReadProgress.setMaxProgress(Math.max(0, count - 1));
        hpbReadProgress.setDurProgress(0);
        hpbReadProgress.setEnabled(!mPageLoader.isPageFrozen());
    }

    @Override
    public void onPageChange(int chapterIndex, int pageIndex, int pageSize) {
        mPresenter.getBookShelf().setDurChapter(chapterIndex);
        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
        mPresenter.getBookShelf().upDurChapterName();
        mPresenter.saveProgress();

        readStatusBar.updateChapterInfo(mPresenter.getBookShelf(), pageSize);

        hpbReadProgress.post(() -> hpbReadProgress.setDurProgress(pageIndex));

        //继续朗读
        if ((ReadAloudService.running) && pageIndex >= 0) {
            String content = mPageLoader.getContent(pageIndex);
            if (content != null) {
                ReadAloudService.play(ReadBookActivity.this, false, content,
                        mPresenter.getBookShelf().getBookInfoBean().getName(),
                        mPresenter.getChapterTitle(chapterIndex)
                );
            }
            return;
        }
        //启动朗读
        if (getIntent().getBooleanExtra("readAloud", false)
                && pageIndex >= 0 && mPageLoader.getContent(pageIndex) != null) {
            getIntent().putExtra("readAloud", false);
            onMediaButton();
            return;
        }
        autoPage();
    }

    @Override
    protected void bindEvent() {
        //打开URL
        atvUrl.setOnClickListener(this);

        //朗读定时
        fabReadAloudTimer.setOnClickListener(this);

        //朗读
        fabReadAloud.setOnClickListener(this);
        //长按停止朗读
        fabReadAloud.setOnLongClickListener(this);

        //自动翻页
        fabAutoPage.setOnClickListener(this);
        fabAutoPage.setOnLongClickListener(this);

        //替换
        fabReplaceRule.setOnClickListener(this);
        fabReplaceRule.setOnLongClickListener(this);

        //夜间模式
        fabNightTheme.setOnClickListener(this);
        fabNightTheme.setOnLongClickListener(this);

        //上一章
        tvPre.setOnClickListener(this);

        //下一章
        tvNext.setOnClickListener(this);

        //目录
        btnCatalog.setOnClickListener(this);

        //亮度
        btnLight.setOnClickListener(this);

        //界面
        btnFont.setOnClickListener(this);

        //设置
        btnSetting.setOnClickListener(this);

        //菜单
        controlsView.setOnClickListener(this);

        //动态设置状态栏，导航栏
        controlsView.setOnInsetsCallback(insets -> {
            appBar.setPadding(0, insets.top, 0, 0);
            navigationBar.setPadding(0, 0, 0, insets.bottom);
            Rect newInsets = new Rect(insets);
            newInsets.bottom = 0;
        });

        //阅读进度
        hpbReadProgress.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {

            }

            @Override
            public void moveStopProgress(float dur) {
                if (mPageLoader != null) {
                    int realDur = (int) Math.ceil(dur);
                    if ((realDur) != mPresenter.getBookShelf().getDurChapterPage()) {
                        mPageLoader.skipToPage(realDur);
                    }
                    if (hpbReadProgress.getDurProgress() != realDur)
                        hpbReadProgress.setDurProgress(realDur);
                }
            }

            @Override
            public void setDurProgress(float dur) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.controls_frame:
                popMenuOut();
                break;
            case R.id.atv_url:
                openCurrentChapterBrowser();
                break;
            case R.id.fab_read_aloud_timer:
                ReadAloudService.setTimer(this);
                break;
            case R.id.fabReadAloud:
                onMediaButton();
                break;
            case R.id.fabAutoPage:
                if (ReadAloudService.running) {
                    toast("朗读正在运行,不能自动翻页");
                    return;
                }
                autoPage = !autoPage;
                autoPage();
                popMenuOut();
                break;
            case R.id.fabReplaceRule:
                isOrWillShow = true;
                popMenuOut();
                ensureWindowAnimNotTranslucent();
                new Handler().postDelayed(() -> ReplaceRuleActivity.startThis(this), 200L);
                break;
            case R.id.fabNightTheme:
                popMenuOut();
                new Handler().postDelayed(() -> setNightTheme(!isNightTheme()), navigationBar.getPaddingBottom() > 0 ? 400L : 200L);
                break;
            case R.id.tv_pre:
                if (mPresenter.getBookShelf() != null) {
                    mPageLoader.skipPreChapter();
                }
                break;
            case R.id.tv_next:
                if (mPresenter.getBookShelf() != null) {
                    mPageLoader.skipNextChapter();
                }
                break;
            case R.id.btn_catalog:
                isOrWillShow = true;
                popMenuOut();
                ensureWindowAnimNotTranslucent();
                if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().realChapterListEmpty()) {
                    new Handler().postDelayed(() -> ChapterListActivity.startThis(ReadBookActivity.this, mPresenter.getBookShelf(), CHAPTER_SKIP_RESULT), 200L);
                }
                break;
            case R.id.btn_light:
                isOrWillShow = true;
                popMenuOut();
                ensureReadAdjustPop();
                new Handler().postDelayed(() -> {
                    readAdjustPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, 220L);
                break;
            case R.id.btn_font:
                isOrWillShow = true;
                popMenuOut();
                ensureReadInterfacePop();
                new Handler().postDelayed(() -> {
                    readInterfacePop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, 220L);
                break;
            case R.id.btn_setting:
                isOrWillShow = true;
                popMenuOut();
                ensureMoreSettingPop();
                new Handler().postDelayed(() -> {
                    moreSettingPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, 220L);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fabReadAloud:
                if (ReadAloudService.running) {
                    toast(getString(R.string.aloud_stop));
                    ReadAloudService.stop(this);
                } else {
                    toast(getString(R.string.read_aloud));
                }
                break;
            case R.id.fabAutoPage:
                toast(getString(R.string.auto_next_page));
                break;
            case R.id.fabReplaceRule:
                toast(getString(R.string.replace_rule_title));
                break;
            case R.id.fabNightTheme:
                toast(getString(R.string.night_theme));
                break;
        }
        return true;
    }

    @Override
    public void startLoadingBook() {
        hpbReadProgress.setMaxProgress(0);
        initPageView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_read_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mPresenter.getBookShelf() == null || mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            for (int i = 0; i < menu.size(); i++) {
                if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                    menu.getItem(i).setVisible(false);
                    menu.getItem(i).setEnabled(false);
                }
            }
            if (mPresenter.getBookShelf() != null) {
                menu.findItem(R.id.edit_charset).setVisible(true);
                menu.findItem(R.id.edit_charset).setEnabled(true);
            }
        } else {

            for (int i = 0; i < menu.size(); i++) {
                if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                    menu.getItem(i).setVisible(true);
                    menu.getItem(i).setEnabled(true);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_change_source:
                changeSource();
                break;
            case R.id.action_refresh:
                refreshDurChapter();
                break;
            case R.id.action_download:
                download();
                break;
            case R.id.add_bookmark:
                showBookmark(null);
                break;
            case R.id.action_copy_text:
                popMenuOut();
                if (mPageLoader != null) {
                    ensureProgressHUD();
                    moDialogHUD.showText(mPageLoader.getCurrentContent());
                }
                break;
            case R.id.disable_book_source:
                mPresenter.disableDurBookSource();
                break;
            case R.id.edit_charset:
                setCharset();
                break;
            case R.id.action_book_info:
                ensureWindowAnimNotTranslucent();
                BookInfoActivity.startThis(this, mPresenter.getBookShelf().copy());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCurrentChapterBrowser() {
        try {
            String url = atvUrl.getText().toString();
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            toast(getString(R.string.can_not_open));
        }
    }

    /**
     * 刷新当前章节
     */
    private void refreshDurChapter() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法刷新当前章节!");
            return;
        }
        popMenuOut();
        if (mPageLoader != null) {
            new Handler().postDelayed(() -> mPageLoader.refreshDurChapter(), 200L);
        }
    }

    /**
     * 书签
     */
    private void showBookmark(BookmarkBean bookmarkBean) {
        this.popMenuOut();
        boolean isAdd = false;
        if (mPresenter.getBookShelf() != null) {
            if (bookmarkBean == null) {
                isAdd = true;
                bookmarkBean = new BookmarkBean();
                bookmarkBean.setNoteUrl(mPresenter.getBookShelf().getNoteUrl());
                bookmarkBean.setBookName(mPresenter.getBookShelf().getBookInfoBean().getName());
                bookmarkBean.setChapterIndex(mPresenter.getBookShelf().getDurChapter());
                bookmarkBean.setPageIndex(mPresenter.getBookShelf().getDurChapterPage());
                bookmarkBean.setChapterName(mPresenter.getChapterTitle(mPresenter.getBookShelf().getDurChapter()));
            }

            ensureProgressHUD();
            moDialogHUD.showBookmark(bookmarkBean, isAdd, new EditBookmarkView.OnBookmarkClick() {
                @Override
                public void saveBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.saveBookmark(bookmarkBean);
                }

                @Override
                public void delBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.delBookmark(bookmarkBean);
                }

                @Override
                public void openChapter(int chapterIndex, int pageIndex) {
                    mPageLoader.skipToChapter(chapterIndex, pageIndex);
                }
            });
        }

    }

    /**
     * 换源
     */
    private void changeSource() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法换源");
            return;
        }
        popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            ensureProgressHUD();
            moDialogHUD.showChangeSource(this, mPresenter.getBookShelf().getBookInfoBean(), searchBookBean -> {
                if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                    mPageLoader.setCurrentStatus(PageStatus.STATUS_HY);
                    mPresenter.changeBookSource(searchBookBean);
                }
            });
        }
    }

    /**
     * 下载
     */
    private void download() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法下载");
            return;
        }

        if (!mPresenter.inBookShelf()) {
            toast("请先将书籍加入书架");
            return;
        }

        if (!mPageLoader.isChapterListPrepare()) {
            toast("书籍目录获取失败，无法下载");
            return;
        }

        popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            //弹出离线下载界面
            int endIndex = mPresenter.getBookShelf().getChapterListSize() - 1;

            ensureProgressHUD();
            moDialogHUD.showDownloadList(mPresenter.getBookShelf().getDurChapter(), endIndex,
                    mPresenter.getBookShelf().getChapterListSize(),
                    (start, end) -> {
                        moDialogHUD.dismiss();
                        mPresenter.addDownload(start, end);
                    });
        }
    }

    /**
     * 设置编码
     */
    private void setCharset() {
        popMenuOut();

        if (mPageLoader instanceof LocalPageLoader && mPresenter.getBookShelf() != null) {
            ensureProgressHUD();
            String charset = mPresenter.getBookShelf().getBookInfoBean().getCharset();
            moDialogHUD.showInputBox(getString(R.string.edit_charset),
                    charset,
                    new String[]{"UTF-8", "GB2312", "GBK", "Unicode", "UTF-16", "ASCII"}
                    , inputText -> {
                        if (!TextUtils.equals(charset, inputText)) {
                            mPresenter.getBookShelf().getBookInfoBean().setCharset(inputText);
                            mPresenter.saveProgress();
                            mPresenter.getBookShelf().setHasUpdate(true);
                            ((LocalPageLoader) mPageLoader).updateCharset();
                        }
                    });
        } else {
            toast("正在加载，请稍候");
        }
    }

    /**
     * 隐藏菜单
     */
    private void popMenuOut() {
        if (isMenuShowing()) {
            ensureMenuOutAnim();
            llMenuTop.startAnimation(menuTopOut);
            llMenuBottom.startAnimation(menuBottomOut);
        }
    }

    /**
     * 显示菜单
     */
    private void popMenuIn() {
        if (!isMenuShowing()) {
            ensureMenuInAnim();
            controlsView.setVisibility(View.VISIBLE);
            llMenuTop.startAnimation(menuTopIn);
            llMenuBottom.startAnimation(menuBottomIn);
        }
    }

    /**
     * 更新朗读状态
     */
    @Override
    public void upAloudState(int status) {
        aloudStatus = status;
        autoPageStop();
        switch (status) {
            case NEXT:
                if (mPageLoader == null) {
                    ReadAloudService.stop(this);
                    break;
                }
                if (!mPageLoader.noAnimationToNextPage()) {
                    ReadAloudService.stop(this);
                }
                break;
            case PLAY:
                fabReadAloud.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            case PAUSE:
                fabReadAloud.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            default:
                fabReadAloud.setImageResource(R.drawable.ic_headset_black_24dp);
                llReadAloudTimer.setVisibility(View.INVISIBLE);
        }
        AppCompat.setTint(fabReadAloud, getResources().getColor(R.color.menu_color_default));
    }

    @Override
    public void upAloudTimer(String text) {
        tvReadAloudTimer.setText(text);
    }

    @Override
    public void speakIndex(int speakIndex) {
//        runOnUiThread(() -> csvBook.speakStart(speakIndex));
    }

    @Override
    public void refresh(boolean recreate) {
        if (recreate) {
            recreate();
        } else {
            if (mPageLoader != null) {
                mPageLoader.refreshUI();
            }
            if (readInterfacePop != null) {
                readInterfacePop.setBg();
            }
            readStatusBar.refreshUI();
            initImmersionBar();
        }
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (mPresenter.inBookShelf() || mPresenter.getBookShelf() == null) {
            return true;
        } else {
            if (checkAddShelfPop == null) {
                checkAddShelfPop = new CheckAddShelfPop(this, mPresenter.getBookShelf().getBookInfoBean().getName(),
                        new CheckAddShelfPop.OnItemClickListener() {
                            @Override
                            public void clickExit() {
                                mPresenter.removeFromShelf();
                            }

                            @Override
                            public void clickAddShelf() {
                                mPresenter.addToShelf(null);
                                checkAddShelfPop.dismiss();
                            }
                        });
            }
            if (!checkAddShelfPop.isShowing()) {
                checkAddShelfPop.showAtLocation(flContent, Gravity.CENTER, 0, 0);
            }
            return false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean isDown = action == 0;
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return isDown ? this.onKeyDown(keyCode, event) : this.onKeyUp(keyCode, event);
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 按键事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD == null ? false : moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (ReadAloudService.running && aloudStatus == PLAY) {
                    ReadAloudService.pause(this);
                    toast(getString(R.string.read_aloud_pause));
                    return true;
                } else {
                    finish();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (isMenuShowing()) {
                    popMenuOut();
                } else {
                    popMenuIn();
                }
                return true;
            } else if (!isMenuShowing()) {
                if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.PLAY) && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    if (mPageLoader != null && !pageView.isStarted()) {
                        mPageLoader.skipToNextPage();
                    }
                    return true;
                } else if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.PLAY) && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (mPageLoader != null && !pageView.isStarted()) {
                        mPageLoader.skipToPrePage();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_N) {
                    if (mPageLoader != null && !pageView.isStarted()) {
                        mPageLoader.skipToNextPage();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_P) {
                    if (mPageLoader != null && !pageView.isStarted()) {
                        mPageLoader.skipToPrePage();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
                    if (mPageLoader != null && !pageView.isStarted()) {
                        mPageLoader.skipToNextPage();
                    }
                    return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isMenuShowing()) {
            if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.PLAY)
                    && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 朗读按钮
     */
    @Override
    public void onMediaButton() {
        if (!ReadAloudService.running) {
            aloudStatus = ReadAloudService.STOP;
        }
        switch (aloudStatus) {
            case PAUSE:
                ReadAloudService.resume(this);
                fabReadAloud.setContentDescription(getString(R.string.read_aloud));
                break;
            case PLAY:
                ReadAloudService.pause(this);
                fabReadAloud.setContentDescription(getString(R.string.read_aloud_pause));
                break;
            default:
                popMenuOut();
                if (mPresenter.getBookShelf() != null && mPageLoader != null) {
                    ReadAloudService.play(this, true, mPageLoader.getCurrentContent(),
                            mPresenter.getBookShelf().getBookInfoBean().getName(),
                            mPresenter.getChapterTitle(mPageLoader.getChapterPosition())
                    );
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAPTER_SKIP_RESULT && resultCode == RESULT_OK) {
            int what = data.getIntExtra("what", -1);
            switch (what) {
                case 0:
                    mPageLoader.skipToChapter(data.getIntExtra("chapter", 0), data.getIntExtra("page", 0));
                    break;
                case 1:
                    showBookmark(data.getParcelableExtra("bookmark"));
                    break;
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onResume() {
        super.onResume();
        if (batInfoReceiver == null) {
            batInfoReceiver = new ThisBatInfoReceiver();
        }
        batInfoReceiver.registerReceiverBatInfo();
        screenOffTimerStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoPageStop();
        if (batInfoReceiver != null) {
            unregisterReceiver(batInfoReceiver);
            batInfoReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (upHpbNextPage != null) {
            mHandler.removeCallbacks(upHpbNextPage);
        }
        if (keepScreenRunnable != null) {
            mHandler.removeCallbacks(keepScreenRunnable);
        }
        super.onDestroy();
        if (batInfoReceiver != null) {
            unregisterReceiver(batInfoReceiver);
            batInfoReceiver = null;
        }
        ReadAloudService.stop(this);
        if (mPageLoader != null) {
            mPageLoader.closeBook();
            mPageLoader = null;
        }
    }

    /**
     * 结束
     */
    @Override
    public void finish() {
        if (!checkAddShelf()) {
            return;
        }
        BookShelfDataHolder.getInstance().cleanData();
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)
                && !AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            startActivityByAnim(intent, R.anim.anim_alpha_in, R.anim.anim_alpha_out);
        }
        super.finish();
    }


    @Override
    public void changeSourceFinish(boolean success) {
        if (mPageLoader != null) {
            if (success) {
                mPageLoader.changeSourceFinish(mPresenter.getBookShelf());
            } else {
                if (mPageLoader.hasCurrentChapter()) {
                    toast("换源失败，请选择其他书源");
                    mPageLoader.setCurrentStatus(PageStatus.STATUS_FINISH);
                } else {
                    mPageLoader.setCurrentStatus(PageStatus.STATUS_HY_ERROR);
                }
            }
        }
    }

    /**
     * 时间和电量广播
     */
    class ThisBatInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (readStatusBar != null) {
                if (android.content.Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    readStatusBar.updateTime();
                } else if (android.content.Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    readStatusBar.updateBattery(level);
                }
            }
        }

        public void registerReceiverBatInfo() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(android.content.Intent.ACTION_TIME_TICK);
            filter.addAction(android.content.Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batInfoReceiver, filter);
        }

    }
}