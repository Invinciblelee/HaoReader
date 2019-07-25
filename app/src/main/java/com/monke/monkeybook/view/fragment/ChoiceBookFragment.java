package com.monke.monkeybook.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.monke.basemvplib.BaseFragment;
import com.monke.basemvplib.NetworkUtil;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.presenter.ChoiceBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ChoiceBookContract;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.activity.SearchBookActivity;
import com.monke.monkeybook.view.adapter.ChoiceBookAdapter;
import com.monke.monkeybook.widget.refreshview.OnLoadMoreListener;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ChoiceBookFragment extends BaseFragment<ChoiceBookContract.Presenter> implements ChoiceBookContract.View {

    @BindView(R.id.rfRv_search_books)
    RefreshRecyclerView rfRvSearchBooks;

    private ChoiceBookAdapter searchBookAdapter;

    private Unbinder unbinder;

    public static ChoiceBookFragment newInstance(String tag, String url) {
        Bundle args = new Bundle();
        args.putString("tag", tag);
        args.putString("url", url);
        ChoiceBookFragment fragment = new ChoiceBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ChoiceBookContract.Presenter initInjector() {
        return new ChoiceBookPresenterImpl(getArguments());
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_choice_book, container, false);
    }

    @Override
    protected void bindView() {
        unbinder = ButterKnife.bind(this, view);

        searchBookAdapter = new ChoiceBookAdapter(getActivity());
        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(requireContext()));
        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        rfRvSearchBooks.getRecyclerView().setClipToPadding(false);
        rfRvSearchBooks.getRecyclerView().setPadding(0, padding, 0, padding);

        View viewRefreshError = LayoutInflater.from(requireContext()).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            searchBookAdapter.replaceAll(null);
            rfRvSearchBooks.startRefresh();
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(requireContext()).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);
    }

    @Override
    protected void bindEvent() {
        searchBookAdapter.setItemClickListener(new ChoiceBookAdapter.OnItemClickListener() {
            @Override
            public void clickToSearch(View clickView, int position, SearchBookBean searchBookBean) {
                SearchBookActivity.startByKey((MBaseActivity) getActivity(), searchBookBean.getName());
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                BookDetailActivity.startThis((MBaseActivity) getActivity(), searchBookBean);
            }
        });

        rfRvSearchBooks.setOnRefreshListener(() -> {
            rfRvSearchBooks.resetLoadMore();
            mPresenter.initPage();
            mPresenter.toSearchBooks(null);
        });

        rfRvSearchBooks.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadMore() {
                mPresenter.toSearchBooks(null);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toSearchBooks(null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void refreshSearchBook(List<SearchBookBean> books) {
        searchBookAdapter.replaceAll(books);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        if (books.isEmpty()) {
            loadMoreFinish(true);
            return;
        }
        searchBookAdapter.addAll(books);
        loadMoreFinish(false);
    }

    @Override
    public void searchBookError(boolean isRefresh, String errorMsg) {
        if (isRefresh) {
            //刷新失败
            if (!NetworkUtil.isNetworkAvailable()) {
                rfRvSearchBooks.refreshError("网络不可用");
            } else {
                rfRvSearchBooks.refreshError(errorMsg);
            }
        } else {
            rfRvSearchBooks.loadMoreError();
        }
    }

    @Override
    public ChoiceBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    protected void firstRequest() {
        rfRvSearchBooks.startRefresh();
    }
}
