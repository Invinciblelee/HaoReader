package com.monke.monkeybook.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.keyboard.KeyboardHeightProvider;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.utils.KeyboardUtil;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.activity.ChoiceBookActivity;
import com.monke.monkeybook.view.activity.SourceEditActivity;
import com.monke.monkeybook.view.adapter.FindBookAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindBookFragment extends BaseFragment<FindBookContract.Presenter> implements Refreshable, FindBookContract.View {

    @BindView(R.id.rv_find_book_list)
    RecyclerView rvFindList;
    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;
    @BindView(R.id.edit_find_search)
    AppCompatEditText searchEdit;
    @BindView(R.id.card_search_field)
    View searchField;

    private FindBookAdapter mAdapter;

    private KeyboardHeightProvider mHeightProvider;

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_find_book, container, false);
    }


    @Override
    protected void bindView() {
        ButterKnife.bind(this, view);

        rvFindList.setAdapter(mAdapter = new FindBookAdapter(requireContext()));
    }

    @Override
    protected void bindEvent() {
        mAdapter.setOnMultiItemClickListener(new FindBookAdapter.OnMultiItemClickListener() {
            @Override
            public void onItemGroupClick(FindKindGroupBean groupBean) {
                ChoiceBookActivity.startThis((MBaseActivity) getActivity(), groupBean);
            }

            @Override
            public void onItemGroupLongClick(FindKindGroupBean groupBean) {
                SourceEditActivity.startThis(FindBookFragment.this, BookSourceManager.getByUrl(groupBean.getTag()));
            }

            @Override
            public void onItemPreviewClick(SearchBookBean searchBookBean) {
                BookDetailActivity.startThis((MBaseActivity) getActivity(), searchBookBean);
            }
        });

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(s == null ? null : s.toString());
            }
        });

        mHeightProvider = new KeyboardHeightProvider(requireActivity()).init().setHeightListener(height -> {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) searchField.getLayoutParams();
            params.bottomMargin = height + DensityUtil.dp2px(requireContext(), 16);
            searchField.setLayoutParams(params);
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.initData();
    }

    @Override
    public void onRefresh() {
        if (mAdapter.getItemCount() == 0) {
            showProgress();
        }else {
            rvFindList.scrollToPosition(0);
        }
        mPresenter.initData();
    }

    @Override
    public void onRestore() {
        showProgress();
        mPresenter.initData();
    }

    @Override
    public void updateUI(List<FindKindGroupBean> group) {
        mAdapter.setItems(group);
        searchField.setVisibility(group.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void updateItem(FindKindGroupBean item) {
        mAdapter.updateItem(item);
    }

    @Override
    public void showProgress() {
        rvFindList.setVisibility(View.INVISIBLE);
        progressBar.show();
    }

    @Override
    public void hideProgress() {
        progressBar.hide();
        rvFindList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SourceEditActivity.REQUEST_EDIT_SOURCE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final int type = data.getIntExtra("type", 0);
                final String url = data.getStringExtra("url");
                if (type == -1) {
                    mAdapter.removeItem(new FindKindGroupBean(url));
                } else {
                    mPresenter.updateData(url);
                }
            }
        }
    }

    public boolean onBackPressed() {
        if (mHeightProvider.isKeyboardActive()) {
            KeyboardUtil.hideKeyboard(searchEdit);
            return true;
        }

        CharSequence text = searchEdit.getText();
        if (text != null && StringUtils.isNotBlank(text.toString())) {
            searchEdit.setText(null);
            return true;
        }
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect rect = new Rect();
        searchField.getGlobalVisibleRect(rect);
        if (!rect.contains((int) ev.getX(), (int) ev.getY()) && mHeightProvider.isKeyboardActive()) {
            KeyboardUtil.hideKeyboard(searchEdit);
            return true;
        }
        return false;
    }
}
