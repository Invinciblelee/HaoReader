//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.MainPresenterImpl;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;
import com.monke.monkeybook.widget.BookShelfSearchView;
import com.monke.monkeybook.widget.ScrimInsetsFrameLayout;
import com.monke.monkeybook.widget.ViewCompat;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends MBaseActivity<MainContract.Presenter> implements MainContract.View {
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;
    private static final int REQUEST_BOOKSHELF_PX = 14;
    private static final int REQUEST_CODE_SIGN_IN = 15;

    @BindView(R.id.layout_container)
    ScrimInsetsFrameLayout container;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView drawerLeft;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;
    @BindView(R.id.main_view)
    LinearLayout mainView;
    @BindView(R.id.bookshelf_search_view)
    BookShelfSearchView drawerRight;

    private TextView tvUser;
    private Switch swNightTheme;
    private int group = -1;
    private BookShelfGridAdapter bookShelfGridAdapter;
    private BookShelfListAdapter bookShelfListAdapter;
    private boolean viewIsList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MoProgressHUD moProgressHUD;
    private long exitTime = 0;
    private String bookPx;
    private boolean isRecreate;

    @Override
    protected MainContract.Presenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            group = savedInstanceState.getInt("group", -1);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("group", group);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_main);
    }

    /**
     * 沉浸状态栏
     */
    @Override
    public void initImmersionBar() {
        super.initImmersionBar();
    }

    @Override
    protected void initData() {
        viewIsList = preferences.getBoolean("bookshelfIsList", true);
        bookPx = preferences.getString(getString(R.string.pk_bookshelf_px), "0");
        if (group == -1) {
            group = preferences.getInt("shelf_group", 0);
        }
        isRecreate = getIntent().getBooleanExtra("isRecreate", false);
        getIntent().putExtra("isRecreate", true);
    }

    private List<BookShelfBean> getBookshelfList() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            return drawerRight.getBooks();
        }
        if (viewIsList) {
            return bookShelfListAdapter.getBooks();
        } else {
            return bookShelfGridAdapter.getBooks();
        }
    }

    private boolean getNeedAnim() {
        return preferences.getBoolean(getString(R.string.pk_bookshelf_anim), false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();
        initDrawer();
        upGroup(group);

        container.setOnInsetsCallback(insets -> {
            appBar.setPadding(0, insets.top, 0, 0);
            drawerLeft.getHeaderView(0).setPadding(0, insets.top, 0, 0);
            drawerRight.applyWindowInsets(insets);
        });

        if (viewIsList) {
            bookShelfListAdapter = new BookShelfListAdapter(this);
            rvBookshelf.setAdapter(bookShelfListAdapter);
            rvBookshelf.setLayoutManager(new LinearLayoutManager(this));
        } else {
            bookShelfGridAdapter = new BookShelfGridAdapter(this);
            rvBookshelf.setAdapter(bookShelfGridAdapter);
            rvBookshelf.setLayoutManager(new GridLayoutManager(this, 3));
        }

        moProgressHUD = new MoProgressHUD(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
        if (swNightTheme != null) {
            swNightTheme.setChecked(isNightTheme());
        }
    }

    @Override
    protected void bindEvent() {
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        if (bookPx.equals("2")) {
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        } else {
            itemTouchHelpCallback.setDragEnable(false);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
        }
        if (viewIsList) {
            bookShelfListAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfListAdapter.getItemTouchCallbackListener());
        } else {
            bookShelfGridAdapter.setItemClickListener(getAdapterListener());
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookShelfGridAdapter.getItemTouchCallbackListener());
        }

        drawerRight.setupItemClickListener(getAdapterListener());
    }

    private OnItemClickListenerTwo getAdapterListener() {
        return new OnItemClickListenerTwo() {
            @Override
            public void onClick(View view, int index) {
                KeyboardUtil.hideKeyboard(drawerRight.getSearchAutoComplete(false));
                BookShelfBean bookShelfBean = getBookshelfList().get(index);
                if (!mPresenter.checkLocalBookExist(bookShelfBean)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.delete_bookshelf)
                            .setMessage(R.string.delete_bookshelf_not_exist_s)
                            .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.removeFromBookSelf(bookShelfBean))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ReadBookActivity.class);
                    intent.putExtra("openFrom", ReadBookPresenterImpl.OPEN_FROM_APP);
                    intent.putExtra("inBookShelf", true);
                    String key = String.valueOf(System.currentTimeMillis());
                    intent.putExtra("data_key", key);
                    try {
                        BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                    } catch (CloneNotSupportedException e) {
                        BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                    }
                    startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }

            @Override
            public void onLongClick(View view, int index) {
                KeyboardUtil.hideKeyboard(drawerRight.getSearchAutoComplete(false));
                BookShelfBean bookShelf = getBookshelfList().get(index);
                if (bookShelf.getGroup() == 2) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.delete_bookshelf)
                            .setMessage(R.string.delete_bookshelf_s)
                            .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.removeFromBookSelf(bookShelf))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .show();
                } else {
                    Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                    intent.putExtra("openFrom", BookDetailPresenterImpl.FROM_BOOKSHELF);
                    String key = String.valueOf(System.currentTimeMillis());
                    intent.putExtra("data_key", key);
                    try {
                        BitIntentDataManager.getInstance().putData(key, bookShelf.clone());
                    } catch (CloneNotSupportedException e) {
                        BitIntentDataManager.getInstance().putData(key, bookShelf);
                    }
                    startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenu = menu.findItem(R.id.action_list_grid);
        if (viewIsList) {
            pauseMenu.setTitle(R.string.action_grid);
        } else {
            pauseMenu.setTitle(R.string.action_list);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //点击搜索
                startActivityByAnim(new Intent(this, SearchBookActivity.class),
                        toolbar, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.action_library:
                startActivity(new Intent(this, FindBookActivity.class));
                break;
            case R.id.action_add_local:
                if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                    startActivity(new Intent(this, ImportBookActivity.class));
                } else {
                    EasyPermissions.requestPermissions(this, "添加本地书籍",
                            FILE_SELECT_RESULT, MApplication.PerList);
                }
                break;
            case R.id.action_add_url:
                moProgressHUD.showInputBox("添加书籍网址", null, inputText -> mPresenter.addBookUrl(inputText));
                break;
            case R.id.action_list_grid:
                editor.putBoolean("bookshelfIsList", !viewIsList);
                editor.apply();
                recreate();
                break;
            case R.id.action_clearCaches:
                mPresenter.clearCaches();
                break;
            case R.id.action_clearBookshelf:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_bookshelf)
                        .setMessage(R.string.clear_bookshelf_s)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        })
                        .show();
                break;
            case R.id.action_refreshBookshelf:
                mPresenter.queryBookShelf(NetworkUtil.isNetworkAvailable(), group);
                if (!NetworkUtil.isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this, "无网络，请打开网络后再试。", Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)
                        ) {
                    drawer.closeDrawers();
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //初始化侧边栏
    private void initDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView == drawerRight) {
                    return;
                }
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView == drawerRight) {
                    if (slideOffset > 0) {
                        KeyboardUtil.hideKeyboard(getCurrentFocus());
                    }
                    return;
                }
                super.onDrawerSlide(drawerView, slideOffset);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView == drawerRight) {
                    KeyboardUtil.showKeyboard(drawerRight.getSearchAutoComplete(true));
                    return;
                }
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerToggle.syncState();
        drawer.addDrawerListener(mDrawerToggle);

        setUpNavigationView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void upGroup(int group) {
        if (this.group != group) {
            this.group = group;

            mPresenter.queryBookShelf(false, group);
        }

        switch (group) {
            case 1:
                drawerLeft.setCheckedItem(R.id.action_group_yf);
                break;
            case 0:
                drawerLeft.setCheckedItem(R.id.action_group_zg);
                break;
            default:
                drawerLeft.setCheckedItem(R.id.action_group_bd);
                break;
        }
    }

    //侧边栏按钮
    private void setUpNavigationView() {
        ViewCompat.setNavigationMenuLineStyle(drawerLeft, getResources().getColor(R.color.bg_divider_line), getResources().getDimensionPixelSize(R.dimen.line_height));
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        drawerLeft.addHeaderView(headerView);
        tvUser = headerView.findViewById(R.id.tv_user);
        ColorStateList colorStateList = getResources().getColorStateList(R.color.navigation_color);
        drawerLeft.setItemTextColor(colorStateList);
        drawerLeft.setItemIconTintList(colorStateList);
        Menu drawerMenu = drawerLeft.getMenu();
        swNightTheme = drawerMenu.findItem(R.id.action_night_theme).getActionView().findViewById(R.id.sw_night_theme);
        swNightTheme.setChecked(isNightTheme());
        swNightTheme.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                setNightTheme(b);
            }
        });
        drawerLeft.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_group_zg:
                    new Handler().postDelayed(() -> upGroup(0), 200L);
                    break;
                case R.id.action_group_yf:
                    new Handler().postDelayed(() -> upGroup(1), 200L);
                    break;
                case R.id.action_group_bd:
                    new Handler().postDelayed(() -> upGroup(2), 200L);
                    break;
                case R.id.action_download:
                    new Handler().postDelayed(() -> DownloadActivity.startThis(this), 200L);
                    break;
                case R.id.action_book_source_manage:
                    new Handler().postDelayed(() -> BookSourceActivity.startThis(this), 200L);
                    break;
                case R.id.action_replace_rule:
                    new Handler().postDelayed(() -> ReplaceRuleActivity.startThis(this), 200L);
                    break;
                case R.id.action_setting:
                    new Handler().postDelayed(() -> SettingActivity.startThis(this, REQUEST_BOOKSHELF_PX), 200L);
                    break;
                case R.id.action_about:
                    new Handler().postDelayed(() -> AboutActivity.startThis(this), 200L);
                    break;
                case R.id.action_donate:
                    new Handler().postDelayed(() -> DonateActivity.startThis(this), 200L);
                    break;
                case R.id.action_backup:
                    backup();
                    break;
                case R.id.action_restore:
                    restore();
                    break;
                case R.id.action_night_theme:
                    swNightTheme.setChecked(!isNightTheme());
                    setNightTheme(!isNightTheme());
                    break;
            }
            if (menuItem.getItemId() != R.id.action_night_theme) {
                drawer.closeDrawers();
            }
            return true;
        });
    }

    //备份
    private void backup() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.backup_confirmation)
                    .setMessage(R.string.backup_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.backupData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.backup_permission),
                    BACKUP_RESULT, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(BACKUP_RESULT)
    private void backupResult() {
        backup();
    }

    //恢复
    private void restore() {
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.restore_confirmation)
                    .setMessage(R.string.restore_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.restoreData())
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.restore_permission),
                    RESTORE_RESULT, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(RESTORE_RESULT)
    private void restoreResult() {
        restore();
    }

    @AfterPermissionGranted(FILE_SELECT_RESULT)
    private void fileSelectResult() {
        startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
    }

    private void versionUpRun() {
        if (preferences.getInt("versionCode", 0) != MApplication.getVersionCode()) {
            //保存版本号
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", MApplication.getVersionCode());
            editor.apply();
            //更新日志
            moProgressHUD.showAssetMarkdown("updateLog.md");
        }
    }

    private boolean haveRefresh() {
        return preferences.getBoolean(getString(R.string.pk_auto_refresh), false) && !isRecreate;
    }

    private boolean isDayNightChanged() {
        return isActNightTheme != isNightTheme();
    }

    private void startLayoutAnimationIfNeed() {
        if (getNeedAnim()) {
            if (rvBookshelf.getLayoutAnimation() == null) {
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.anim_bookshelf_layout);
                rvBookshelf.setLayoutAnimation(animation);
            } else {
                rvBookshelf.startLayoutAnimation();
            }
        } else {
            if (rvBookshelf.getLayoutAnimation() != null) {
                rvBookshelf.setLayoutAnimation(null);
            }
        }
    }

    @Override
    protected void firstRequest() {
        if (NetworkUtil.isNetworkAvailable()) {
            mPresenter.queryBookShelf(haveRefresh(), group);
        } else {
            mPresenter.queryBookShelf(false, group);
            if (haveRefresh()) {
                Toast.makeText(MainActivity.this, "无网络，自动刷新失败！", Toast.LENGTH_SHORT).show();
            }
        }

        getWindow().getDecorView().post(this::versionUpRun);
    }

    @Override
    public void refreshBookShelf(int group, List<BookShelfBean> bookShelfBeanList) {
        if (group != this.group) {
            mPresenter.queryBookShelf(false, this.group);
            return;
        }

        startLayoutAnimationIfNeed();

        if (viewIsList) {
            bookShelfListAdapter.replaceAll(bookShelfBeanList, bookPx);
        } else {
            bookShelfGridAdapter.replaceAll(bookShelfBeanList, bookPx);
        }
    }

    @Override
    public void updateBook(BookShelfBean bookShelfBean, boolean sort) {
        drawerRight.updateBook(bookShelfBean);
        if (isDayNightChanged() || bookShelfBean.getGroup() != this.group) {
            return;
        }
        if (viewIsList) {
            bookShelfListAdapter.updateBook(bookShelfBean, sort);
        } else {
            bookShelfGridAdapter.updateBook(bookShelfBean, sort);
        }
    }

    @Override
    public void addToBookShelf(BookShelfBean bookShelfBean) {
        drawerRight.addToBookShelfIfNeed(bookShelfBean);
        if (isDayNightChanged() || bookShelfBean.getGroup() != this.group) {
            return;
        }
        if (viewIsList) {
            bookShelfListAdapter.addBook(bookShelfBean);
        } else {
            bookShelfGridAdapter.addBook(bookShelfBean);
        }
    }

    @Override
    public void removeFromBookShelf(BookShelfBean bookShelfBean) {
        if (isDayNightChanged()) {
            return;
        }
        drawerRight.removeFromBookShelfIfNeed(bookShelfBean);
        if (viewIsList) {
            bookShelfListAdapter.removeBook(bookShelfBean);
        } else {
            bookShelfGridAdapter.removeBook(bookShelfBean);
        }
    }

    @Override
    public void sortBookShelf() {
        if (isDayNightChanged()) {
            return;
        }
        if (viewIsList) {
            bookShelfListAdapter.sort();
        } else {
            bookShelfGridAdapter.sort();
        }
    }

    @Override
    public void dismissHUD() {
        moProgressHUD.dismiss();
    }

    @Override
    public void refreshError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void onRestore(String msg) {
        moProgressHUD.showLoading(msg);
    }

    @Override
    public void restoreSuccess() {
        dismissHUD();

        initImmersionBar();
    }

    @Override
    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (drawer.isDrawerOpen(GravityCompat.START)
                        || drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawers();
                    return true;
                }
                exit();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    protected void onDestroy() {
        if (preferences != null) {
            preferences.edit().putInt("shelf_group", group).apply();
        }
        super.onDestroy();
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(rvBookshelf, "再按一次退出程序", Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BOOKSHELF_PX && resultCode == RESULT_OK) {
            recreate();
        }
    }

}