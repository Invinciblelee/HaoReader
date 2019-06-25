package com.monke.monkeybook.view.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.view.activity.MainActivity;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.widget.BookFloatingActionMenu;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainBookListFragment extends BaseFragment implements Refreshable{

    private static final int[] BOOK_GROUPS = {R.string.item_group_zg, R.string.item_group_yf, R.string.item_group_wj,
            R.string.item_group_bd, R.string.item_group_ys, R.string.item_group_mh};

    @BindView(R.id.book_shelf_menu)
    BookFloatingActionMenu bookShelfMenu;

    private int group = -1;

    private BookListFragment[] fragments = new BookListFragment[4];

    @Override
    protected void initData() {
        group = AppConfigHelper.get().getInt("shelfGroup", 0);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_main_book_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FragmentManager manager = getChildFragmentManager();
            fragments[0] = (BookListFragment) manager.findFragmentByTag(getString(BOOK_GROUPS[0]));
            fragments[1] = (BookListFragment) manager.findFragmentByTag(getString(BOOK_GROUPS[1]));
            fragments[2] = (BookListFragment) manager.findFragmentByTag(getString(BOOK_GROUPS[2]));
            fragments[3] = (BookListFragment) manager.findFragmentByTag(getString(BOOK_GROUPS[3]));

            for (BookListFragment fragment : fragments) {
                if (fragment != null) {
                    fragment.setItemClickListenerTwo(getAdapterListener());
                }
            }
        } else {
            showFragment(this.group);
        }

    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this, view);

        upGroup(group);

        bookShelfMenu.setSelection(group);
    }

    @Override
    protected void bindEvent() {
        bookShelfMenu.setOnActionMenuClickListener(new BookFloatingActionMenu.OnActionMenuClickListener() {
            @Override
            public void onMainLongClick(View fabMain) {
                BookListFragment current = fragments[group];
                if (current != null) {
                    current.refreshBookShelf(true);
                }
            }

            @Override
            public void onMenuClick(int index, View menuView) {
                bookShelfMenu.postDelayed(() -> upGroup(index), 400L);
            }
        });
    }

    public void addBookSuccess(BookShelfBean bookShelfBean) {
        BookListFragment fragment = fragments[fragments.length - 1];
        if (fragment != null) {
            fragment.addBookShelf(bookShelfBean);
        }
        upGroup(bookShelfBean.getGroup());
        bookShelfMenu.setSelection(this.group);
    }

    public void clearBookshelf() {
        for (BookListFragment fragment : fragments) {
            if (fragment != null) {
                fragment.clearBookShelf();
            }
        }
    }

    public void upLayoutType(boolean viewIsList) {
        for (BookListFragment fragment : fragments) {
            if (fragment != null) {
                fragment.updateLayoutType(viewIsList);
            }
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (bookShelfMenu.isExpanded()) {
            Rect rect = new Rect();
            bookShelfMenu.getGlobalVisibleRect(rect);
            if (!rect.contains((int) ev.getX(), (int) ev.getY())) {
                bookShelfMenu.collapse();
                return true;
            }
        }
        return false;
    }

    public boolean onBackPressed() {
        if (bookShelfMenu.isExpanded()) {
            bookShelfMenu.collapse();
            return true;
        }
        return false;
    }

    private void upGroup(int group) {
        group = Math.max(0, group);
        group = Math.min(group, fragments.length - 1);
        if (this.group != group) {
            showFragment(group);

            AppConfigHelper.get().edit().putInt("shelfGroup", group).apply();

            this.group = group;
        }
    }

    private void showFragment(int group) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        BookListFragment from = fragments[this.group];
        BookListFragment to = fragments[group];
        if (from != null) {
            transaction.hide(from);
        }

        if (to == null) {
            to = fragments[group] = BookListFragment.newInstance(group);
            to.setItemClickListenerTwo(getAdapterListener());
        }

        if (!to.isAdded()) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.book_list_frame, to, getString(BOOK_GROUPS[group]))
                    .show(to)
                    .commitAllowingStateLoss();
        } else if (to.isSupportHidden()) {
            transaction.setTransition(this.group > group ? FragmentTransaction.TRANSIT_FRAGMENT_OPEN : FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .show(to)
                    .commitAllowingStateLoss();
        }
    }

    private OnBookItemClickListenerTwo getAdapterListener() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getAdapterListener();
        }
        return null;
    }

    @Override
    public void onRefresh() {
        BookListFragment current = fragments[group];
        if (current != null) {
            current.refreshBookShelf(true);
        }
    }

    @Override
    public void onRestore() {
        for (BookListFragment fragment : fragments) {
            if (fragment != null) {
                fragment.refreshBookShelf(false);
            }
        }
    }
}
