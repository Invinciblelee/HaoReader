//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gyf.immersionbar.BarHide;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.WebLoadConfig;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.BookShelfHolder;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.NoDoubleClickListener;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ReadBookContract;
import com.monke.monkeybook.service.ReadAloudService;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.utils.SystemUtil;
import com.monke.monkeybook.utils.URLUtils;
import com.monke.monkeybook.view.fragment.ChapterDrawerFragment;
import com.monke.monkeybook.view.fragment.dialog.AlertDialog;
import com.monke.monkeybook.view.fragment.dialog.BookmarkDialog;
import com.monke.monkeybook.view.fragment.dialog.ChangeSourceDialog;
import com.monke.monkeybook.view.fragment.dialog.DownLoadDialog;
import com.monke.monkeybook.view.fragment.dialog.InputDialog;
import com.monke.monkeybook.view.fragment.dialog.LargeTextDialog;
import com.monke.monkeybook.view.fragment.dialog.ProgressDialog;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.ReadBottomStatusBar;
import com.monke.monkeybook.widget.ScrimInsetsRelativeLayout;
import com.monke.monkeybook.widget.page.LocalPageLoader;
import com.monke.monkeybook.widget.page.OnPageChangeListener;
import com.monke.monkeybook.widget.page.PageLoader;
import com.monke.monkeybook.widget.page.PageStatus;
import com.monke.monkeybook.widget.page.PageView;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.basemvplib.NetworkUtil.isNetworkAvailable;
import static com.monke.monkeybook.service.ReadAloudService.NEXT;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;
import static com.monke.monkeybook.utils.ScreenBrightnessUtil.getScreenBrightness;
import static com.monke.monkeybook.utils.ScreenBrightnessUtil.setScreenBrightness;

public class ReadBookActivity extends MBaseActivity<ReadBookContract.Presenter> implements ReadBookContract.View, OnPageChangeListener, View.OnClickListener, View.OnLongClickListener {

    private static final long DELAY_SHORT = 200L;
    private static final long DELAY_MIDDLE = 300L;
    private static final long DELAY_LONG = 500L;
    private static final int HPB_UPDATE_INTERVAL = 100;

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.controls_frame)
    ScrimInsetsRelativeLayout controlsView;
    @BindView(R.id.view_controls_back)
    View controlsBackView;
    @BindView(R.id.ll_menu_bottom)
    LinearLayout llMenuBottom;
    @BindView(R.id.tv_pre)
    TextView tvPre;
    @BindView(R.id.tv_next)
    TextView tvNext;
    @BindView(R.id.hpb_read_progress)
    AppCompatSeekBar hpbReadProgress;
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
    @BindView(R.id.read_toolbar)
    Toolbar toolbar;
    @BindView(R.id.atv_divider)
    View atvDivider;
    @BindView(R.id.atv_layout)
    View atvLayout;
    @BindView(R.id.atv_url)
    TextView atvUrl;
    @BindView(R.id.atv_source_name)
    TextView atvSourceName;
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
    ProgressBar hpbNextPageProgress;
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

    private int chapterProgressMax;
    private int chapterDurProgress;
    private boolean chapterDraggable;

    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private ThisBatInfoReceiver batInfoReceiver;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    private boolean autoPage = false;
    private boolean isOrWillShow = false;
    private boolean mFirstVisible = true;

    private final Handler mHandler = new Handler();
    private Runnable mScreenOnRunnable;
    private Runnable mNextPageRunnable;

    private ProgressDialog progressDialog;

    private ChapterDrawerFragment chapterFragment;

    public static void startThis(MBaseActivity activity, BookShelfBean bookShelf) {
        Intent intent = new Intent(activity, ReadBookActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        activity.startActivity(intent);
    }

    public static void startThisFromUri(MBaseActivity activity, BookShelfBean bookShelf) {
        Intent intent = new Intent(activity, ReadBookActivity.class);
        intent.putExtra("fromUri", true);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        activity.startActivityByAnim(intent, R.anim.anim_alpha_in, R.anim.anim_alpha_out);
    }


    @Override
    protected ReadBookContract.Presenter initInjector() {
        return new ReadBookPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        if (savedInstanceState != null) {
            chapterProgressMax = savedInstanceState.getInt("chapterPageMax");
            chapterDurProgress = savedInstanceState.getInt("chapterDurPage");
            chapterDraggable = savedInstanceState.getBoolean("chapterDraggable");
            aloudStatus = savedInstanceState.getInt("aloudStatus");
        }
        readBookControl.initPageConfiguration();
        screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[readBookControl.getScreenTimeOut()];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(params);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_read);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("aloudStatus", aloudStatus);
        outState.putInt("chapterPageMax", hpbReadProgress.getMax());
        outState.putInt("chapterDurPage", hpbReadProgress.getProgress());
        outState.putBoolean("chapterDraggable", hpbReadProgress.isEnabled());
        if (mPresenter.getBookShelf() != null) {
            BitIntentDataManager dataManager = BitIntentDataManager.getInstance();
            dataManager.putData("inBookShelf", mPresenter.inBookShelf());
            dataManager.putData("bookShelf", mPresenter.getBookShelf());
            dataManager.putData("chapter", mPageLoader.getCurrentChapter());
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mFirstVisible && hasFocus) {
            initImmersionBar();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mFirstVisible = false;
    }

    /**
     * 状态栏
     */
    @Override
    public void initImmersionBar() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mImmersionBar.statusBarDarkFont(false);
            if (readBookControl.getHideStatusBar()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_BAR);
            } else {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            }
        } else if (isMenuShowing() || isPopShowing()) {
            mImmersionBar.statusBarDarkFont(false);
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
            if (readBookControl.getDarkStatusIcon()) {
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

        mImmersionBar.fullScreen(true);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

        if (isImmersionBarEnabled()) {
            mImmersionBar.transparentStatusBar();
        } else {
            mImmersionBar.statusBarColor(R.color.colorStatusBar);
        }

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        mImmersionBar.init();
    }


    private void keepScreenOn(boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 屏幕超时
     */
    private void screenOffTimerStart() {
        if (screenTimeOut < 0) {
            keepScreenOn(true);
            return;
        }
        if (mScreenOnRunnable == null) {
            mScreenOnRunnable = () -> keepScreenOn(false);
        }

        int screenOffTime = screenTimeOut * 1000 - SystemUtil.getScreenOffTime(this);
        if (screenOffTime > 0) {
            mHandler.removeCallbacks(mScreenOnRunnable);
            keepScreenOn(true);
            mHandler.postDelayed(mScreenOnRunnable, screenOffTime);
        } else {
            keepScreenOn(false);
        }
    }

    /**
     * 自动翻页
     */
    private void autoPage() {
        if (mNextPageRunnable != null) {
            mHandler.removeCallbacks(mNextPageRunnable);
        }
        if (autoPage) {
            hpbNextPageProgress.setVisibility(View.VISIBLE);
            nextPageTime = readBookControl.getClickSensitivity() * 1000;
            hpbNextPageProgress.setMax(nextPageTime);
            if (mNextPageRunnable == null) {
                mNextPageRunnable = this::upHpbNextPage;
            }
            mHandler.postDelayed(mNextPageRunnable, HPB_UPDATE_INTERVAL);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_stop_black_24dp);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page_stop));
        } else {
            hpbNextPageProgress.setVisibility(View.INVISIBLE);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_black_24dp);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page));
        }
    }

    private void upHpbNextPage() {
        nextPageTime = nextPageTime - HPB_UPDATE_INTERVAL;
        hpbNextPageProgress.setProgress(nextPageTime);
        mHandler.postDelayed(mNextPageRunnable, HPB_UPDATE_INTERVAL);
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
        this.setSupportActionBar(toolbar);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
    public void prepareDisplay() {
        mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());

        getWindow().getDecorView().post(() -> {
            mPresenter.checkBookInfo();

            chapterFragment = ChapterDrawerFragment.newInstance();
            addChapterFragment(chapterFragment);
        });

        if (mPresenter.getBookShelf().getChapterListSize() > 0) {
            readStatusBar.updateChapterInfo(mPresenter.getBookShelf(), 0);
        }

        updateTitle(mPresenter.getBookShelf().getBookInfoBean().getName());

        showHideUrlViews();
    }

    @Override
    protected View getSnackBarView() {
        return pageView;
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showHideUrlViews() {
        if (mPresenter.getBookShelf() == null
                || mPresenter.getBookShelf().realChapterListEmpty()
                || mPresenter.getBookShelf().isLocalBook()) {
            atvDivider.setVisibility(View.GONE);
            atvLayout.setVisibility(View.GONE);
        } else {
            int chapterIndex = mPresenter.getBookShelf().getDurChapter();
            atvUrl.setText(URLUtils.getAbsUrl(mPresenter.getBookShelf().getBookInfoBean().getChapterListUrl(),
                    mPresenter.getBookShelf().getChapter(chapterIndex).getDurChapterUrl()));
            atvSourceName.setText(mPresenter.getBookShelf().getBookInfoBean().getOrigin());
            if (TextUtils.isEmpty(atvUrl.getText())) {
                atvDivider.setVisibility(View.GONE);
                atvLayout.setVisibility(View.GONE);
            } else {
                atvDivider.setVisibility(View.VISIBLE);
                atvLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void upMenu() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void updateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void showLoading(String msg) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, msg);
        } else {
            progressDialog.setMessage(msg);
            progressDialog.show(this);
        }
    }

    @Override
    public void dismissHUD() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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

    private void ensureCenterClickArea() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) controlsBackView.getLayoutParams();
        int top = appBar.getHeight();
        int bottom = navigationBar.getHeight();
        if (params.topMargin != top || params.bottomMargin != bottom) {
            params.topMargin = top;
            params.bottomMargin = bottom;
            controlsBackView.requestLayout();
        }
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
                    ensureCenterClickArea();
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
                    mPageLoader.setMargin();
                }
            }

            @Override
            public void bgChange() {
                initImmersionBar();
                readStatusBar.refreshUI();

                if (mPageLoader != null) {
                    mPageLoader.setBackground();
                }
            }

            @Override
            public void refresh() {
                readStatusBar.refreshUI();
                mPageLoader.refreshUI();
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

            @Override
            public void refreshStatusBar() {
                readStatusBar.refreshUI();
            }


        });
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
        showHideUrlViews();

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
    public void onCategoryFinish(List<ChapterBean> chapters) {
        updateTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
        mPresenter.getBookShelf().setChapterList(chapters);
        mPresenter.getBookShelf().upDurChapterName();
        mPresenter.getBookShelf().upLastChapterName();
        showHideUrlViews();
        BookShelfHolder.get().post(mPresenter.getBookShelf());
    }

    @Override
    public void onPageCountChange(int count) {
        setReadProgress(Math.max(0, count - 1), 0, mPageLoader.isPageScrollable());
    }

    @Override
    public void onPageChange(int chapterIndex, int pageIndex, int pageSize) {
        mPresenter.getBookShelf().setDurChapter(chapterIndex);
        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
        mPresenter.getBookShelf().upDurChapterName();
        mPresenter.saveProgress();

        readStatusBar.updateChapterInfo(mPresenter.getBookShelf(), pageSize);

        setReadProgress(pageSize, pageIndex, mPageLoader.isPageScrollable());

        //继续朗读
        if ((ReadAloudService.running) && pageIndex >= 0) {
            String content = mPageLoader.getContent(pageIndex);
            if (content != null) {
                ReadAloudService.play(ReadBookActivity.this, false, content,
                        mPresenter.getBookShelf().getBookInfoBean().getName(),
                        mPresenter.getChapterTitle(chapterIndex),
                        mPresenter.getBookShelf().getBookInfoBean().getRealCoverUrl()
                );
            }
            return;
        }
        autoPage();
    }

    @Override
    protected void bindEvent() {
        //打开URL
        atvLayout.setOnClickListener(this);

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

        //目录
        btnCatalog.setOnClickListener(this);

        //亮度
        btnLight.setOnClickListener(this);

        //界面
        btnFont.setOnClickListener(this);

        //设置
        btnSetting.setOnClickListener(this);

        //菜单
        controlsBackView.setOnClickListener(this);

        NoDoubleClickListener clickListener = new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                switch (v.getId()) {
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
                }
            }
        };

        //上一章
        tvPre.setOnClickListener(clickListener);

        //下一章
        tvNext.setOnClickListener(clickListener);

        //动态设置状态栏，导航栏
        controlsView.setOnInsetsCallback(insets -> {
            appBar.setPadding(0, insets.top, 0, 0);
            navigationBar.setPadding(0, 0, 0, insets.bottom);
        });

        //阅读进度
        hpbReadProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mPageLoader != null) {
                        if (progress != mPresenter.getBookShelf().getDurChapterPage()) {
                            mPageLoader.skipToPage(progress);
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
                initImmersionBar();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                initImmersionBar();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (chapterFragment != null && !drawerLayout.isDrawerOpen(GravityCompat.START) && newState == DrawerLayout.STATE_SETTLING) {
                    BookShelfHolder.get().post(mPresenter.getBookShelf());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_controls_back:
                popMenuOut();
                break;
            case R.id.atv_layout:
                openCurrentChapterBrowser();
                break;
            case R.id.fab_read_aloud_timer:
                ReadAloudService.setTimer(this);
                break;
            case R.id.fabReadAloud:
                onMediaPlay();
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
                controlsView.postDelayed(() -> ReplaceRuleActivity.startThis(this), DELAY_SHORT);
                break;
            case R.id.fabNightTheme:
                popMenuOut();
                controlsView.postDelayed(() -> setNightTheme(!isNightTheme()), DELAY_LONG);
                break;
            case R.id.btn_catalog:
                isOrWillShow = true;
                popMenuOut();
                if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().realChapterListEmpty()) {
                    controlsView.postDelayed(() -> drawerLayout.openDrawer(GravityCompat.START), DELAY_SHORT);
                }
                break;
            case R.id.btn_light:
                isOrWillShow = true;
                popMenuOut();
                ensureReadAdjustPop();
                controlsView.postDelayed(() -> {
                    readAdjustPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, DELAY_SHORT);
                break;
            case R.id.btn_font:
                isOrWillShow = true;
                popMenuOut();
                ensureReadInterfacePop();
                controlsView.postDelayed(() -> {
                    readInterfacePop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, DELAY_SHORT);
                break;
            case R.id.btn_setting:
                isOrWillShow = true;
                popMenuOut();
                ensureMoreSettingPop();
                controlsView.postDelayed(() -> {
                    moreSettingPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    initImmersionBar();
                }, DELAY_SHORT);
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
        setReadProgress(chapterProgressMax, chapterDurProgress, chapterDraggable);

        initPageView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_read_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mPresenter.getBookShelf() == null || mPresenter.getBookShelf().isLocalBook()) {
            for (int i = 0; i < menu.size(); i++) {
                if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                    menu.getItem(i).setVisible(false);
                    menu.getItem(i).setEnabled(false);
                }
            }
            if (mPresenter.getBookShelf() != null) {
                MenuItem charsetItem = menu.findItem(R.id.edit_charset);
                charsetItem.setVisible(true);
                charsetItem.setEnabled(true);
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
                    String content = mPageLoader.getCurrentContent();
                    if (!TextUtils.isEmpty(content)) {
                        LargeTextDialog.show(getSupportFragmentManager(), content, false);
                    }
                }
                break;
            case R.id.disable_book_source:
                mPresenter.disableDurBookSource();
                break;
            case R.id.edit_charset:
                setCharset();
                break;
            case R.id.action_clean_cache:
                new AlertDialog.Builder(getSupportFragmentManager())
                        .setTitle(R.string.dialog_title)
                        .setMessage(R.string.clean_book_cache_s)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.cleanCache())
                        .show();
                break;
            case R.id.action_book_info:
                BookInfoActivity.startThis(this, mPresenter.getBookShelf().copy());
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setReadProgress(int max, int progress, boolean enabled) {
        hpbReadProgress.setMax(max);
        hpbReadProgress.setProgress(max == 1 ? 1 : progress);
        hpbReadProgress.setEnabled(enabled && max > 1);
    }

    private void addChapterFragment(ChapterDrawerFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content, fragment, ChapterDrawerFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }


    private void openCurrentChapterBrowser() {
        String url = atvUrl.getText().toString();
        if (StringUtils.isBlank(url)) {
            return;
        }
        BookSourceBean bookSource = mPresenter.getBookSource();
        final String title = mPresenter.getBookShelf().getBookInfoBean().getName();
        WebLoadConfig config = new WebLoadConfig(title, url, bookSource == null ? null : bookSource.getBookSourceUrl(), bookSource == null ? null : bookSource.getHttpUserAgent());
        WebViewActivity.startThis(this, config);
    }

    /**
     * 刷新当前章节
     */
    private void refreshDurChapter() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法刷新当前章节");
            return;
        }
        popMenuOut();
        if (mPageLoader != null) {
            mHandler.postDelayed(() -> mPageLoader.refreshDurChapter(), DELAY_SHORT);
        }
    }

    /**
     * 书签
     */
    @Override
    public void showBookmark(BookmarkBean bookmarkBean) {
        this.popMenuOut();
        boolean isAdd = false;
        if (mPresenter.getBookShelf() != null) {
            if (!mPresenter.inBookShelf()) {
                toast("请先将书籍加入书架");
                return;
            }

            if (bookmarkBean == null) {
                isAdd = true;
                bookmarkBean = new BookmarkBean();
                bookmarkBean.setNoteUrl(mPresenter.getBookShelf().getNoteUrl());
                bookmarkBean.setBookName(mPresenter.getBookShelf().getBookInfoBean().getName());
                bookmarkBean.setChapterIndex(mPresenter.getBookShelf().getDurChapter());
                bookmarkBean.setPageIndex(mPresenter.getBookShelf().getDurChapterPage());
                bookmarkBean.setChapterName(mPresenter.getChapterTitle(mPresenter.getBookShelf().getDurChapter()));
            }

            BookmarkDialog.show(getSupportFragmentManager(), bookmarkBean, isAdd, new BookmarkDialog.OnBookmarkClick() {
                @Override
                public void saveBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.saveBookmark(bookmarkBean);
                }

                @Override
                public void delBookmark(BookmarkBean bookmarkBean) {
                    mPresenter.delBookmark(bookmarkBean);
                }

                @Override
                public void openBookmark(BookmarkBean bookmarkBean) {
                    closeDrawer();
                    drawerLayout.postDelayed(() -> mPageLoader.skipToChapter(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex()), DELAY_MIDDLE);
                }
            });
        }

    }

    @Override
    public void openChapter(ChapterBean chapterBean) {
        closeDrawer();
        if (mPageLoader != null) {
            drawerLayout.postDelayed(() -> mPageLoader.skipToChapter(chapterBean.getDurChapterIndex(), 0), DELAY_MIDDLE);
        }
    }

    @Override
    public void openBookmark(BookmarkBean bookmarkBean) {
        closeDrawer();

        if (mPageLoader != null) {
            drawerLayout.postDelayed(() -> mPageLoader.skipToChapter(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex()), DELAY_MIDDLE);
        }
    }

    @Override
    public void updateBookmark(BookShelfBean bookShelfBean) {
        BookShelfHolder.get().post(bookShelfBean);
    }

    private void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
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
            ChangeSourceDialog.show(getSupportFragmentManager(), mPresenter.getBookShelf().getBookInfoBean(), false, searchBookBean -> {
                mPageLoader.cancelRequest();
                mPageLoader.setCurrentStatus(PageStatus.STATUS_HY);
                mPresenter.changeBookSource(searchBookBean);
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
            DownLoadDialog.show(getSupportFragmentManager(), mPresenter.getBookShelf().getDurChapter(), mPresenter.getBookShelf().getChapterListSize()
                    , (start, end) -> {
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
            final String charset = mPresenter.getBookShelf().getBookInfoBean().getCharset();
            InputDialog.show(getSupportFragmentManager(), getString(R.string.edit_charset), charset,
                    new String[]{"UTF-8", "GB2312", "GBK", "Unicode", "UTF-16", "ASCII"}, inputText -> {
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
                                BookshelfHelp.cleanBookCache(mPresenter.getBookShelf());
                                finish();
                            }

                            @Override
                            public void clickAddShelf() {
                                mPresenter.addToShelf(null);
                                checkAddShelfPop.dismiss();
                            }
                        }, false);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (ReadAloudService.running && aloudStatus == PLAY) {
                ReadAloudService.pause(this);
                toast(getString(R.string.read_aloud_pause));
                return true;
            } else {
                onBackPressed();
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
                if (mPageLoader != null) {
                    mPageLoader.skipToNextPage();
                }
                return true;
            } else if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.PLAY) && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (mPageLoader != null) {
                    mPageLoader.skipToPrePage();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_N) {
                if (mPageLoader != null) {
                    mPageLoader.skipToNextPage();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_P) {
                if (mPageLoader != null) {
                    mPageLoader.skipToPrePage();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
                if (mPageLoader != null) {
                    mPageLoader.skipToNextPage();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
            return;
        }

        onMediaPlay();
    }

    private void onMediaPlay() {
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
                    controlsView.postDelayed(() -> ReadAloudService.play(ReadBookActivity.this, true, mPageLoader.getCurrentContent(),
                            mPresenter.getBookShelf().getBookInfoBean().getName(),
                            mPresenter.getChapterTitle(mPageLoader.getChapterPosition()),
                            mPresenter.getBookShelf().getBookInfoBean().getRealCoverUrl()
                    ), DELAY_SHORT);
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
        if (mNextPageRunnable != null) {
            mHandler.removeCallbacks(mNextPageRunnable);
        }
        if (mScreenOnRunnable != null) {
            mHandler.removeCallbacks(mScreenOnRunnable);
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

        BookShelfHolder.get().clear();
    }

    @Override
    public void onBackPressed() {
        if (!checkAddShelf()) {
            return;
        }
        finish();
    }

    /**
     * 结束
     */
    @Override
    public void finish() {
        BitIntentDataManager.getInstance().cleanAllData();
        if (AppActivityManager.getInstance().indexOf(this) == 0
                && AppActivityManager.getInstance().size() == 1) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivityByAnim(intent, R.anim.anim_alpha_in, R.anim.anim_alpha_out);
            super.finishByAnim(R.anim.anim_alpha_in, R.anim.anim_alpha_out);
        } else {
            super.finish();
        }
    }

    @Override
    public void stopRefreshChapterList() {
        if (mPageLoader != null) {
            mPageLoader.cancelRequest();
        }
    }

    @Override
    public void changeSourceFinish(boolean success, String errorMsg) {
        supportInvalidateOptionsMenu();
        if (mPageLoader != null) {
            if (success) {
                mPageLoader.changeSourceFinish(mPresenter.getBookShelf());
            } else {
                if (mPageLoader.hasCurrentChapter()) {
                    toast(errorMsg == null ? "换源失败，请选择其他书源" : errorMsg);
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
        public void onReceive(Context context, Intent intent) {
            if (readStatusBar != null) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    readStatusBar.updateTime();
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    readStatusBar.updateBattery(level);
                }
            }
        }

        public void registerReceiverBatInfo() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batInfoReceiver, filter);
            if (readStatusBar != null) {
                readStatusBar.updateTime();
            }
        }

    }
}