package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.model.ReplaceRuleManager;
import com.monke.monkeybook.presenter.ReplaceRulePresenterImpl;
import com.monke.monkeybook.presenter.contract.ReplaceRuleContract;
import com.monke.monkeybook.view.adapter.ReplaceRuleAdapter;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.view.fragment.dialog.InputDialog;
import com.monke.monkeybook.view.fragment.dialog.ProgressDialog;
import com.monke.monkeybook.view.fragment.dialog.ReplaceRuleDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class ReplaceRuleActivity extends MBaseActivity<ReplaceRuleContract.Presenter> implements ReplaceRuleContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rl_content)
    RelativeLayout rlContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerViewBookSource;

    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

    private ProgressDialog progressDialog;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, ReplaceRuleActivity.class));
    }

    @Override
    protected ReplaceRuleContract.Presenter initInjector() {
        return new ReplaceRulePresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplaceRuleAdapter(this);
        recyclerViewBookSource.setAdapter(adapter);
        adapter.resetDataS(ReplaceRuleManager.getAll());
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchHelpCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookSource);

    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        ReplaceRuleDialog.show(getSupportFragmentManager(), replaceRuleBean, ruleBean ->
                mPresenter.editData(ruleBean));
    }

    public void upDateSelectAll() {
        selectAll = true;
        for (ReplaceRuleBean replaceRuleBean : adapter.getDataList()) {
            if (replaceRuleBean.getEnable() == null || !replaceRuleBean.getEnable()) {
                selectAll = false;
                break;
            }
        }
    }

    private void selectAllDataS() {
        for (ReplaceRuleBean replaceRuleBean : adapter.getDataList()) {
            replaceRuleBean.setEnable(!selectAll);
        }
        adapter.notifyDataSetChanged();
        selectAll = !selectAll;
        ReplaceRuleManager.saveAll(adapter.getDataList());
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        mPresenter.delData(replaceRuleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getDataList());
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.replace_rule_title);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_replace_rule_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_replace_rule:
                editReplaceRule(null);
                break;
            case R.id.action_select_all:
                selectAllDataS();
                break;
            case R.id.action_import:
                selectReplaceRuleFile();
                break;
            case R.id.action_import_onLine:
                String cacheUrl = ACache.get(this).getAsString("replaceUrl");
                InputDialog.show(getSupportFragmentManager(), "输入替换规则网址", cacheUrl, null,
                        inputText -> {
                            ACache.get(this).put("replaceUrl", inputText);
                            mPresenter.importDataS(inputText);
                        });
                break;
            case R.id.action_del_all:
                mPresenter.delData(adapter.getDataList());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectReplaceRuleFile() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("存储")
                .onGranted(requestCode -> resultImportPerms())
                .request();
    }

    private void resultImportPerms() {
        FileSelectorDialog.newInstance("选择文件", true, false, false, new String[]{"txt", "json", "xml"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                mPresenter.importDataS(new File(path));
            }
        });
    }


    @Override
    protected View getSnackBarView() {
        return toolbar;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void refresh(List<ReplaceRuleBean> replaceRuleBeans) {
        adapter.resetDataS(replaceRuleBeans);
    }

    @Override
    public void dismissLoading() {
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
    protected void onDestroy() {
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        super.onDestroy();
    }
}
