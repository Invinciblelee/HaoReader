package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FileSnapshot;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.presenter.FileSelectorPresenterImpl;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.BigImageActivity;
import com.monke.monkeybook.view.adapter.FileSelectorAdapter;
import com.monke.monkeybook.widget.theme.AppCompat;
import com.monke.monkeybook.widget.refreshview.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileSelectorDialog extends AppCompatDialog implements FileSelectorContract.View,
        View.OnClickListener, Toolbar.OnMenuItemClickListener, FileSelectorAdapter.OnItemClickListener
        , SwipeRefreshLayout.OnRefreshListener {

    private Toolbar toolbar;
    private RecyclerView rvFiles;
    private ContentLoadingProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;

    private Button okBth, backBtn, cancelBtn;

    private FileSelectorAdapter adapter;

    private OnFileSelectedListener selectedListener;

    private FileSelectorContract.Presenter mPresenter;

    public static FileSelectorDialog newInstance(String title, boolean singleChoice, boolean checkBookAdded, boolean isImage, String[] suffixes) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBoolean("isSingleChoice", singleChoice);
        args.putBoolean("checkBookAdded", checkBookAdded);
        args.putBoolean("isImage", isImage);
        args.putStringArrayList("suffixes", new ArrayList<>(Arrays.asList(suffixes)));
        FileSelectorDialog fragment = new FileSelectorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new FileSelectorPresenterImpl();
        mPresenter.attachView(this);
        mPresenter.init(this);
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_file_selector, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        rvFiles = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.loading_progress);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        okBth = view.findViewById(android.R.id.button1);
        cancelBtn = view.findViewById(android.R.id.button2);
        backBtn = view.findViewById(android.R.id.button3);
        initView();
    }

    private void initView() {
        if (mPresenter.getTitle() != null) {
            toolbar.setTitle(mPresenter.getTitle());
        }
        toolbar.inflateMenu(R.menu.menu_file_selector);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_format_line_spacing_black_24dp));
        AppCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.white));
        Menu menu = toolbar.getMenu();
        if (mPresenter.isSingleChoice()) {
            MenuItem item = menu.findItem(R.id.action_select_all);
            item.setEnabled(false);
            item.setVisible(false);
        }
        for (int i = 0; i < menu.size(); i++) {
            AppCompat.setTint(menu.getItem(i), getResources().getColor(R.color.white));
        }
        toolbar.setOnMenuItemClickListener(this);

        rvFiles.setAdapter(adapter = new FileSelectorAdapter(this, mPresenter.isSingleChoice(), mPresenter.checkBookAdded(), mPresenter.isImage()));

        refreshLayout.setOnRefreshListener(this);

        adapter.setOnItemClickListener(this);

        okBth.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        okBth.setText(R.string.ok);
        cancelBtn.setText(R.string.cancel);
        backBtn.setText(R.string.back);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new Handler().postDelayed(() -> mPresenter.startLoad(), 400L);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void showLoading() {
        progressBar.show();
        refreshLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.hide();
        refreshLayout.setVisibility(View.VISIBLE);
        refreshLayout.stopRefreshing();
    }

    @Override
    public int getScrollOffset() {
        return rvFiles == null ? 0 : rvFiles.computeVerticalScrollOffset();
    }

    @Override
    public void onShow(FileSnapshot snapshot, boolean back) {
        hideLoading();
        adapter.setItems(snapshot.getFiles());

        toolbar.setSubtitle(snapshot.getParent().getPath());

        if (back) {
            int oldScrollOffset = rvFiles.computeVerticalScrollOffset();
            rvFiles.scrollBy(0, snapshot.getScrollOffset() - oldScrollOffset);
        } else {
            rvFiles.scrollToPosition(0);
        }

        backBtn.setVisibility(mPresenter.canGoBack() ? View.VISIBLE : View.INVISIBLE);
        okBth.setText(R.string.ok);
    }

    @Override
    public void showBigImage(View shareView, String url) {
        BigImageActivity.startThis((AppCompatActivity) getActivity(), url, shareView);
    }

    @Override
    public void onRefresh() {
        mPresenter.refreshCurrent();
    }

    @Override
    public void onItemClick(View view, RipeFile file) {
        if (file.isDirectory()) {
            mPresenter.push(file, rvFiles.computeVerticalScrollOffset());
            okBth.setText(R.string.ok);
        } else if (!mPresenter.isSingleChoice()) {
            int count = adapter.getSelectedFiles().size();
            if (count == 0) {
                okBth.setText(R.string.ok);
            } else {
                okBth.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.ok), count));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == backBtn) {
            mPresenter.pop();
        } else if (view == cancelBtn) {
            dismiss();
        } else if (view == okBth) {
            boolean haveSelected = true;
            if (mPresenter.isSingleChoice()) {
                String path = adapter.getSelectedFile();
                if (path == null) {
                    haveSelected = false;
                } else if (selectedListener != null) {
                    dismiss();
                    selectedListener.onSingleChoice(path);
                }
            } else {
                List<String> paths = adapter.getSelectedFiles();
                if (paths.isEmpty()) {
                    haveSelected = false;
                } else if (selectedListener != null) {
                    dismiss();
                    selectedListener.onMultiplyChoice(paths);
                }
            }

            if (!haveSelected) {
                ToastUtils.toast(Objects.requireNonNull(getContext()), "请选择文件");
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int checkIndex = -1;
        switch (item.getItemId()) {
            case R.id.action_select_all:
                adapter.selectAll();
                int count = adapter.getSelectedFiles().size();
                if (count == 0) {
                    okBth.setText(R.string.ok);
                } else {
                    okBth.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.ok), count));
                }
                break;
            case R.id.item_sort_name_asc:
                checkIndex = 0;
                break;
            case R.id.item_sort_name_desc:
                checkIndex = 1;
                break;
            case R.id.item_sort_date_asc:
                checkIndex = 2;
                break;
            case R.id.item_sort_date_desc:
                checkIndex = 3;
                break;
            case R.id.item_sort_length_asc:
                checkIndex = 4;
                break;
            case R.id.item_sort_length_desc:
                checkIndex = 5;
                break;
        }
        if (checkIndex != -1) {
            item.setChecked(true);
            adapter.sort(mPresenter.sort(checkIndex));
        }
        return true;
    }

    public void show(AppCompatActivity activity, OnFileSelectedListener selectedListener) {
        this.selectedListener = selectedListener;
        super.show(activity.getSupportFragmentManager(), "FileSelectorDialog");
    }

    public static abstract class OnFileSelectedListener {
        public void onSingleChoice(String path) {

        }

        public void onMultiplyChoice(List<String> paths) {

        }
    }
}
