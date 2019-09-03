package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.view.fragment.dialog.FileSelectorDialog;
import com.monke.monkeybook.view.fragment.dialog.ChangeSourceDialog;
import com.monke.monkeybook.widget.theme.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookInfoActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tie_book_name)
    TextInputEditText tieBookName;
    @BindView(R.id.til_book_name)
    TextInputLayout tilBookName;
    @BindView(R.id.tie_book_author)
    TextInputEditText tieBookAuthor;
    @BindView(R.id.til_book_author)
    TextInputLayout tilBookAuthor;
    @BindView(R.id.tie_cover_url)
    TextInputEditText tieCoverUrl;
    @BindView(R.id.til_cover_url)
    TextInputLayout tilCoverUrl;
    @BindView(R.id.tv_select_local_cover)
    TextView tvSelectLocalCover;
    @BindView(R.id.tv_change_cover)
    TextView tvChangeCover;
    @BindView(R.id.tv_refresh_cover)
    TextView tvRefreshCover;
    @BindView(R.id.tie_book_jj)
    TextInputEditText tieBookJj;
    @BindView(R.id.til_book_jj)
    TextInputLayout tilBookJj;

    private BookShelfBean bookShelf;
    private BookInfoBean bookInfo;

    public static void startThis(MBaseActivity context, BookShelfBean bookShelf, View transitionView) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        context.startActivityByAnim(intent, transitionView, transitionView.getTransitionName());
    }

    public static void startThis(MBaseActivity context, BookShelfBean bookShelf) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        context.startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bookShelf != null) {
            String key = String.valueOf(System.currentTimeMillis());
            getIntent().putExtra("data_key", key);
            BitIntentDataManager.getInstance().putData(key, bookShelf);
        }
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);
        tilBookName.setHint("书名");
        tilBookAuthor.setHint("作者");
        tilCoverUrl.setHint("封面地址");
        tilBookJj.setHint("简介");
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        String key = getIntent().getStringExtra("data_key");
        bookShelf = BitIntentDataManager.getInstance().getData(key, null);
        bookInfo = bookShelf.getBookInfoBean();
        BitIntentDataManager.getInstance().cleanData(key);

        if (bookInfo != null) {
            tieBookName.setText(bookInfo.getName());
            tieBookAuthor.setText(bookInfo.getAuthor());
            tieBookJj.setText(bookInfo.getIntroduce());
            tieCoverUrl.setText(bookInfo.getRealCoverUrl());
        }
        initCover(getTextString(tieCoverUrl));
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        super.bindEvent();
        tvSelectLocalCover.setOnClickListener(view -> {
            new PermissionsCompat.Builder(BookInfoActivity.this)
                    .addPermissions(Permissions.Group.STORAGE)
                    .rationale("存储")
                    .onGranted(requestCode -> imageSelectorResult()).build().request();
        });
        tvChangeCover.setOnClickListener(view -> ChangeSourceDialog.show(getSupportFragmentManager(), bookInfo, true, searchBookBean -> {
            tieCoverUrl.setText(searchBookBean.getCoverUrl());
            initCover(getTextString(tieCoverUrl));
        }));
        tvRefreshCover.setOnClickListener(view -> initCover(getTextString(tieCoverUrl)));
    }

    private String getTextString(TextInputEditText editText) {
        return editText.getText() == null ? null : editText.getText().toString();
    }

    private void initCover(String url) {
        if (!this.isFinishing()) {
            Glide.with(this).load(url)
                    .apply(new RequestOptions().dontAnimate().centerCrop()
                            .placeholder(R.drawable.img_cover_default)).into(ivCover);
        }
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_info);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveInfo();
                break;
            case android.R.id.home:
                finishByTransition();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveInfo() {
        bookInfo.setName(getTextString(tieBookName));
        bookInfo.setAuthor(getTextString(tieBookAuthor));
        bookInfo.setIntroduce(getTextString(tieBookJj));
        bookInfo.setCustomCoverPath(getTextString(tieCoverUrl));
        bookShelf.setBookInfoBean(bookInfo);
        DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().insertOrReplace(bookInfo);
        RxBus.get().post(RxBusTag.UPDATE_BOOK_INFO, bookShelf);
        finishByTransition();
    }

    private void imageSelectorResult() {
        FileSelectorDialog.newInstance("选择图片", true, false, true, new String[]{"png", "jpg", "jpeg"}).show(this, new FileSelectorDialog.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                tieCoverUrl.setText(path);
                initCover(getTextString(tieCoverUrl));
            }
        });
    }

    private void finishByTransition() {
        KeyboardUtil.hideKeyboard(this);
        super.supportFinishAfterTransition();
    }

}
