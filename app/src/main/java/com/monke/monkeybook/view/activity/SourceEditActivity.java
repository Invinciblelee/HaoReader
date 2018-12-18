package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.presenter.SourceEditPresenterImpl;
import com.monke.monkeybook.presenter.contract.SourceEditContract;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.view.fragment.FileSelector;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View {
    public final static int EDIT_SOURCE = 1101;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rl_content)
    RelativeLayout rlContent;
    @BindView(R.id.tie_book_source_type)
    TextInputEditText tieBookSourceType;
    @BindView(R.id.til_book_source_type)
    TextInputLayout tilBookSourceType;
    @BindView(R.id.tie_book_source_rule_type)
    TextInputEditText tieBookSourceRuleType;
    @BindView(R.id.til_book_source_rule_type)
    TextInputLayout tilBookSourceRuleType;
    @BindView(R.id.tie_book_source_url)
    TextInputEditText tieBookSourceUrl;
    @BindView(R.id.til_book_source_url)
    TextInputLayout tilBookSourceUrl;
    @BindView(R.id.tie_book_source_name)
    TextInputEditText tieBookSourceName;
    @BindView(R.id.til_book_source_name)
    TextInputLayout tilBookSourceName;
    @BindView(R.id.tie_ruleSearchUrl)
    TextInputEditText tieRuleSearchUrl;
    @BindView(R.id.til_ruleSearchUrl)
    TextInputLayout tilRuleSearchUrl;
    @BindView(R.id.tie_ruleSearchList)
    TextInputEditText tieRuleSearchList;
    @BindView(R.id.til_ruleSearchList)
    TextInputLayout tilRuleSearchList;
    @BindView(R.id.tie_ruleSearchName)
    TextInputEditText tieRuleSearchName;
    @BindView(R.id.til_ruleSearchName)
    TextInputLayout tilRuleSearchName;
    @BindView(R.id.tie_ruleSearchAuthor)
    TextInputEditText tieRuleSearchAuthor;
    @BindView(R.id.til_ruleSearchAuthor)
    TextInputLayout tilRuleSearchAuthor;
    @BindView(R.id.tie_ruleSearchKind)
    TextInputEditText tieRuleSearchKind;
    @BindView(R.id.til_ruleSearchKind)
    TextInputLayout tilRuleSearchKind;
    @BindView(R.id.tie_ruleSearchLastChapter)
    TextInputEditText tieRuleSearchLastChapter;
    @BindView(R.id.til_ruleSearchLastChapter)
    TextInputLayout tilRuleSearchLastChapter;
    @BindView(R.id.tie_ruleSearchCoverUrl)
    TextInputEditText tieRuleSearchCoverUrl;
    @BindView(R.id.til_ruleSearchCoverUrl)
    TextInputLayout tilRuleSearchCoverUrl;
    @BindView(R.id.tie_ruleSearchNoteUrl)
    TextInputEditText tieRuleSearchNoteUrl;
    @BindView(R.id.til_ruleSearchNoteUrl)
    TextInputLayout tilRuleSearchNoteUrl;
    @BindView(R.id.tie_ruleBookName)
    TextInputEditText tieRuleBookName;
    @BindView(R.id.til_ruleBookName)
    TextInputLayout tilRuleBookName;
    @BindView(R.id.tie_ruleBookAuthor)
    TextInputEditText tieRuleBookAuthor;
    @BindView(R.id.til_ruleBookAuthor)
    TextInputLayout tilRuleBookAuthor;
    @BindView(R.id.til_ruleLastChapter)
    TextInputLayout tilRuleLastChapter;
    @BindView(R.id.tie_ruleLastChapter)
    TextInputEditText tieRuleLastChapter;
    @BindView(R.id.tie_ruleCoverUrl)
    TextInputEditText tieRuleCoverUrl;
    @BindView(R.id.til_ruleCoverUrl)
    TextInputLayout tilRuleCoverUrl;
    @BindView(R.id.tie_ruleChapterUrl)
    TextInputEditText tieRuleChapterUrl;
    @BindView(R.id.til_ruleChapterUrl)
    TextInputLayout tilRuleChapterUrl;
    @BindView(R.id.tie_ruleIntroduce)
    TextInputEditText tieRuleIntroduce;
    @BindView(R.id.til_ruleIntroduce)
    TextInputLayout tilRuleIntroduce;
    @BindView(R.id.tie_ruleChapterList)
    TextInputEditText tieRuleChapterList;
    @BindView(R.id.til_ruleChapterList)
    TextInputLayout tilRuleChapterList;
    @BindView(R.id.tie_ruleChapterName)
    TextInputEditText tieRuleChapterName;
    @BindView(R.id.til_ruleChapterName)
    TextInputLayout tilRuleChapterName;
    @BindView(R.id.tie_ruleContentUrl)
    TextInputEditText tieRuleContentUrl;
    @BindView(R.id.til_ruleContentUrl)
    TextInputLayout tilRuleContentUrl;
    @BindView(R.id.tie_ruleBookContent)
    TextInputEditText tieRuleBookContent;
    @BindView(R.id.til_ruleBookContent)
    TextInputLayout tilRuleBookContent;
    @BindView(R.id.tie_httpUserAgent)
    TextInputEditText tieHttpUserAgent;
    @BindView(R.id.til_httpUserAgent)
    TextInputLayout tilHttpUserAgent;
    @BindView(R.id.tie_ruleFindUrl)
    TextInputEditText tieRuleFindUrl;
    @BindView(R.id.til_ruleFindUrl)
    TextInputLayout tilRuleFindUrl;
    @BindView(R.id.tie_bookSourceGroup)
    TextInputEditText tieBookSourceGroup;
    @BindView(R.id.til_bookSourceGroup)
    TextInputLayout tilBookSourceGroup;
    @BindView(R.id.tie_checkUrl)
    TextInputEditText tieCheckUrl;
    @BindView(R.id.til_checkUrl)
    TextInputLayout tilCheckUrl;
    @BindView(R.id.til_loginUrl)
    TextInputLayout tilLoginUrl;
    @BindView(R.id.tie_loginUrl)
    TextInputEditText tieLoginUrl;
    @BindView(R.id.tie_ruleChapterUrlNext)
    TextInputEditText tieRuleChapterUrlNext;
    @BindView(R.id.til_ruleChapterUrlNext)
    TextInputLayout tilRuleChapterUrlNext;
    @BindView(R.id.tie_ruleContentUrlNext)
    TextInputEditText tieRuleContentUrlNext;
    @BindView(R.id.til_ruleContentUrlNext)
    TextInputLayout tilRuleContentUrlNext;

    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private boolean enable;
    private String title;

    public static void startThis(Activity activity, BookSourceBean sourceBean) {
        Intent intent = new Intent(activity, SourceEditActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        activity.startActivityForResult(intent, EDIT_SOURCE);
    }

    @Override
    protected SourceEditContract.Presenter initInjector() {
        return new SourceEditPresenterImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            serialNumber = savedInstanceState.getInt("serialNumber");
            enable = savedInstanceState.getBoolean("enable");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putInt("serialNumber", serialNumber);
        outState.putBoolean("enable", enable);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_edit);

    }

    @Override
    protected void initData() {
        String key = this.getIntent().getStringExtra("data_key");
        if (title == null) {
            if (isEmpty(key)) {
                title = getString(R.string.add_book_source);
            } else {
                title = getString(R.string.edit_book_source);
                bookSourceBean = (BookSourceBean) BitIntentDataManager.getInstance().getData(key);
                serialNumber = bookSourceBean.getSerialNumber();
                enable = bookSourceBean.getEnable();
                BitIntentDataManager.getInstance().cleanData(key);
            }
        }
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();

        setHint();
        setText(bookSourceBean);
    }

    private void saveBookSource() {
        if (isEmpty(tieBookSourceName.getText()) || isEmpty(tieBookSourceUrl.getText())) {
            toast("书源名称和URL不能为空");
            return;
        }
        mPresenter.saveSource(getBookSource(), bookSourceBean);
    }

    @Override
    protected View getSnackBarView() {
        return toolbar;
    }

    @Override
    public void saveSuccess() {
        bookSourceBean = getBookSource();
        toast("保存成功");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public String getBookSourceStr() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        return gson.toJson(getBookSource());
    }

    private void scanBookSource() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        integrator.setCaptureActivity(QRCodeScanActivity.class);
        integrator.initiateScan();
    }

    private String trim(CharSequence string) {
        if (string == null) {
            return null;
        }
        return string.toString().trim();
    }

    private BookSourceBean getBookSource() {
        BookSourceBean bookSourceBeanN = new BookSourceBean();
        bookSourceBeanN.setBookSourceType(trim(tieBookSourceType.getText()));
        bookSourceBeanN.setBookSourceRuleType(trim(tieBookSourceRuleType.getText()));
        bookSourceBeanN.setBookSourceName(trim(tieBookSourceName.getText()));
        bookSourceBeanN.setBookSourceUrl(trim(tieBookSourceUrl.getText()));
        bookSourceBeanN.setBookSourceGroup(trim(tieBookSourceGroup.getText()));
        bookSourceBeanN.setCheckUrl(trim(tieCheckUrl.getText()));
        bookSourceBeanN.setLoginUrl(trim(tieLoginUrl.getText()));
        bookSourceBeanN.setRuleBookAuthor(trim(tieRuleBookAuthor.getText()));
        bookSourceBeanN.setRuleBookContent(trim(tieRuleBookContent.getText()));
        bookSourceBeanN.setRuleBookName(trim(tieRuleBookName.getText()));
        bookSourceBeanN.setRuleLastChapter(trim(tieRuleLastChapter.getText()));
        bookSourceBeanN.setRuleChapterList(trim(tieRuleChapterList.getText()));
        bookSourceBeanN.setRuleChapterName(trim(tieRuleChapterName.getText()));
        bookSourceBeanN.setRuleChapterUrl(trim(tieRuleChapterUrl.getText()));
        bookSourceBeanN.setRuleChapterUrlNext(trim(tieRuleChapterUrlNext.getText()));
        bookSourceBeanN.setRuleContentUrl(trim(tieRuleContentUrl.getText()));
        bookSourceBeanN.setRuleCoverUrl(trim(tieRuleCoverUrl.getText()));
        bookSourceBeanN.setRuleIntroduce(trim(tieRuleIntroduce.getText()));
        bookSourceBeanN.setRuleSearchAuthor(trim(tieRuleSearchAuthor.getText()));
        bookSourceBeanN.setRuleSearchCoverUrl(trim(tieRuleSearchCoverUrl.getText()));
        bookSourceBeanN.setRuleSearchKind(trim(tieRuleSearchKind.getText()));
        bookSourceBeanN.setRuleSearchLastChapter(trim(tieRuleSearchLastChapter.getText()));
        bookSourceBeanN.setRuleSearchList(trim(tieRuleSearchList.getText()));
        bookSourceBeanN.setRuleSearchName(trim(tieRuleSearchName.getText()));
        bookSourceBeanN.setRuleSearchNoteUrl(trim(tieRuleSearchNoteUrl.getText()));
        bookSourceBeanN.setRuleSearchUrl(trim(tieRuleSearchUrl.getText()));
        bookSourceBeanN.setHttpUserAgent(trim(tieHttpUserAgent.getText()));
        bookSourceBeanN.setRuleFindUrl(trim(tieRuleFindUrl.getText()));
        bookSourceBeanN.setRuleContentUrlNext(trim(tieRuleContentUrlNext.getText()));
        bookSourceBeanN.setEnable(enable);
        bookSourceBeanN.setSerialNumber(serialNumber);
        return bookSourceBeanN;
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        if (bookSourceBean == null) {
            return;
        }
        String bookType = trim(bookSourceBean.getBookSourceType());
        if(!TextUtils.isEmpty(bookType)) {
            tieBookSourceType.setText(bookType);
        }
        String ruleType = trim(bookSourceBean.getBookSourceRuleType());
        if(!TextUtils.isEmpty(ruleType)){
            tieBookSourceRuleType.setText(ruleType);
        }
        tieBookSourceName.setText(trim(bookSourceBean.getBookSourceName()));
        tieBookSourceUrl.setText(trim(bookSourceBean.getBookSourceUrl()));
        tieBookSourceGroup.setText(trim(bookSourceBean.getBookSourceGroup()));
        tieCheckUrl.setText(trim(bookSourceBean.getCheckUrl()));
        tieLoginUrl.setText(trim(bookSourceBean.getLoginUrl()));
        tieRuleBookAuthor.setText(trim(bookSourceBean.getRuleBookAuthor()));
        tieRuleBookContent.setText(trim(bookSourceBean.getRuleBookContent()));
        tieRuleBookName.setText(trim(bookSourceBean.getRuleBookName()));
        tieRuleLastChapter.setText(trim(bookSourceBean.getRuleLastChapter()));
        tieRuleChapterList.setText(trim(bookSourceBean.getRuleChapterList()));
        tieRuleChapterName.setText(trim(bookSourceBean.getRuleChapterName()));
        tieRuleChapterUrl.setText(trim(bookSourceBean.getRuleChapterUrl()));
        tieRuleChapterUrlNext.setText(trim(bookSourceBean.getRuleChapterUrlNext()));
        tieRuleContentUrl.setText(trim(bookSourceBean.getRuleContentUrl()));
        tieRuleCoverUrl.setText(trim(bookSourceBean.getRuleCoverUrl()));
        tieRuleIntroduce.setText(trim(bookSourceBean.getRuleIntroduce()));
        tieRuleSearchAuthor.setText(trim(bookSourceBean.getRuleSearchAuthor()));
        tieRuleSearchCoverUrl.setText(trim(bookSourceBean.getRuleSearchCoverUrl()));
        tieRuleSearchKind.setText(trim(bookSourceBean.getRuleSearchKind()));
        tieRuleSearchLastChapter.setText(trim(bookSourceBean.getRuleSearchLastChapter()));
        tieRuleSearchList.setText(trim(bookSourceBean.getRuleSearchList()));
        tieRuleSearchName.setText(trim(bookSourceBean.getRuleSearchName()));
        tieRuleSearchNoteUrl.setText(trim(bookSourceBean.getRuleSearchNoteUrl()));
        tieRuleSearchUrl.setText(trim(bookSourceBean.getRuleSearchUrl()));
        tieHttpUserAgent.setText(trim(bookSourceBean.getHttpUserAgent()));
        tieRuleFindUrl.setText(trim(bookSourceBean.getRuleFindUrl()));
        tieRuleContentUrlNext.setText(trim(bookSourceBean.getRuleContentUrlNext()));
    }

    private void setHint() {
        tilBookSourceType.setHint("书源类型");
        tilBookSourceRuleType.setHint("书源规则类型");
        tilBookSourceName.setHint("书源名称");
        tilBookSourceUrl.setHint("书源URL");
        tilBookSourceGroup.setHint("书源分组");
        tilCheckUrl.setHint("书源校验URL");
        tilLoginUrl.setHint("书源登录URL");
        tilRuleBookAuthor.setHint("作者获取规则");
        tilRuleBookContent.setHint("章节内容获取规则");
        tilRuleBookName.setHint("书名获取规则");
        tilRuleLastChapter.setHint("最新章节获取规则");
        tilRuleChapterList.setHint("目录列表获取规则");
        tilRuleChapterName.setHint("章节名称获取规则");
        tilRuleChapterUrl.setHint("目录URL获取规则");
        tilRuleChapterUrlNext.setHint("下一页目录URL获取规则");
        tilRuleContentUrl.setHint("章节内容URL获取规则");
        tilRuleCoverUrl.setHint("封面URL获取规则");
        tilRuleIntroduce.setHint("简介获取规则");
        tilRuleSearchAuthor.setHint("搜索结果作者获取规则");
        tilRuleSearchCoverUrl.setHint("搜索结果封面获取规则");
        tilRuleSearchKind.setHint("搜索分类获取规则");
        tilRuleSearchLastChapter.setHint("搜索最新章节获取规则");
        tilRuleSearchList.setHint("搜索结果列表获取规则");
        tilRuleSearchName.setHint("搜索结果书名获取规则");
        tilRuleSearchNoteUrl.setHint("搜索结果书籍URL获取规则");
        tilRuleSearchUrl.setHint("搜索URL");
        tilHttpUserAgent.setHint("用户代理");
        tilRuleFindUrl.setHint("发现获取规则");
        tilRuleContentUrlNext.setHint("下一页章节内容URL获取规则");
    }

    @SuppressLint("SetWorldReadable")
    private void shareBookSource() {
        Bitmap bitmap = mPresenter.encodeAsBitmap(getBookSourceStr());
        try {
            File file = new File(this.getExternalCacheDir(), "bookSource.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "分享书源"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterPermissionGranted(MApplication.RESULT__PERMS)
    private void selectLocalImage() {
        FileSelector.newInstance(true, false, true, new String[]{"png", "jpg", "jpeg"}).show(this, new FileSelector.OnFileSelectedListener() {
            @Override
            public void onSingleChoice(String path) {
                mPresenter.analyzeBitmap(path);
            }
        });
    }

    private void openRuleSummary() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.source_rule_url)));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open);
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_source_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveBookSource();
                break;
            case R.id.action_copy_source:
                mPresenter.copySource(getBookSource());
                break;
            case R.id.action_paste_source:
                mPresenter.pasteSource();
                break;
            case R.id.action_qr_code_camera:
                scanBookSource();
                break;
            case R.id.action_share_it:
                shareBookSource();
                break;
            case R.id.action_qr_code_image:
                if (EasyPermissions.hasPermissions(this, MApplication.PerList)) {
                    selectLocalImage();
                } else {
                    EasyPermissions.requestPermissions(this, "获取背景图片需存储权限", MApplication.RESULT__PERMS, MApplication.PerList);
                }
                break;
            case R.id.action_rule_summary:
                openRuleSummary();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                mPresenter.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (bookSourceBean == null) {
            bookSourceBean = new BookSourceBean();
        }
        if (!getBookSource().equals(bookSourceBean)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.exit))
                    .setMessage(getString(R.string.exit_no_save))
                    .setPositiveButton("是", (DialogInterface dialogInterface, int which) -> {
                    })
                    .setNegativeButton("否", (DialogInterface dialogInterface, int which) -> finish())
                    .show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        KeyboardUtil.hideKeyboard(this);
        super.finish();
    }
}
