//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.permission.OnPermissionsGrantedCallback;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.presenter.MainPresenterImpl;
import com.monke.monkeybook.presenter.contract.MainContract;
import com.monke.monkeybook.service.AudioBookPlayService;
import com.monke.monkeybook.service.WebService;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.view.fragment.AudioBookFragment;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.view.fragment.FindBookFragment;
import com.monke.monkeybook.view.fragment.FragmentTrigger;
import com.monke.monkeybook.view.fragment.MainBookListFragment;
import com.monke.monkeybook.view.fragment.dialog.AlertDialog;
import com.monke.monkeybook.view.fragment.dialog.InputDialog;
import com.monke.monkeybook.view.fragment.dialog.ProgressDialog;
import com.monke.monkeybook.widget.BookShelfSearchView;
import com.monke.monkeybook.widget.ScrimInsetsRelativeLayout;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import compat.Optional;

public class MainActivity extends MBaseActivity<MainContract.Presenter> implements MainContract.View {
    private static final int BACKUP_RESULT = 11;
    private static final int RESTORE_RESULT = 12;
    private static final int FILE_SELECT_RESULT = 13;

    @BindView(R.id.layout_container)
    ScrimInsetsRelativeLayout container;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view)
    NavigationView drawerLeft;
    @BindView(R.id.appBar)
    View appBar;
    @BindView(R.id.card_search_bar)
    View mSearchBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.bookshelf_search_view)
    BookShelfSearchView drawerRight;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindArray(R.array.tab_main)
    String[] titles;

    private Switch swNightTheme;
    private boolean viewIsList;
    private long exitTime = 0;

    private ProgressDialog progressDialog;

    private final OnPermissionsGrantedCallback grantedCallback = requestCode -> {
        switch (requestCode) {
            case BACKUP_RESULT:
                mPresenter.backupData();
                break;
            case RESTORE_RESULT:
                mPresenter.restoreData();
                break;
            case FILE_SELECT_RESULT:
                fileSelectResult();
                break;
        }
    };

    @Override
    protected MainContract.Presenter initInjector() {
        return new MainPresenterImpl();
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
        viewIsList = getPreferences().getBoolean("bookshelfIsList", true);
        getIntent().putExtra("isRecreate", false);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MainBookListFragment mainFragment = findFragment(MainBookListFragment.class);
        if (viewPager.getCurrentItem() == 0 && mainFragment != null && mainFragment.dispatchTouchEvent(ev)) {
            return true;
        }


        FindBookFragment findFragment = findFragment(FindBookFragment.class);

        if (!drawer.isDrawerOpen(GravityCompat.END) && viewPager.getCurrentItem() == 1 && findFragment != null && findFragment.dispatchTouchEvent(ev)) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        initDrawer();

        container.setOnInsetsCallback(insets -> {
            appBar.setPadding(0, insets.top, 0, 0);
            drawerLeft.getHeaderView(0).setPadding(0, insets.top, 0, 0);
            drawerRight.applyWindowInsets(insets);
        });

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            setCustomView(tab);
        }

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), titles));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabSelected(tab, false);
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                setTabSelected(tab, true);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                whenTabReselected(tab);
            }
        });
    }

    @Override
    protected void firstRequest() {
        requestPermissions(9999);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (swNightTheme != null) {
            swNightTheme.setChecked(isNightTheme());
        }
    }

    @Override
    protected void bindEvent() {
        drawerRight.setOnItemClickListener(getAdapterListener());
        drawerRight.setIQuery(query -> mPresenter.queryBooks(query));

        toolbar.setOnClickListener(v -> {
            //点击搜索
            Intent intent = new Intent(MainActivity.this, SearchBookActivity.class);
            startActivityByAnim(intent, mSearchBar, mSearchBar.getTransitionName());
        });

    }

    public void setCurrentItem(int item) {
        viewPager.setCurrentItem(item);
    }

    private void setCustomView(TabLayout.Tab tab) {
        if (tab == null) return;
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.item_home_tab, null);
        TextView text = view.findViewById(R.id.text);
        ImageView icon = view.findViewById(R.id.icon);
        text.setText(tab.getText());
        text.setTag(text.getCurrentTextColor());
        icon.setImageDrawable(tab.getIcon());
        icon.setTag(tab.getIcon());
        tab.setCustomView(view);
        if (tab.getPosition() == 0) {
            text.setTextColor(ContextCompat.getColor(this, R.color.colorBarText));
        }

        if (tab.getCustomView() != null) {
            View tabView = (View) tab.getCustomView().getParent();
            tabView.setOnLongClickListener(v -> whenTabLongClick(tab));
        }
    }

    private void setTabSelected(TabLayout.Tab tab, boolean selected) {
        View custom = tab.getCustomView();
        if (custom != null) {
            TextView text = custom.findViewById(R.id.text);
            text.setTextColor(selected ? ContextCompat.getColor(this, R.color.colorBarText) : (Integer) text.getTag());
        }
    }

    private boolean whenTabLongClick(TabLayout.Tab tab) {
        if (tab.isSelected()) {
            FragmentTrigger fragmentTrigger = null;
            switch (tab.getPosition()) {
                case 0:
                    fragmentTrigger = findFragment(MainBookListFragment.class);
                    break;
                case 1:
                    fragmentTrigger = findFragment(FindBookFragment.class);
                    break;
                case 2:
                    fragmentTrigger = findFragment(AudioBookFragment.class);
            }

            if (fragmentTrigger != null) {
                fragmentTrigger.onRefresh();
            }

            Optional.ofNullable(tab.getCustomView()).ifPresent(view -> {
                ImageView imageView = view.findViewById(R.id.icon);
                startRefreshAnim(imageView);
            });
            return true;
        }
        return false;
    }

    private void whenTabReselected(TabLayout.Tab tab) {
        FragmentTrigger fragmentTrigger = null;
        switch (tab.getPosition()) {
            case 0:
                fragmentTrigger = findFragment(MainBookListFragment.class);
                break;
            case 1:
                fragmentTrigger = findFragment(FindBookFragment.class);
                break;
            case 2:
                fragmentTrigger = findFragment(AudioBookFragment.class);
        }

        if (fragmentTrigger != null) {
            fragmentTrigger.onReselected();
        }
    }

    public OnBookItemClickListenerTwo getAdapterListener() {
        return new OnBookItemClickListenerTwo() {
            @Override
            public void onClick(View view, BookShelfBean bookShelf) {
                KeyboardUtil.hideKeyboard(drawerRight.getCurrentFocus(false));
                if (mPresenter.checkLocalBookNotExists(bookShelf)) {
                    new AlertDialog.Builder(getSupportFragmentManager())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.delete_bookshelf_not_exist_s)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.removeFromBookShelf(bookShelf))
                            .show();
                } else {
                    ReadBookActivity.startThis(MainActivity.this, bookShelf);
                }
            }

            @Override
            public void onLongClick(View view, BookShelfBean bookShelf) {
                KeyboardUtil.hideKeyboard(drawerRight.getCurrentFocus(false));
                if (mPresenter.checkLocalBookNotExists(bookShelf)) {
                    new AlertDialog.Builder(getSupportFragmentManager())
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.delete_bookshelf_not_exist_s)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.removeFromBookShelf(bookShelf))
                            .show();
                } else {
                    BookDetailActivity.startThis(MainActivity.this, bookShelf);
                }
            }

        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenu = menu.findItem(R.id.action_list_grid);
        if (viewIsList) {
            pauseMenu.setIcon(R.drawable.ic_view_grid_black_24dp);
            pauseMenu.setTitle(R.string.action_grid);
        } else {
            pauseMenu.setIcon(R.drawable.ic_view_list_black_24dp);
            pauseMenu.setTitle(R.string.action_list);
        }
        AppCompat.setTint(pauseMenu, getResources().getColor(R.color.colorMenuText));
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
        SharedPreferences.Editor editor = getPreferences().edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_local:
                requestPermissions(FILE_SELECT_RESULT);
                break;
            case R.id.action_add_url:
                InputDialog.show(getSupportFragmentManager(), "添加书籍网址", null, null, inputText -> mPresenter.addBookUrl(inputText));
                break;
            case R.id.action_list_grid:
                viewIsList = !viewIsList;
                editor.putBoolean("bookshelfIsList", viewIsList);
                if (editor.commit()) {
                    Optional.ofNullable(findFragment(MainBookListFragment.class)).ifPresent(mainBookListFragment -> mainBookListFragment.upLayoutType(viewIsList));
                }
                break;
            case R.id.action_clearCaches:
                new AlertDialog.Builder(getSupportFragmentManager())
                        .setTitle(R.string.dialog_title)
                        .setMessage(R.string.clean_caches_s)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.cleanCaches())
                        .show();
                break;
            case R.id.action_clearBookshelf:
                new AlertDialog.Builder(getSupportFragmentManager())
                        .setTitle(R.string.dialog_title)
                        .setMessage(R.string.clear_bookshelf_s)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, (dialog, which) -> mPresenter.clearBookshelf())
                        .show();
                break;
            case R.id.action_web_start:
                WebService.startThis(this);
                break;
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawers();
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
//        AppCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.colorMenuText));
    }

    //初始化侧边栏
    private void initDrawer() {
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView == drawerRight) {
                    if (slideOffset < 0.8f) {
                        KeyboardUtil.hideKeyboard(drawerRight.getCurrentFocus(false));
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView == drawerRight) {
                    KeyboardUtil.showKeyboard(drawerRight.getCurrentFocus(true));
                }
            }
        });

        setUpNavigationView();
    }

    //侧边栏按钮
    private void setUpNavigationView() {
        AppCompat.useCustomNavigationViewDivider(drawerLeft);
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
                case R.id.action_download:
                    drawerLeft.postDelayed(() -> DownloadActivity.startThis(this), 220L);
                    break;
                case R.id.action_book_source_manage:
                    drawerLeft.postDelayed(() -> BookSourceActivity.startThis(this), 220L);
                    break;
                case R.id.action_replace_rule:
                    drawerLeft.postDelayed(() -> ReplaceRuleActivity.startThis(this), 220L);
                    break;
                case R.id.action_setting:
                    drawerLeft.postDelayed(() -> SettingActivity.startThis(this), 220L);
                    break;
                case R.id.action_about:
                    drawerLeft.postDelayed(() -> AboutActivity.startThis(this), 220L);
                    break;
                case R.id.action_cache_manager:
                    drawerLeft.postDelayed(() -> CacheManagerActivity.startThis(this), 220L);
                    break;
                case R.id.action_backup:
                    drawerLeft.postDelayed(this::backup, 220L);
                    break;
                case R.id.action_restore:
                    drawerLeft.postDelayed(this::restore, 220L);
                    break;
                case R.id.action_night_theme:
                    getIntent().putExtra("isRecreate", true);
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
        new AlertDialog.Builder(getSupportFragmentManager())
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.backup_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> requestPermissions(BACKUP_RESULT))
                .show();
    }

    //恢复
    private void restore() {
        new AlertDialog.Builder(getSupportFragmentManager())
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.restore_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> requestPermissions(RESTORE_RESULT))
                .show();
    }

    private void requestPermissions(int requestCode) {
        new PermissionsCompat.Builder(this)
                .requestCode(requestCode)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(grantedCallback)
                .request();
    }

    private void fileSelectResult() {
        FileSelectorDialog.newInstance("选择文件", false, true, false, new String[]{"txt"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onMultiplyChoice(List<String> paths) {
                mPresenter.importBooks(paths);
            }
        });
    }

    private void startRefreshAnim(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        Animation animation = imageView.getAnimation();
        if (animation != null && animation.hasStarted() && !animation.hasEnded()) {
            return;
        }
        imageView.setImageResource(R.drawable.ic_refresh_white_24dp);
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        animationSet.addAnimation(rotateAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setImageDrawable((Drawable) imageView.getTag());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animationSet);
    }

    @SuppressWarnings("unchecked")
    private <T extends Fragment> T findFragment(Class<T> clazz) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.getClass().equals(clazz)) {
                return (T) fragment;
            }
        }
        return null;
    }

    @Override
    public void clearBookshelf() {
        AudioBookPlayService.stop(this);
        drawerRight.clear();

        Optional.ofNullable(findFragment(MainBookListFragment.class)).ifPresent(MainBookListFragment::clearBookshelf);
    }

    @Override
    public void showQueryBooks(List<BookShelfBean> bookShelfBeans) {
        drawerRight.showQueryBooks(bookShelfBeans);
    }

    @Override
    public void updateBook(BookShelfBean bookShelfBean, boolean sort) {
        drawerRight.updateBookShelfIfNeed(bookShelfBean);
    }

    @Override
    public void addBookShelf(BookShelfBean bookShelfBean) {
        drawerRight.addBookShelfIfNeed(bookShelfBean);
    }

    @Override
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        drawerRight.removeBookShelfIfNeed(bookShelfBean);
    }

    @Override
    public void dismissHUD() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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
    public void addSuccess(BookShelfBean bookShelfBean) {
        Optional.ofNullable(findFragment(MainBookListFragment.class)).ifPresent(mainBookListFragment -> mainBookListFragment.addBookSuccess(bookShelfBean));
    }

    @Override
    public void restoreSuccess() {
        dismissHUD();
        initImmersionBar();

        Optional.ofNullable(findFragment(MainBookListFragment.class)).ifPresent(MainBookListFragment::onRestore);
        Optional.ofNullable(findFragment(FindBookFragment.class)).ifPresent(FindBookFragment::onRestore);
        Optional.ofNullable(findFragment(AudioBookFragment.class)).ifPresent(AudioBookFragment::onRestore);
    }

    @Override
    protected View getSnackBarView() {
        return toolbar;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MainBookListFragment mainFragment = findFragment(MainBookListFragment.class);
            if (viewPager.getCurrentItem() == 0 && mainFragment != null && mainFragment.onBackPressed()) {
                return true;
            }

            FindBookFragment findFragment = findFragment(FindBookFragment.class);
            if (viewPager.getCurrentItem() == 1 && findFragment != null && findFragment.onBackPressed()) {
                return true;
            }

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

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            toast("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }


    private static class PagerAdapter extends FragmentPagerAdapter {

        private final String[] titles;

        PagerAdapter(@NonNull FragmentManager fm, String[] titles) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.titles = titles;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MainBookListFragment();
                case 1:
                    return new FindBookFragment();
                case 2:
                default:
                    return new AudioBookFragment();
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }
}