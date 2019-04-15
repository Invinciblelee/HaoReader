package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.ReplaceRuleManager;
import com.monke.monkeybook.presenter.ReplaceRulePresenterImpl;
import com.monke.monkeybook.presenter.contract.ReplaceRuleContract;
import com.monke.monkeybook.view.adapter.ReplaceRuleAdapter;
import com.monke.monkeybook.view.fragment.FileSelector;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.monke.monkeybook.view.activity.BookSourceActivity.RESULT_IMPORT_PERMS;

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

    private MoDialogHUD moDialogHUD;
    private ReplaceRuleAdapter adapter;
    private boolean selectAll = true;

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
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void initData() {

    }

    private void initRecyclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplaceRuleAdapter(this);
        recyclerViewBookSource.setAdapter(adapter);
        adapter.resetDataS(ReplaceRuleManager.getInstance().getAll());
        MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
        itemTouchHelpCallback.setOnItemTouchCallbackListener(adapter.getItemTouchCallbackListener());
        itemTouchHelpCallback.setDragEnable(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookSource);

    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        moDialogHUD.showPutReplaceRule(replaceRuleBean, ruleBean -> {
            Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
                ReplaceRuleManager.getInstance().saveData(ruleBean);
                e.onNext(ReplaceRuleManager.getInstance().getAll());
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                        @Override
                        public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                            adapter.resetDataS(replaceRuleBeans);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        });
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
        ReplaceRuleManager.getInstance().saveDataS(adapter.getDataList());
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        mPresenter.delData(replaceRuleBean);
    }

    public void saveDataS() {
        mPresenter.saveData(adapter.getDataList());
    }

    //设置ToolBar
    private void setupActionBar() {
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
                moDialogHUD.showInputBox("输入替换规则网址", cacheUrl, null,
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
        if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
            resultImportPerms();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.import_book_source),
                    RESULT_IMPORT_PERMS, MApplication.PerList);
        }
    }

    @AfterPermissionGranted(RESULT_IMPORT_PERMS)
    private void resultImportPerms() {
        FileSelector.newInstance("选择文件",true, false, false, new String[]{"txt", "json", "xml"}).show(this, new FileSelector.OnFileSelectedListener() {
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
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void refresh() {
        adapter.resetDataS(ReplaceRuleManager.getInstance().getAll());
    }

    @Override
    protected void onDestroy() {
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        super.onDestroy();
    }
}
