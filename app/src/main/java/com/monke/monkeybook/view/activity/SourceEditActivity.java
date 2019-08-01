package com.monke.monkeybook.view.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.monkeybook.BuildConfig;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.WebLoadConfig;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.permission.Permissions;
import com.monke.monkeybook.help.permission.PermissionsCompat;
import com.monke.monkeybook.presenter.SourceEditPresenterImpl;
import com.monke.monkeybook.presenter.contract.SourceEditContract;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.view.popupwindow.KeyboardToolPop;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/26.
 * 编辑书源
 */

public class SourceEditActivity extends MBaseActivity<SourceEditContract.Presenter> implements SourceEditContract.View {
    public final static int REQUEST_EDIT_SOURCE = 1101;
    public final static int REQUEST_QR_SCAN = 1102;

    private static final int POP_TOOL_HEIGHT = 100;

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
    @BindView(R.id.tie_ruleSearchIntroduce)
    TextInputEditText tieRuleSearchIntroduce;
    @BindView(R.id.til_ruleSearchIntroduce)
    TextInputLayout tilRuleSearchIntroduce;
    @BindView(R.id.tie_ruleSearchCoverUrl)
    TextInputEditText tieRuleSearchCoverUrl;
    @BindView(R.id.til_ruleSearchCoverUrl)
    TextInputLayout tilRuleSearchCoverUrl;
    @BindView(R.id.tie_ruleSearchNoteUrl)
    TextInputEditText tieRuleSearchNoteUrl;
    @BindView(R.id.til_ruleSearchNoteUrl)
    TextInputLayout tilRuleSearchNoteUrl;
    @BindView(R.id.tie_rulePersistedVariables)
    TextInputEditText tiePersistedVariables;
    @BindView(R.id.til_rulePersistedVariables)
    TextInputLayout tilRulePersistedVariables;
    @BindView(R.id.tie_ruleBookName)
    TextInputEditText tieRuleBookName;
    @BindView(R.id.til_ruleBookName)
    TextInputLayout tilRuleBookName;
    @BindView(R.id.tie_ruleBookAuthor)
    TextInputEditText tieRuleBookAuthor;
    @BindView(R.id.til_ruleBookAuthor)
    TextInputLayout tilRuleBookAuthor;
    @BindView(R.id.til_ruleBookLastChapter)
    TextInputLayout tilRuleBookLastChapter;
    @BindView(R.id.tie_ruleBookLastChapter)
    TextInputEditText tieRuleBookLastChapter;
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
    @BindView(R.id.tie_ruleChapterUrlNext)
    TextInputEditText tieRuleChapterUrlNext;
    @BindView(R.id.til_ruleChapterUrlNext)
    TextInputLayout tilRuleChapterUrlNext;
    @BindView(R.id.tie_ruleContentUrlNext)
    TextInputEditText tieRuleContentUrlNext;
    @BindView(R.id.til_ruleContentUrlNext)
    TextInputLayout tilRuleContentUrlNext;
    @BindView(R.id.scroll_view)
    NestedScrollView scrollContent;
    @BindView(R.id.switch_layout)
    View switchLayout;
    @BindView(R.id.checker_enable_source)
    AppCompatCheckBox sourceEnableChecker;
    @BindView(R.id.checker_enable_find)
    AppCompatCheckBox findEnableChecker;

    private BookSourceBean bookSourceBean;
    private int serialNumber;
    private int weight;
    private String title;
    private KeyboardToolPop mSoftKeyboardTool;
    private boolean mIsSoftKeyBoardShowing = false;


    private KeyboardOnGlobalChangeListener mKeyboardListener;

    public static void startThis(Activity activity, BookSourceBean sourceBean) {
        Intent intent = new Intent(activity, SourceEditActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        activity.startActivityForResult(intent, REQUEST_EDIT_SOURCE);
    }

    public static void startThis(Fragment fragment, BookSourceBean sourceBean) {
        Intent intent = new Intent(fragment.requireContext(), SourceEditActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, sourceBean.clone());
        fragment.startActivityForResult(intent, REQUEST_EDIT_SOURCE);
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
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putInt("serialNumber", serialNumber);
        if (bookSourceBean != null) {
            String key = String.valueOf(System.currentTimeMillis());
            getIntent().putExtra("data_key", key);
            BitIntentDataManager.getInstance().putData(key, bookSourceBean);
        }
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_source_edit);

    }

    @Override
    protected void initData() {
        String key = getIntent().getStringExtra("data_key");
        if (isEmpty(key)) {
            title = getString(R.string.add_book_source);
        } else {
            title = getString(R.string.edit_book_source);
            bookSourceBean = BitIntentDataManager.getInstance().getData(key, null);
            if (bookSourceBean != null) {
                BitIntentDataManager.getInstance().cleanData(key);
            }
        }
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        setHint();
        setText(bookSourceBean);
        mSoftKeyboardTool = new KeyboardToolPop(this, this::insertTextToEditText);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mKeyboardListener = new KeyboardOnGlobalChangeListener());
    }

    @Override
    protected void bindEvent() {
        scrollContent.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                ViewCompat.setElevation(switchLayout, v.canScrollVertically(-1) ? DensityUtil.dp2px(v.getContext(), 3) : 0));
    }

    private void saveBookSource() {
        if (isEmpty(tieBookSourceName.getText()) || isEmpty(tieBookSourceUrl.getText())) {
            toast("书源名称和URL不能为空");
            return;
        }
        mPresenter.saveSource(getBookSource(), bookSourceBean, false);
    }

    @Override
    protected View getSnackBarView() {
        return toolbar;
    }

    @Override
    public void saveSuccess() {
        bookSourceBean = getBookSource();
        toast("保存成功");
        setResult(bookSourceBean);
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

    @Override
    public String getBookSourceName() {
        return trim(tieBookSourceName.getText());
    }

    private void setResult(BookSourceBean sourceBean) {
        setBasicInfo(sourceBean);

        Intent data = new Intent();
        data.putExtra("url", sourceBean.getBookSourceUrl());
        data.putExtra("type", (StringUtils.isBlank(sourceBean.getRuleFindUrl()) || !sourceBean.getEnableFind()) ? -1 : 0);
        setResult(RESULT_OK, data);
    }

    private void scanBookSource() {
        new PermissionsCompat.Builder(this)
                .addPermissions(Permissions.Group.CAMERA)
                .addPermissions(Permissions.Group.STORAGE)
                .rationale("相机/存储")
                .onGranted(requestCode -> {
                    Intent intent = new Intent(SourceEditActivity.this, QRCodeScanActivity.class);
                    startActivityForResult(intent, REQUEST_QR_SCAN);
                })
                .request();
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
        bookSourceBeanN.setRuleBookAuthor(trim(tieRuleBookAuthor.getText()));
        bookSourceBeanN.setRuleBookContent(trim(tieRuleBookContent.getText()));
        bookSourceBeanN.setRulePersistedVariables(trim(tiePersistedVariables.getText()));
        bookSourceBeanN.setRuleBookName(trim(tieRuleBookName.getText()));
        bookSourceBeanN.setRuleBookLastChapter(trim(tieRuleBookLastChapter.getText()));
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
        bookSourceBeanN.setRuleSearchIntroduce(trim(tieRuleSearchIntroduce.getText()));
        bookSourceBeanN.setRuleSearchList(trim(tieRuleSearchList.getText()));
        bookSourceBeanN.setRuleSearchName(trim(tieRuleSearchName.getText()));
        bookSourceBeanN.setRuleSearchNoteUrl(trim(tieRuleSearchNoteUrl.getText()));
        bookSourceBeanN.setRuleSearchUrl(trim(tieRuleSearchUrl.getText()));
        bookSourceBeanN.setHttpUserAgent(trim(tieHttpUserAgent.getText()));
        bookSourceBeanN.setRuleFindUrl(trim(tieRuleFindUrl.getText()));
        bookSourceBeanN.setRuleContentUrlNext(trim(tieRuleContentUrlNext.getText()));
        bookSourceBeanN.setEnable(sourceEnableChecker.isChecked());
        bookSourceBeanN.setEnableFind(findEnableChecker.isChecked());
        bookSourceBeanN.setSerialNumber(serialNumber);
        bookSourceBeanN.setWeight(weight);
        return bookSourceBeanN;
    }

    @Override
    public void setText(BookSourceBean bookSourceBean) {
        setBasicInfo(bookSourceBean);
        if (bookSourceBean == null) {
            return;
        }

        String bookType = trim(bookSourceBean.getBookSourceType());
        if (!TextUtils.isEmpty(bookType)) {
            tieBookSourceType.setText(bookType);
        }
        String ruleType = trim(bookSourceBean.getBookSourceRuleType());
        if (!TextUtils.isEmpty(ruleType)) {
            tieBookSourceRuleType.setText(ruleType);
        }
        tieBookSourceName.setText(trim(bookSourceBean.getBookSourceName()));
        tieBookSourceUrl.setText(trim(bookSourceBean.getBookSourceUrl()));
        tieBookSourceGroup.setText(trim(bookSourceBean.getBookSourceGroup()));
        tieCheckUrl.setText(trim(bookSourceBean.getCheckUrl()));
        tieRuleBookAuthor.setText(trim(bookSourceBean.getRuleBookAuthor()));
        tieRuleBookContent.setText(trim(bookSourceBean.getRuleBookContent()));
        tiePersistedVariables.setText(trim(bookSourceBean.getRulePersistedVariables()));
        tieRuleBookName.setText(trim(bookSourceBean.getRuleBookName()));
        tieRuleBookLastChapter.setText(trim(bookSourceBean.getRuleBookLastChapter()));
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
        tieRuleSearchIntroduce.setText(trim(bookSourceBean.getRuleSearchIntroduce()));
        tieRuleSearchList.setText(trim(bookSourceBean.getRuleSearchList()));
        tieRuleSearchName.setText(trim(bookSourceBean.getRuleSearchName()));
        tieRuleSearchNoteUrl.setText(trim(bookSourceBean.getRuleSearchNoteUrl()));
        tieRuleSearchUrl.setText(trim(bookSourceBean.getRuleSearchUrl()));
        tieHttpUserAgent.setText(trim(bookSourceBean.getHttpUserAgent()));
        tieRuleFindUrl.setText(trim(bookSourceBean.getRuleFindUrl()));
        tieRuleContentUrlNext.setText(trim(bookSourceBean.getRuleContentUrlNext()));
    }

    @Override
    public void toDebug(BookSourceBean bookSourceBean) {
        this.bookSourceBean = bookSourceBean;
        setResult(bookSourceBean);
        SourceDebugActivity.startThis(SourceEditActivity.this, getBookSource().getBookSourceUrl());
    }

    private void setHint() {
        tilBookSourceType.setHint("书源类型(BookSourceType)");
        tilBookSourceRuleType.setHint("书源规则类型(BookSourceRuleType)");
        tilBookSourceName.setHint("书源名称(BookSourceName)");
        tilBookSourceUrl.setHint("书源URL(BookSourceUrl)");
        tilBookSourceGroup.setHint("书源分组(BookSourceGroup)");
        tilCheckUrl.setHint("书源校验URL(CheckUrl)");
        tilRuleBookAuthor.setHint("作者获取规则(RuleBookAuthor)");
        tilRuleBookContent.setHint("内容获取规则(RuleBookContent)");
        tilRuleBookName.setHint("书名获取规则(RuleBookName)");
        tilRuleBookLastChapter.setHint("最新章节获取规则(RuleBookLastChapter)");
        tilRuleChapterList.setHint("目录列表获取规则(RuleChapterList)");
        tilRuleChapterName.setHint("章节名称获取规则(RuleChapterName)");
        tilRuleChapterUrl.setHint("目录URL获取规则(RuleChapterUrl)");
        tilRuleChapterUrlNext.setHint("下一页目录URL获取规则(RuleChapterUrlNext)");
        tilRuleContentUrl.setHint("内容URL获取规则(RuleContentUrl)");
        tilRuleCoverUrl.setHint("封面URL获取规则(RuleCoverUrl)");
        tilRuleIntroduce.setHint("简介获取规则(RuleIntroduce)");
        tilRuleSearchAuthor.setHint("搜索结果作者获取规则(RuleSearchAuthor)");
        tilRuleSearchCoverUrl.setHint("搜索结果封面获取规则(RuleSearchCoverUrl)");
        tilRuleSearchKind.setHint("搜索分类获取规则(RuleSearchKind)");
        tilRuleSearchLastChapter.setHint("搜索最新章节获取规则(RuleSearchLastChapter)");
        tilRuleSearchIntroduce.setHint("搜索简介获取规则(RuleSearchIntroduce)");
        tilRuleSearchList.setHint("搜索结果列表获取规则(RuleSearchList)");
        tilRuleSearchName.setHint("搜索结果书名获取规则(RuleSearchName)");
        tilRuleSearchNoteUrl.setHint("搜索结果书籍URL获取规则(RuleSearchNoteUrl)");
        tilRuleSearchUrl.setHint("搜索URL(SearchUrl)");
        tilRulePersistedVariables.setHint("持久化变量(RulePersistedVariables)");
        tilHttpUserAgent.setHint("用户代理(HttpUserAgent)");
        tilRuleFindUrl.setHint("发现获取规则(RuleFinalUrl)");
        tilRuleContentUrlNext.setHint("下一页内容URL获取规则(RuleContentUrlNext)");
    }

    private void setBasicInfo(BookSourceBean sourceBean) {
        if (sourceBean == null) {
            sourceEnableChecker.setChecked(true);
            findEnableChecker.setChecked(true);
            return;
        }

        serialNumber = sourceBean.getSerialNumber();
        weight = sourceBean.getWeight();

        sourceEnableChecker.setChecked(sourceBean.getEnable());
        findEnableChecker.setChecked(sourceBean.getEnableFind());
    }

    @Override
    public void shareSource(File file, String mediaType) {
        Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.setType(mediaType);
        startActivity(Intent.createChooser(intent, "分享书源"));
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    private boolean canSaveBookSource() {
        if (isEmpty(trim(tieBookSourceName.getText())) || isEmpty(trim(tieBookSourceUrl.getText()))) {
            toast(R.string.non_null_source_name_url);
            return false;
        }
        return true;
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
                mPresenter.handleSourceShare();
                break;
            case R.id.action_open:
                String url = trim(tieBookSourceUrl.getText());
                if (isEmpty(url)) {
                    toast("请先配置书源URL");
                } else {
                    WebLoadConfig config = new WebLoadConfig(url, trim(tieHttpUserAgent.getText()));
                    WebViewActivity.startThis(this, config);
                }
                break;
            case R.id.action_debug:
                if (canSaveBookSource()) {
                    mPresenter.saveSource(getBookSource(), bookSourceBean, true);
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK && null != data) {
            String result = data.getStringExtra("result");
            if (!TextUtils.isEmpty(result)) {
                mPresenter.setText(result);
            }
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

    @Override
    protected void onDestroy() {
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mKeyboardListener);
        super.onDestroy();
        closePopupWindow();
    }

    private void insertTextToEditText(String txt) {
        if (TextUtils.isEmpty(txt)) return;
        View view = getWindow().getDecorView().findFocus();
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            Editable edit = editText.getEditableText();//获取EditText的文字
            if (start < 0 || start >= edit.length()) {
                edit.append(txt);
            } else {
                edit.replace(start, end, txt);//光标所在位置插入文字
            }
        }
    }

    private void showKeyboardTopPopupWindow() {
        if (isFinishing()) {
            return;
        }

        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            return;
        }
        if (mSoftKeyboardTool != null) {
            mSoftKeyboardTool.showAtLocation(rlContent, Gravity.BOTTOM, 0, 0);
        }
    }

    private void closePopupWindow() {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前页面窗口的显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = ScreenUtils.getScreenHeight(SourceEditActivity.this);
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean preShowing = mIsSoftKeyBoardShowing;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsSoftKeyBoardShowing = true; // 超过
                scrollContent.setPadding(0, 0, 0, POP_TOOL_HEIGHT);
                showKeyboardTopPopupWindow();
                if (!preShowing) {
                    scrollContent.post(() -> scrollContent.scrollBy(0, POP_TOOL_HEIGHT));
                }
            } else {
                mIsSoftKeyBoardShowing = false;
                scrollContent.setPadding(0, 0, 0, 0);
                if (preShowing) {
                    closePopupWindow();
                    scrollContent.post(() -> {
                        View contentView = scrollContent.getChildAt(0);
                        if (contentView.getMeasuredHeight() - POP_TOOL_HEIGHT > scrollContent.getScrollY() + scrollContent.getHeight()) {
                            scrollContent.scrollBy(0, -POP_TOOL_HEIGHT);
                        }
                    });
                }
            }
        }
    }
}
