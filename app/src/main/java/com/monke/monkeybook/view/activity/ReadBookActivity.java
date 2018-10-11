//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.MApplication;
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
import com.monke.monkeybook.utils.BatteryUtil;
import com.monke.monkeybook.utils.SystemUtil;
import com.monke.monkeybook.utils.barUtil.BarHide;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.view.popupwindow.MoreSettingPop;
import com.monke.monkeybook.view.popupwindow.ReadAdjustPop;
import com.monke.monkeybook.view.popupwindow.ReadInterfacePop;
import com.monke.monkeybook.widget.ChapterListView;
import com.monke.monkeybook.widget.modialog.EditBookmarkView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;
import com.monke.monkeybook.widget.page.PageLoader;
import com.monke.monkeybook.widget.page.PageView;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.text.TextUtils.isEmpty;
import static com.monke.monkeybook.presenter.ReadBookPresenterImpl.OPEN_FROM_APP;
import static com.monke.monkeybook.presenter.ReadBookPresenterImpl.OPEN_FROM_OTHER;
import static com.monke.monkeybook.service.ReadAloudService.NEXT;
import static com.monke.monkeybook.service.ReadAloudService.PAUSE;
import static com.monke.monkeybook.service.ReadAloudService.PLAY;
import static com.monke.monkeybook.utils.NetworkUtil.isNetworkAvailable;

public class ReadBookActivity extends MBaseActivity<ReadBookContract.Presenter> implements ReadBookContract.View {

    @BindView(R.id.fl_content)
    FrameLayout flContent;
    @BindView(R.id.ll_menu_bottom)
    LinearLayout llMenuBottom;
    @BindView(R.id.tv_pre)
    TextView tvPre;
    @BindView(R.id.tv_next)
    TextView tvNext;
    @BindView(R.id.hpb_read_progress)
    MHorProgressBar hpbReadProgress;
    @BindView(R.id.ll_catalog)
    LinearLayout llCatalog;
    @BindView(R.id.ll_light)
    LinearLayout llLight;
    @BindView(R.id.ll_font)
    LinearLayout llFont;
    @BindView(R.id.ll_setting)
    LinearLayout llSetting;
    @BindView(R.id.tv_read_aloud_timer)
    TextView tvReadAloudTimer;
    @BindView(R.id.ll_read_aloud_timer)
    LinearLayout llReadAloudTimer;
    @BindView(R.id.ivCList)
    ImageView ivCList;
    @BindView(R.id.ivAdjust)
    ImageView ivAdjust;
    @BindView(R.id.ivInterface)
    ImageView ivInterface;
    @BindView(R.id.ivSetting)
    ImageView ivSetting;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.atv_divider)
    View atvDivider;
    @BindView(R.id.atv_url)
    TextView atvUrl;
    @BindView(R.id.ll_menu_top)
    LinearLayout llMenuTop;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
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
    @BindView(R.id.clp_chapterList_viewStub)
    ViewStub chapterListViewStub;

    private ChapterListView chapterListView;

    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;
    private ActionBar actionBar;
    private PageLoader mPageLoader;

    private Handler mHandler;
    private Runnable keepScreenRunnable;
    private Runnable upHpbNextPage;

    private String noteUrl;
    private Boolean isAdd = false; //判断是否已经添加进书架
    private int aloudStatus;
    private int screenTimeOut;
    private int nextPageTime;
    private int upHpbInterval = 100;

    private Menu menu;
    private CheckAddShelfPop checkAddShelfPop;
    private ReadAdjustPop readAdjustPop;
    private ReadInterfacePop readInterfacePop;
    private MoreSettingPop moreSettingPop;
    private MoProgressHUD moProgressHUD;
    private ThisBatInfoReceiver batInfoReceiver;
    private ReadBookControl readBookControl = ReadBookControl.getInstance();

    private Boolean showCheckPermission = false;
    private boolean autoPage = false;

    private boolean isFirstIn = true;

    @Override
    protected ReadBookContract.Presenter initInjector() {
        return new ReadBookPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            noteUrl = savedInstanceState.getString("noteUrl");
            aloudStatus = savedInstanceState.getInt("aloudStatus");
            isAdd = savedInstanceState.getBoolean("isAdd");
        }
        readBookControl.setLineChange(System.currentTimeMillis());
        readBookControl.initPageConfiguration();
        screenTimeOut = getResources().getIntArray(R.array.screen_time_out_value)[readBookControl.getScreenTimeOut()];
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_read);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            outState.putString("noteUrl", mPresenter.getBookShelf().getNoteUrl());
            outState.putInt("aloudStatus", aloudStatus);
            outState.putBoolean("isAdd", isAdd);

            BookShelfDataHolder.getInstance().setBookShelf(mPresenter.getBookShelf());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            initImmersionBar();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        pageView.invalidate();
    }

    /**
     * 状态栏
     */
    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        mImmersionBar.fullScreen(true);

        if (isMenuShowing() || isPopShowing()) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            if (isMenuShowing()) {
                mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
            } else if (isPopShowing()) {
                if (readBookControl.getHideStatusBar()) {
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_BAR);
                } else {
                    mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
                }
            }
        } else if (isChapterListShowing()) {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }

            mImmersionBar.hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR);
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

    public void keepScreenOn(boolean keepScreenOn) {
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
            mHandler.postDelayed(upHpbNextPage, upHpbInterval);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page_stop);
            fabAutoPage.getDrawable().mutate();
            fabAutoPage.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page_stop));
        } else {
            hpbNextPageProgress.setVisibility(View.INVISIBLE);
            fabAutoPage.setImageResource(R.drawable.ic_auto_page);
            fabAutoPage.getDrawable().mutate();
            fabAutoPage.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
            fabAutoPage.setContentDescription(getString(R.string.auto_next_page));
        }
    }

    private void upHpbNextPage() {
        nextPageTime = nextPageTime - upHpbInterval;
        hpbNextPageProgress.setDurProgress(nextPageTime);
        mHandler.postDelayed(upHpbNextPage, upHpbInterval);
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
        setSupportActionBar(toolbar);
        setupActionBar();
        appBar.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0);
        //图标眷色
        ivCList.getDrawable().mutate();
        ivCList.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivAdjust.getDrawable().mutate();
        ivAdjust.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivInterface.getDrawable().mutate();
        ivInterface.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        ivSetting.getDrawable().mutate();
        ivSetting.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        if (isNightTheme()) {
            fabNightTheme.setImageResource(R.drawable.ic_daytime_24dp);
        } else {
            fabNightTheme.setImageResource(R.drawable.ic_brightness);
        }

        pageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);


        mPresenter.initData(this);

        if (mPresenter.getOpen_from() == OPEN_FROM_APP) {
            mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());
        }
    }

    /**
     * 菜单是否显示
     *
     * @return
     */
    private boolean isMenuShowing() {
        return llMenuTop.getVisibility() == View.VISIBLE && llMenuBottom.getVisibility() == View.VISIBLE;
    }

    private boolean isPopShowing() {
        return (readAdjustPop != null && readAdjustPop.isShowing())
                || (readInterfacePop != null && readInterfacePop.isShowing())
                || (moreSettingPop != null && moreSettingPop.isShowing());
    }

    /**
     * 目录是否显示
     *
     * @return
     */
    private boolean isChapterListShowing() {
        return chapterListView != null && chapterListView.isShowing();
    }

    /**
     * 隐藏目录
     *
     * @return
     */
    private boolean dismissChapterList() {
        return chapterListView != null && chapterListView.dismissChapterList();
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
                    llMenuTop.setVisibility(View.INVISIBLE);
                    initImmersionBar();
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
                    llMenuBottom.setVisibility(View.INVISIBLE);
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
                if (mPageLoader != null) {
                    mPageLoader.upMargin();
                }
            }

            @Override
            public void bgChange() {
                initImmersionBar();
                if (mPageLoader != null) {
                    mPageLoader.setPageStyle(false);
                }
            }

            @Override
            public void refresh() {
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
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
                if (mPageLoader != null) {
                    mPageLoader.refreshUi();
                }
            }

        });
    }

    /**
     * 弹窗
     */
    private void ensureProgressHUD() {
        if (moProgressHUD != null) {
            return;
        }
        moProgressHUD = new MoProgressHUD(this);
    }

    @Override
    public void setHpbReadProgressMax(int count) {
        hpbReadProgress.setMaxProgress(count);
    }

    /**
     * 加载阅读页面
     */
    private void initPageView() {
        //获取页面加载器
        if (mPageLoader == null) {
            mPageLoader = pageView.getPageLoader(this, mPresenter.getBookShelf());
        }
        mPageLoader.updateBattery(BatteryUtil.getLevel(this));
        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {

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
                    public void requestChapter(int chapterIndex) {
                        if (isNetworkAvailable()) {
                            mPresenter.loadContent(chapterIndex);
                        }
                    }

                    @Override
                    public void onCategoryFinish(List<ChapterListBean> chapters) {
                        updateTitle(mPresenter.getBookShelf().getBookInfoBean().getName());
                        mPresenter.getBookShelf().getBookInfoBean().setChapterList(chapters);
                        mPresenter.getBookShelf().setChapterListSize(chapters.size());
                        mPresenter.getBookShelf().upLastChapterName();
                        initChapterList();
                    }

                    @Override
                    public void onPageCountChange(int count) {
                        hpbReadProgress.setMaxProgress(Math.max(0, count - 1));
                        hpbReadProgress.setDurProgress(0);
                        hpbReadProgress.setEnabled(!mPageLoader.isPageFrozen());
                    }

                    @Override
                    public void onPageChange(int chapterIndex, int pageIndex) {
                        mPresenter.getBookShelf().setDurChapter(chapterIndex);
                        mPresenter.getBookShelf().setDurChapterPage(pageIndex);
                        mPresenter.saveProgress();
                        hpbReadProgress.post(
                                () -> hpbReadProgress.setDurProgress(pageIndex)
                        );
                        //继续朗读
                        if ((ReadAloudService.running) && pageIndex >= 0) {
                            if (mPageLoader.getContent(pageIndex) != null) {
                                ReadAloudService.play(ReadBookActivity.this,
                                        false,
                                        mPageLoader.getContent(pageIndex),
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
                    public void onPageDrawFinish() {
                        if (isFirstIn && !mPresenter.isRecreate()) {
                            isFirstIn = false;
                            ObjectAnimator animator = ObjectAnimator.ofFloat(pageView, "alpha", 0.2F, 1.0F);
                            animator.setDuration(300L);
                            animator.start();
                        }
                    }
                }
        );
        pageView.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                screenOffTimerStart();
                if (isMenuShowing()) {
                    popMenuOut();
                    return false;
                }
                return true;
            }

            @Override
            public void center() {
                toggleMenu();
            }

        });
        mPageLoader.refreshChapterList();
    }

    /**
     * 初始化目录列表
     */
    @Override
    public void initChapterList() {
        if (chapterListView == null) {
            chapterListViewStub.inflate();
            chapterListView = findViewById(R.id.clp_chapterList);
        }

        chapterListView.setOnChangeListener(new ChapterListView.OnChangeListener() {
            @Override
            public void animIn() {
                initImmersionBar();
            }

            @Override
            public void animOut() {
                initImmersionBar();
            }
        });
        chapterListView.setData(mPresenter.getBookShelf(), new ChapterListView.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                mPageLoader.skipToChapter(index, page);
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                showBookmark(bookmarkBean);
            }
        });
    }

    @Override
    protected void bindEvent() {
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

        //打开URL
        atvUrl.setOnClickListener(view -> {
            try {
                String url = atvUrl.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                toast(getString(R.string.can_not_open));
            }
        });

        //朗读定时
        fabReadAloudTimer.getDrawable().mutate();
        fabReadAloudTimer.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReadAloudTimer.setOnClickListener(view -> ReadAloudService.setTimer(this));

        //朗读
        fabReadAloud.getDrawable().mutate();
        fabReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReadAloud.setOnClickListener(view -> onMediaButton());
        //长按停止朗读
        fabReadAloud.setOnLongClickListener(view -> {
            if (ReadAloudService.running) {
                toast(getString(R.string.aloud_stop));
                ReadAloudService.stop(this);
            } else {
                toast(getString(R.string.read_aloud));
            }
            return true;
        });

        //自动翻页
        fabAutoPage.getDrawable().mutate();
        fabAutoPage.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabAutoPage.setOnClickListener(view -> {
            if (ReadAloudService.running) {
                Toast.makeText(this, "朗读正在运行,不能自动翻页", Toast.LENGTH_SHORT).show();
                return;
            }
            autoPage = !autoPage;
            autoPage();
            popMenuOut();
        });
        fabAutoPage.setOnLongClickListener(view -> {
            toast(getString(R.string.auto_next_page));
            return true;
        });

        //替换
        fabReplaceRule.getDrawable().mutate();
        fabReplaceRule.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabReplaceRule.setOnClickListener(view -> {
            popMenuOut();
            ReplaceRuleActivity.startThis(this);
        });
        fabReplaceRule.setOnLongClickListener(view -> {
            toast(getString(R.string.replace_rule_title));
            return true;
        });

        //夜间模式
        fabNightTheme.getDrawable().mutate();
        fabNightTheme.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        fabNightTheme.setOnClickListener(view -> {
            popMenuOut();
            new Handler().postDelayed(() -> setNightTheme(!isNightTheme()), 200L);
        });
        fabNightTheme.setOnLongClickListener(view -> {
            toast(getString(R.string.night_theme));
            return true;
        });

        //上一章
        tvPre.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                mPageLoader.skipPreChapter();
            }
        });

        //下一章
        tvNext.setOnClickListener(view -> {
            if (mPresenter.getBookShelf() != null) {
                mPageLoader.skipNextChapter();
            }
        });

        //目录
        llCatalog.setOnClickListener(view -> {
            popMenuOut();
            if (chapterListView != null) {
                new Handler().postDelayed(() -> chapterListView.show(mPresenter.getBookShelf().getDurChapter()), 200L);
            }
        });

        //亮度
        llLight.setOnClickListener(view -> {
            ensureReadAdjustPop();
            popMenuOut();
            new Handler().postDelayed(() -> readAdjustPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), 200L);
        });

        //界面
        llFont.setOnClickListener(view -> {
            ensureReadInterfacePop();
            popMenuOut();
            new Handler().postDelayed(() -> readInterfacePop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), 200L);
        });

        //设置
        llSetting.setOnClickListener(view -> {
            ensureMoreSettingPop();
            popMenuOut();
            new Handler().postDelayed(() -> moreSettingPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0), 200L);
        });

        tvReadAloudTimer.setOnClickListener(null);
    }

    @Override
    public void postCheckInShelf() {
        getWindow().getDecorView().post(() -> mPresenter.checkInShelf());
    }

    @Override
    public void startLoadingBook() {
        initPageView();
    }

    //设置ToolBar
    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_read_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        showMenu();
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
                    moProgressHUD.showText(mPageLoader.getContent(mPageLoader.getPagePos()));
                }
                break;
            case R.id.disable_book_source:
                mPresenter.disableDurBookSource();
                break;
            case R.id.action_book_info:
                BookInfoActivity.startThis(this, mPresenter.getBookShelf().getNoteUrl());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
            mPageLoader.refreshDurChapter();
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
            moProgressHUD.showBookmark(bookmarkBean, isAdd, new EditBookmarkView.OnBookmarkClick() {
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
            toast("网络不可用，无法换源!");
            return;
        }
        popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            ensureProgressHUD();
            moProgressHUD.showChangeSource(this, mPresenter.getBookShelf(), searchBookBean -> {
                if (!Objects.equals(searchBookBean.getNoteUrl(), mPresenter.getBookShelf().getNoteUrl())) {
                    mPageLoader.setStatus(PageLoader.STATUS_HY);
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

        if (!mPageLoader.isChapterListPrepare()) {
            toast("书籍目录获取失败，无法下载");
            return;
        }

        popMenuOut();
        if (mPresenter.getBookShelf() != null) {
            //弹出离线下载界面
            int endIndex = mPresenter.getBookShelf().getChapterListSize() - 1;

            ensureProgressHUD();
            moProgressHUD.showDownloadList(mPresenter.getBookShelf().getDurChapter(), endIndex,
                    mPresenter.getBookShelf().getChapterListSize(),
                    (start, end) -> {
                        moProgressHUD.dismiss();
                        mPresenter.addDownload(start, end);
                    });
        }
    }

    /**
     * 隐藏菜单
     */
    private boolean popMenuOut() {
        if (isMenuShowing()) {
            ensureMenuOutAnim();
            llMenuTop.startAnimation(menuTopOut);
            llMenuBottom.startAnimation(menuBottomOut);
            return true;
        }
        return false;
    }

    /**
     * 显示菜单
     */
    private boolean popMenuIn() {
        if (!isMenuShowing()) {
            ensureMenuInAnim();
            llMenuTop.setVisibility(View.VISIBLE);
            llMenuBottom.setVisibility(View.VISIBLE);
            llMenuTop.startAnimation(menuTopIn);
            llMenuBottom.startAnimation(menuBottomIn);
            return true;
        }
        return false;
    }

    private void toggleMenu() {
        if (!popMenuIn()) {
            popMenuOut();
        }
    }

    @Override
    public void updateTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
                fabReadAloud.setImageResource(R.drawable.ic_pause2);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            case PAUSE:
                fabReadAloud.setImageResource(R.drawable.ic_play2);
                llReadAloudTimer.setVisibility(View.VISIBLE);
                break;
            default:
                fabReadAloud.setImageResource(R.drawable.ic_read_aloud);
                llReadAloudTimer.setVisibility(View.INVISIBLE);
        }
        fabReadAloud.getDrawable().mutate();
        fabReadAloud.getDrawable().setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
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
                mPageLoader.refreshUi();
            }
            if (readInterfacePop != null) {
                readInterfacePop.setBg();
            }
            initImmersionBar();
        }
    }

    /**
     * 检查是否加入书架
     */
    public boolean checkAddShelf() {
        if (isAdd || mPresenter.getBookShelf() == null) {
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
        ensureProgressHUD();
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (popMenuOut()) {
                    return true;
                } else if (dismissChapterList()) {
                    return true;
                } else if (ReadAloudService.running && aloudStatus == PLAY) {
                    ReadAloudService.pause(this);
                    toast(getString(R.string.read_aloud_pause));
                    return true;
                } else {
                    finish();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                toggleMenu();
                return true;
            } else if (!isMenuShowing() && !isChapterListShowing()) {
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
        if (!isMenuShowing() && !isChapterListShowing()) {
            if (readBookControl.getCanKeyTurn(aloudStatus == ReadAloudService.PLAY)
                    && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void showMenu() {
        if (mPresenter.getBookShelf() != null && !mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvDivider.setVisibility(View.VISIBLE);
            atvUrl.setVisibility(View.VISIBLE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                        menu.getItem(i).setVisible(true);
                        menu.getItem(i).setEnabled(true);
                    }
                }
            }
        } else if (mPresenter.getBookShelf() != null && mPresenter.getBookShelf().getTag().equals(BookShelfBean.LOCAL_TAG)) {
            atvDivider.setVisibility(View.GONE);
            atvUrl.setVisibility(View.GONE);
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    if (menu.getItem(i).getGroupId() == R.id.menuOnLine) {
                        menu.getItem(i).setVisible(false);
                        menu.getItem(i).setEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    public String getNoteUrl() {
        if (isEmpty(noteUrl)) {
            noteUrl = readBookControl.getLastNoteUrl();
        }
        return noteUrl;
    }

    @Override
    public Boolean getAdd() {
        return isAdd;
    }

    @Override
    public void setAdd(Boolean isAdd) {
        this.isAdd = isAdd;
    }

    @Override
    public void finishContent(int chapter) {
        if (mPageLoader != null
                && mPageLoader.getChapterPos() == chapter
                && mPageLoader.getChapterPageStatus() != PageLoader.STATUS_FINISH) {
            mPageLoader.openChapter(chapter);
        }
    }

    @Override
    public void chapterError(int chapter, int status) {
        if (mPageLoader != null) {
            mPageLoader.setStatus(chapter, status);
        }
    }

    @Override
    public void openBookFromOther() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            mPresenter.openBookFromOther(this);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.open_from_other),
                    MApplication.RESULT__PERMS, MApplication.PerList);
        }
    }

    /**
     * 更新目录
     */
    @Override
    public void chapterChange(ChapterListBean chapterListBean) {
        if (chapterListView != null && chapterListView.hasData()) {
            chapterListView.upChapterList(chapterListBean);
        }
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
                    ReadAloudService.play(this, true, mPageLoader.getContent(mPageLoader.getPagePos()),
                            mPresenter.getBookShelf().getBookInfoBean().getName(),
                            mPresenter.getChapterTitle(mPageLoader.getChapterPos())
                    );
                }
        }
    }

    @AfterPermissionGranted(MApplication.RESULT__PERMS)
    private void onResultOpenOtherPerms() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            toast("获取权限成功");
        } else {
            toast("未获取到权限");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initImmersionBar();
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
        if (mPageLoader != null) {
            mPageLoader.updateBattery(BatteryUtil.getLevel(this));
        }
        if (showCheckPermission && mPresenter.getOpen_from() == OPEN_FROM_OTHER && EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            showCheckPermission = true;
            mPresenter.openBookFromOther(this);
        }
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
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }


    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void changeSourceFinish(boolean success) {
        if (mPageLoader != null) {
            if (success) {
                mPageLoader.changeSourceFinish(mPresenter.getBookShelf());
            } else {
                mPageLoader.setStatus(PageLoader.STATUS_FINISH);
            }
        }
    }

    /**
     * 时间和电量广播
     */
    class ThisBatInfoReceiver extends BroadcastReceiver {

        long currentTime = System.currentTimeMillis();

        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (readBookControl.getHideStatusBar() && System.currentTimeMillis() - currentTime > 60 * 1000) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    if (mPageLoader != null) {
                        mPageLoader.updateTime();
                    }
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    if (mPageLoader != null) {
                        mPageLoader.updateBattery(level);
                    }
                }
                currentTime = System.currentTimeMillis();
            }
        }

        public void registerReceiverBatInfo() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batInfoReceiver, filter);
        }

    }
}