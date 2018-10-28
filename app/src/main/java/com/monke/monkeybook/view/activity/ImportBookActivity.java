package com.monke.monkeybook.view.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.ImportBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ImportBookContract;
import com.monke.monkeybook.view.fragment.BaseFileFragment;
import com.monke.monkeybook.view.fragment.FileCategoryFragment;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by newbiechen on 17-5-27.
 */

public class ImportBookActivity extends MBaseActivity<ImportBookContract.Presenter> implements ImportBookContract.View {

    @BindView(R.id.file_system_cb_selected_all)
    CheckBox mCbSelectAll;
    @BindView(R.id.file_system_btn_delete)
    Button mBtnDelete;
    @BindView(R.id.file_system_btn_add_book)
    Button mBtnAddBook;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.file_category_tv_back_last)
    TextView mTvBackLast;
    @BindView(R.id.tv_sd)
    TextView tvSd;
    @BindView(R.id.file_category_tv_path)
    public TextView mTvPath;

    private MoDialogHUD moDialogHUD;

    private FileCategoryFragment mCategoryFragment;

    private BaseFileFragment.OnFileCheckedListener mListener = new BaseFileFragment.OnFileCheckedListener() {
        @Override
        public void onItemCheckedChange(boolean isChecked) {
            changeMenuStatus();
        }

        @Override
        public void onCategoryChanged() {
            //状态归零
            mCategoryFragment.setCheckedAll(false);
            //改变菜单
            changeMenuStatus();
            //改变是否能够全选
            changeCheckedAllStatus();
        }
    };


    @Override
    protected ImportBookContract.Presenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCategoryFragment = (FileCategoryFragment) getSupportFragmentManager().findFragmentByTag(FileCategoryFragment.class.getSimpleName());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_import_book);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {
        if (mCategoryFragment == null) {
            mCategoryFragment = new FileCategoryFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mCategoryFragment, FileCategoryFragment.class.getSimpleName())
                .commitNow();
    }

    @Override
    protected void bindView() {
        mTvBackLast.getCompoundDrawables()[0].mutate();
        mTvBackLast.getCompoundDrawables()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void bindEvent() {
        mTvBackLast.setOnClickListener(v -> mCategoryFragment.backLastCategory());

        tvSd.setOnClickListener(v -> mCategoryFragment.showSdSelector());

        mCbSelectAll.setOnClickListener(v -> {
                    //设置全选状态
                    boolean isChecked = mCbSelectAll.isChecked();
                    mCategoryFragment.setCheckedAll(isChecked);
                    //改变菜单状态
                    changeMenuStatus();
                }
        );

        mBtnAddBook.setOnClickListener(v -> {
                    //获取选中的文件
                    List<File> files = mCategoryFragment.getCheckedFiles();
                    //转换成CollBook,并存储
                    mPresenter.importBooks(files);
                }
        );

        mBtnDelete.setOnClickListener(v -> {
                    //弹出，确定删除文件吗。
                    new AlertDialog.Builder(this)
                            .setTitle("删除文件")
                            .setMessage("确定删除文件吗?")
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                //删除选中的文件
                                mCategoryFragment.deleteCheckedFiles();
                                //提示删除文件成功
                                Toast.makeText(ImportBookActivity.this, "删除文件成功", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
        );

        mCategoryFragment.setOnFileCheckedListener(mListener);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_local);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单状态
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mCategoryFragment.backLastCategory()){
            return;
        }
        super.onBackPressed();
    }

    /**
     * 改变底部选择栏的状态
     */
    private void changeMenuStatus() {

        //点击、删除状态的设置
        if (mCategoryFragment.getCheckedCount() == 0) {
            mBtnAddBook.setText(getString(R.string.nb_file_add_shelf));
            //设置某些按钮的是否可点击
            setMenuClickable(false);

            if (mCbSelectAll.isChecked()) {
                mCategoryFragment.setChecked(false);
                mCbSelectAll.setChecked(mCategoryFragment.isCheckedAll());
            }

        } else {
            mBtnAddBook.setText(getString(R.string.nb_file_add_shelves, mCategoryFragment.getCheckedCount()));
            setMenuClickable(true);

            //全选状态的设置

            //如果选中的全部的数据，则判断为全选
            if (mCategoryFragment.getCheckedCount() == mCategoryFragment.getCheckableCount()) {
                //设置为全选
                mCategoryFragment.setChecked(true);
                mCbSelectAll.setChecked(mCategoryFragment.isCheckedAll());
            }
            //如果曾今是全选则替换
            else if (mCategoryFragment.isCheckedAll()) {
                mCategoryFragment.setChecked(false);
                mCbSelectAll.setChecked(mCategoryFragment.isCheckedAll());
            }
        }

        //重置全选的文字
        if (mCategoryFragment.isCheckedAll()) {
            mCbSelectAll.setText("取消");
        } else {
            mCbSelectAll.setText("全选");
        }

    }

    private void setMenuClickable(boolean isClickable) {

        //设置是否可删除
        mBtnDelete.setEnabled(isClickable);
        mBtnDelete.setClickable(isClickable);

        //设置是否可添加书籍
        mBtnAddBook.setEnabled(isClickable);
        mBtnAddBook.setClickable(isClickable);
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        //获取可选择的文件数量
        int count = mCategoryFragment.getCheckableCount();

        //设置是否能够全选
        if (count > 0) {
            mCbSelectAll.setClickable(true);
            mCbSelectAll.setEnabled(true);
        } else {
            mCbSelectAll.setClickable(false);
            mCbSelectAll.setEnabled(false);
        }
    }

    @Override
    public void showLoading(String msg) {
        if(moDialogHUD == null){
            moDialogHUD = new MoDialogHUD(this);
        }
        moDialogHUD.showLoading(msg);
    }

    @Override
    public void dismissHUD() {
        if(moDialogHUD != null){
            moDialogHUD.dismiss();
        }
    }

    @Override
    public void addSuccess() {
        //设置HashMap为false
        mCategoryFragment.setCheckedAll(false);
        //改变菜单状态
        changeMenuStatus();
        //改变是否可以全选
        changeCheckedAllStatus();
        dismissHUD();
    }

    @Override
    public void addError(String msg) {
        dismissHUD();
        Snackbar.make(toolbar, msg, Snackbar.LENGTH_SHORT).show();
    }
}
