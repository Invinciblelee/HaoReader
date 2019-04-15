package com.monke.monkeybook.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookShelfHolder;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseChapterListFragment<ADT extends BaseChapterListAdapter> extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView rvList;

    private Unbinder unbinder;

    BookShelfBean bookShelf;

    private LinearLayoutManager layoutManager;

    private ADT adapter;

    public abstract int getLayoutResID();

    public abstract ADT createAdapter();

    public boolean reverseLayout() {
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookShelf = BookShelfHolder.get().getBook();
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResID(), container, false);
    }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        rvList.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, reverseLayout()));
        rvList.setHasFixedSize(true);
        rvList.setItemAnimator(null);
        rvList.setAdapter(adapter = createAdapter());
        initView();

        BookShelfHolder.get().observe(this, bookShelfBean -> {
            bookShelf = bookShelfBean;
            updateBookShelf(bookShelfBean);
        });
    }

    @Override
    public void onDestroyView() {
        BookShelfHolder.get().unsubscribe(this);
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    public abstract void initView();

    public BaseChapterListAdapter.MyFilter getFilter() {
        return adapter.getFilter();
    }

    public ADT getAdapter() {
        return adapter;
    }

    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void updateBookShelf(BookShelfBean bookShelfBean) {

    }
}
