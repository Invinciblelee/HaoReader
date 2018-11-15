package com.monke.monkeybook.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.presenter.FileSelectorPresenterImpl;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.view.adapter.FileSelectorAdapter;
import com.monke.monkeybook.widget.AppCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileSelector extends MBaseActivity<FileSelectorContract.Presenter> implements FileSelectorContract.View {

    public static final String RESULT = "RESULT";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_file_list)
    RecyclerView rvFileList;
    @BindView(R.id.loading_progress)
    ContentLoadingProgressBar progressBar;
    @BindView(R.id.fabComplete)
    FloatingActionButton fabComplete;

    MenuItem sortItem;
    SearchView searchView;
    SearchView.SearchAutoComplete searchAutoComplete;

    private FileSelectorAdapter adapter;

    public static void startThis(Activity activity, int requestCode, String title, FileSelectorContract.MediaType mediaType, String[] suffixes) {
        Intent intent = new Intent(activity, FileSelector.class);
        intent.putExtra("title", title);
        intent.putExtra("mediaType", (Parcelable) mediaType);
        intent.putStringArrayListExtra("suffixes", new ArrayList<>(Arrays.asList(suffixes)));
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected FileSelectorContract.Presenter initInjector() {
        return new FileSelectorPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_file_selector);
    }

    @Override
    protected void initData() {
        mPresenter.init(this);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();

        fabComplete.setOnClickListener(v -> {
            RipeFile file = adapter.getSelectedFile();
            if (file != null) {
                Intent intent = new Intent();
                intent.putExtra(RESULT, file.getPath());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        rvFileList.setHasFixedSize(true);
        if (mPresenter.getMediaType() == FileSelectorContract.MediaType.IMAGE) {
            rvFileList.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            rvFileList.setLayoutManager(new LinearLayoutManager(this));
            rvFileList.setPadding(0, 0, 0, 0);
        }

        rvFileList.setAdapter(adapter = new FileSelectorAdapter(this, mPresenter.getMediaType()));

        mPresenter.startLoad();
    }

    @Override
    public void onLoadFinish(List<RipeFile> files) {
        progressBar.hide();
        adapter.setItems(files);
    }

    @Override
    public void showLoading() {
        progressBar.show();
        adapter.clear();
    }

    @Override
    public void hideLoading() {
        progressBar.hide();
    }

    @Override
    public void showFabComplete() {
        if(fabComplete.isOrWillBeHidden()) {
            fabComplete.show();
        }
    }

    @Override
    public void showBigImage(View shareView, String url) {
        BigImageActivity.startThis(this, url, shareView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_selector, menu);
        sortItem = menu.findItem(R.id.action_sort);
        initSearchView(menu.findItem(R.id.action_search));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sort_name_asc:
                item.setChecked(true);
                mPresenter.sort(0);
                break;
            case R.id.item_sort_date_asc:
                item.setChecked(true);
                mPresenter.sort(1);
                break;
            case R.id.item_sort_length_asc:
                item.setChecked(true);
                mPresenter.sort(2);
                break;
            case R.id.item_sort_date_desc:
                item.setChecked(true);
                mPresenter.sort(3);
                break;
            case R.id.item_sort_length_desc:
                item.setChecked(true);
                mPresenter.sort(4);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchAutoComplete.isShown()) {
            searchView.clearFocus();
            searchView.setQuery(null, false);
            searchView.onActionViewCollapsed();
        } else {
            finish();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mPresenter.getTitle());
        }
    }

    private void initSearchView(MenuItem searchItem) {
        searchView = (SearchView) searchItem.getActionView();
        searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        AppCompat.useCustomIconForSearchView(searchView, "输入名称进行搜索");
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mPresenter.query(s);
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            sortItem.setVisible(!hasFocus);
        });
    }

}
