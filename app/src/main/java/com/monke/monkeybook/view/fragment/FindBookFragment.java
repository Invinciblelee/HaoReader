package com.monke.monkeybook.view.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.view.adapter.FindBookAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindBookFragment extends BaseFragment<FindBookContract.Presenter> implements Refreshable, FindBookContract.View {


    @BindView(R.id.rv_find_book_list)
    RecyclerView rvFindList;
    @BindView(R.id.progress_bar)
    ContentLoadingProgressBar progressBar;

    private FindBookAdapter mAdapter;

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
    protected void firstRequest() {
        mPresenter.initData();
    }

    @Override
    public void onRefresh() {
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
    }

    @Override
    public void updateItem(FindKindGroupBean item) {
        mAdapter.updateItem(item);
    }

    @Override
    public void showProgress() {
        progressBar.show();
    }

    @Override
    public void hideProgress() {
        progressBar.hide();
    }
}
