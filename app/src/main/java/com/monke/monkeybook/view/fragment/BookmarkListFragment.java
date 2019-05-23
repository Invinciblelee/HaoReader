package com.monke.monkeybook.view.fragment;

import android.os.Bundle;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.adapter.BookmarkListAdapter;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

public class BookmarkListFragment extends BaseChapterListFragment<BookmarkListAdapter> {


    public static BookmarkListFragment newInstance() {

        Bundle args = new Bundle();

        BookmarkListFragment fragment = new BookmarkListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_bookmark_list;
    }

    @Override
    public BookmarkListAdapter createAdapter() {
        return new BookmarkListAdapter(getContext());
    }

    @Override
    public void initView() {
        getAdapter().setOnItemClickListener(new BaseChapterListAdapter.OnItemClickListener<BookmarkBean>() {
            @Override
            public void itemClick(BookmarkBean item) {
                RxBus.get().post(RxBusTag.OPEN_BOOKMARK, item);
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean) {
                RxBus.get().post(RxBusTag.SHOW_BOOKMARK, bookmarkBean);
            }
        });
    }


    @Override
    public void updateBookShelf(BookShelfBean bookShelfBean) {
        getAdapter().setDataList(bookShelfBean.getBookmarkList());
    }
}
