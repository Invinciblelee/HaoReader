package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.widget.modialog.MoDialogHUD;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.monke.monkeybook.utils.NetworkUtil.isNetworkAvailable;

public class BookInfoActivity extends MBaseActivity {
    private final int ResultSelectCover = 103;

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

    private String noteUrl;
    private BookShelfBean book;
    private MoDialogHUD moDialogHUD;


    public static void startThis(MBaseActivity context, String noteUrl, View transitionView) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra("noteUrl", noteUrl);
        context.startActivityByAnim(intent, transitionView, transitionView.getTransitionName());
    }

    public static void startThis(MBaseActivity context, String noteUrl) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra("noteUrl", noteUrl);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString("noteUrl"))) {
            noteUrl = savedInstanceState.getString("noteUrl");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteUrl", noteUrl);
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        tilBookName.setHint("书名");
        tilBookAuthor.setHint("作者");
        tilCoverUrl.setHint("封面地址");
        tilBookJj.setHint("简介");
        moDialogHUD = new MoDialogHUD(this);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        noteUrl = getIntent().getStringExtra("noteUrl");

        if (!TextUtils.isEmpty(noteUrl)) {
            book = BookshelfHelp.getBookByUrl(noteUrl, false);
            if (book != null) {
                tieBookName.setText(book.getBookInfoBean().getName());
                tieBookAuthor.setText(book.getBookInfoBean().getAuthor());
                tieBookJj.setText(book.getBookInfoBean().getIntroduce());
                if (TextUtils.isEmpty(book.getCustomCoverPath())) {
                    tieCoverUrl.setText(book.getBookInfoBean().getCoverUrl());
                } else {
                    tieCoverUrl.setText(book.getCustomCoverPath());
                }
            }
            initCover(getTextString(tieCoverUrl));
        }
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        super.bindEvent();
        tvSelectLocalCover.setOnClickListener(view -> {
            if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, ResultSelectCover);
            } else {
                EasyPermissions.requestPermissions(this, "获取背景图片需存储权限", MApplication.RESULT__PERMS, MApplication.PerList);
            }
        });
        tvChangeCover.setOnClickListener(view -> moDialogHUD.showChangeSource(this, book, searchBookBean -> {
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
    private void setupActionBar() {
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
        book.getBookInfoBean().setName(getTextString(tieBookName));
        book.getBookInfoBean().setAuthor(getTextString(tieBookAuthor));
        book.getBookInfoBean().setIntroduce(getTextString(tieBookJj));
        book.setCustomCoverPath(getTextString(tieCoverUrl));
        BookshelfHelp.saveBookToShelf(book);
        RxBus.get().post(RxBusTag.UPDATE_BOOK_INFO, book);
        finishByTransition();
    }

    @AfterPermissionGranted(MApplication.RESULT__PERMS)
    private void imageSelectorResult() {
        tvSelectLocalCover.callOnClick();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectCover:
                if (resultCode == RESULT_OK && null != data) {
                    tieCoverUrl.setText(FileUtil.getPath(this, data.getData()));
                    initCover(getTextString(tieCoverUrl));
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return moDialogHUD.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private void finishByTransition() {
        KeyboardUtil.hideKeyboard(this);
        super.supportFinishAfterTransition();
    }

}
