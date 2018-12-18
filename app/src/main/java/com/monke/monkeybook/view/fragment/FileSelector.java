package com.monke.monkeybook.view.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.FileSnapshot;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.presenter.FileSelectorPresenterImpl;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.view.activity.BigImageActivity;
import com.monke.monkeybook.view.adapter.FileSelectorAdapter;
import com.monke.monkeybook.widget.AppCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileSelector extends AppCompatDialogFragment implements FileSelectorContract.View,
        View.OnClickListener, Toolbar.OnMenuItemClickListener, FileSelectorAdapter.OnItemClickListener {

    Toolbar toolbar;
    RecyclerView rvFiles;
    ContentLoadingProgressBar progressBar;

    Button okBth, backBtn;

    boolean isShowing;

    private FileSelectorAdapter adapter;

    private OnFileSelectedListener selectedListener;

    private FileSelectorContract.Presenter mPresenter;

    public static FileSelector newInstance(boolean singleChoice, boolean checkBookAdded, boolean isImage, String[] suffixes) {
        Bundle args = new Bundle();
        args.putBoolean("isSingleChoice", singleChoice);
        args.putBoolean("checkBookAdded", checkBookAdded);
        args.putBoolean("isImage", isImage);
        args.putStringArrayList("suffixes", new ArrayList<>(Arrays.asList(suffixes)));
        FileSelector fragment = new FileSelector();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Style_Dialog_Files);

        mPresenter = new FileSelectorPresenterImpl();
        mPresenter.attachView(this);
        mPresenter.init(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()), getTheme());
        builder.setNeutralButton(R.string.back, null);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_file_selector, null);
        builder.setView(view);
        toolbar = view.findViewById(R.id.toolbar);
        rvFiles = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.loading_progress);
        initView();
        return builder.create();
    }

    private void initView() {
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

        adapter.setOnItemClickListener(this);

        mPresenter.startLoad();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isShowing) {
            AlertDialog dialog = (AlertDialog) getDialog();
            okBth = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            backBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            backBtn.setVisibility(View.INVISIBLE);

            backBtn.setOnClickListener(this);
            okBth.setOnClickListener(this);
            isShowing = true;
        }
    }

    @Override
    public void showSubtitle(String subtitle) {
        toolbar.setSubtitle(subtitle);
    }

    @Override
    public void showLoading() {
        progressBar.show();
    }

    @Override
    public void hideLoading() {
        progressBar.hide();
    }

    @Override
    public void onShow(FileSnapshot snapshot, boolean back) {
        progressBar.hide();
        adapter.reset();
        if (snapshot == null) {
            adapter.setItems(null);
        } else {
            adapter.setItems(snapshot.getFiles());

            toolbar.setSubtitle(snapshot.getParent().getPath());

            if (back) {
                int oldScrollOffset = rvFiles.computeVerticalScrollOffset();
                rvFiles.scrollBy(0, snapshot.getScrollOffset() - oldScrollOffset);
                if (!mPresenter.canGoBack()) {
                    backBtn.setVisibility(View.INVISIBLE);
                }
            } else {
                rvFiles.scrollToPosition(0);
                if (backBtn != null && mPresenter.canGoBack()) {
                    backBtn.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void showBigImage(View shareView, String url) {
        BigImageActivity.startThis((MBaseActivity) getActivity(), url, shareView);
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
    public void onClick(View v) {
        if (v == backBtn) {
            mPresenter.pop();
        } else if (v == okBth) {
            boolean haveSelected = true;
            if (mPresenter.isSingleChoice()) {
                String path = adapter.getSelectedFile();
                if (path == null) {
                    haveSelected = false;
                } else if (selectedListener != null) {
                    dismissAllowingStateLoss();
                    selectedListener.onSingleChoice(path);
                }
            } else {
                List<String> paths = adapter.getSelectedFiles();
                if (paths.isEmpty()) {
                    haveSelected = false;
                } else if (selectedListener != null) {
                    dismissAllowingStateLoss();
                    selectedListener.onMultiplyChoice(paths);
                }
            }

            if (!haveSelected) {
                Toast.makeText(getContext(), "请选择文件", Toast.LENGTH_SHORT).show();
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
        super.show(activity.getSupportFragmentManager(), "FileSelector");
    }

    public static abstract class OnFileSelectedListener {
        public void onSingleChoice(String path) {

        }

        public void onMultiplyChoice(List<String> paths) {

        }
    }
}
