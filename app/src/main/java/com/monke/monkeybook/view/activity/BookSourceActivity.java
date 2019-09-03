package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.presenter.BookSourcePresenterImpl;
import com.monke.monkeybook.presenter.contract.BookSourceContract;
import com.monke.monkeybook.view.adapter.BookSourceAdapter;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.view.fragment.dialog.InputDialog;
import com.monke.monkeybook.view.fragment.dialog.ProgressDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class BookSourceActivity extends MBaseActivity<BookSourceContract.Presenter> implements BookSourceContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rl_content)
    RelativeLayout rlContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.searchView)
    SearchView searchView;

    private MyItemTouchHelpCallback itemTouchHelpCallback;
    private boolean selectAll = true;
    private MenuItem groupItem;
    private SubMenu groupMenu;
    private SubMenu sortMenu;
    private BookSourceAdapter adapter;
    private ProgressDialog progressDialog;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean isSearch;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, BookSourceActivity.class));
    }

    @Override
    protected BookSourceContract.Presenter initInjector() {
        return new BookSourcePresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_source);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        initSearchView();
        initRecyclerView();
        mPresenter.initData();
    }

    private void initSearchView() {
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.search_book_source));
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSearch = !TextUtils.isEmpty(newText);
                mPresenter.refresh();
                return false;
            }
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookSourceAdapter(this);
        recyclerView.setAdapter(adapter);
        itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchHelpCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setDragEnable(getPreferences().getInt("SourceSort", 0));
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            if (!bookSourceBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (BookSourceBean bookSourceBean : adapter.getDataList()) {
            bookSourceBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        recyclerView.post(() -> saveDate(adapter.getDataList()));
    }

    private void setDragEnable(int sort) {
        if (itemTouchHelpCallback == null) {
            return;
        }
        if (sort == 0) {
            itemTouchHelpCallback.setDragEnable(true);
        } else {
            itemTouchHelpCallback.setDragEnable(false);
        }
        if (sort == 2) {
            adapter.setCanTop(false);
        } else {
            adapter.setCanTop(true);
        }
    }

    public void upSearchView(int size) {
        AppCompat.setQueryHintForSearchText(mSearchAutoComplete, getString(R.string.search_book_source_num, size));
    }

    @Override
    public void resetData(List<BookSourceBean> bookSourceBeans) {
        if (recyclerView == null || adapter == null) return;
        adapter.resetDataS(bookSourceBeans);
        upGroupMenu();
    }


    @Override
    public void upGroupMenu(List<String> groupList) {
        if (groupMenu == null) return;
        groupMenu.removeGroup(R.id.source_group);
        if (groupList.size() == 0) {
            groupItem.setVisible(false);
        } else {
            groupItem.setVisible(true);
            for (String groupName : groupList) {
                groupMenu.add(R.id.source_group, Menu.NONE, Menu.NONE, groupName);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        RxBus.get().post(RxBusTag.SOURCE_LIST_CHANGE, true);
    }

    @Override
    public String getQuery() {
        return searchView.getQuery().toString();
    }

    public void delBookSource(BookSourceBean bookSource) {
        mPresenter.delData(bookSource);
    }

    public void saveDate(BookSourceBean date) {
        mPresenter.saveData(date);
    }

    public void saveDate(List<BookSourceBean> date) {
        mPresenter.saveData(date);
        supportInvalidateOptionsMenu();
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

    @Override
    protected View getSnackBarView() {
        return rlContent;
    }

    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.book_source_manage);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_source_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        groupItem = menu.findItem(R.id.action_group);
        groupMenu = groupItem.getSubMenu();
        sortMenu = menu.findItem(R.id.action_sort).getSubMenu();
        upGroupMenu();
        upSortMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_book_source:
                addBookSource();
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import_book_source_local:
                selectBookSourceFile();
                break;
            case R.id.action_import_book_source_onLine:
                importBookSourceOnline();
                break;
            case R.id.action_del_select:
                mPresenter.delData(adapter.getSelectDataList());
                break;
            case R.id.action_check_book_source:
                mPresenter.checkBookSource();
                break;
            case R.id.sort_manual:
                upSourceSort(0);
                break;
            case R.id.sort_auto:
                upSourceSort(1);
                break;
            case R.id.sort_ping_yin:
                upSourceSort(2);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        if (item.getGroupId() == R.id.source_group) {
            searchView.setQuery(item.getTitle(), true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void upGroupMenu() {
        mPresenter.refreshGroup();
    }

    private void upSortMenu() {
        sortMenu.getItem(getPreferences().getInt("SourceSort", 0)).setChecked(true);
    }

    private void upSourceSort(int sort) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt("SourceSort", sort);
        editor.apply();
        upSortMenu();
        setDragEnable(sort);
        mPresenter.refresh();
    }

    private void addBookSource() {
        Intent intent = new Intent(this, SourceEditActivity.class);
        startActivityForResult(intent, SourceEditActivity.REQUEST_EDIT_SOURCE);
    }

    private void selectBookSourceFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(requestCode -> resultImportPerms())
                .request();
    }

    private void importBookSourceOnline() {
        String cacheUrl = ACache.get(this).getAsString("sourceUrl");
        InputDialog.show(getSupportFragmentManager(), "输入书源网址", cacheUrl, null,
                inputText -> {
                    ACache.get(this).put("sourceUrl", inputText);
                    mPresenter.importBookSource(inputText);
                });
    }

    private void resultImportPerms() {
        FileSelectorDialog.newInstance("选择文件", true, false, false, new String[]{"txt", "json", "xml"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                mPresenter.importBookSource(new File(path));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SourceEditActivity.REQUEST_EDIT_SOURCE && resultCode == RESULT_OK) {
            mPresenter.refresh();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearch) {
                try {
                    //如果搜索框中有文字，则会先清空文字.
                    mSearchAutoComplete.setText("");
                } catch (Exception ignore) {
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
