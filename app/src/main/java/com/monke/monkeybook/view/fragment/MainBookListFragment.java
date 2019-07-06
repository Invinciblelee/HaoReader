package com.monke.monkeybook.view.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.view.activity.MainActivity;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.widget.BookFloatingActionMenu;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainBookListFragment extends BaseFragment implements FragmentTrigger {

    private static final int[] BOOK_GROUPS = {R.string.item_group_zg, R.string.item_group_yf, R.string.item_group_wj,
            R.string.item_group_bd, R.string.item_group_ys, R.string.item_group_mh};

    @BindView(R.id.book_shelf_menu)
    BookFloatingActionMenu bookShelfMenu;

    private int group = -1;

    private Fragment[] fragments = new Fragment[5];

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
            fragments[0] = manager.findFragmentByTag(getString(BOOK_GROUPS[0]));
            fragments[1] = manager.findFragmentByTag(getString(BOOK_GROUPS[1]));
            fragments[2] = manager.findFragmentByTag(getString(BOOK_GROUPS[2]));
            fragments[3] = manager.findFragmentByTag(getString(BOOK_GROUPS[3]));
            fragments[4] = manager.findFragmentByTag(getString(BOOK_GROUPS[4]));

            for (Fragment fragment : fragments) {
                if (fragment instanceof BookListFragment) {
                    ((BookListFragment) fragment).setItemClickListenerTwo(getAdapterListener());
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
                refreshFragment(fragments[group]);
            }

            @Override
            public void onMenuClick(int index, View menuView) {
                bookShelfMenu.postDelayed(() -> upGroup(index), 400L);
            }
        });
    }

    private void refreshFragment(Fragment fragment) {
        if (fragment instanceof BookListFragment) {
            ((BookListFragment) fragment).refreshBookShelf(true);
        } else if (fragment instanceof AudioBookFragment) {
            ((AudioBookFragment) fragment).onRefresh();
        }
    }

    private void scrollTop(Fragment fragment) {
        if (fragment instanceof BookListFragment) {
            ((BookListFragment) fragment).scrollToTop();
        } else if (fragment instanceof AudioBookFragment) {
            ((AudioBookFragment) fragment).onReselected();
        }
    }

    public void addBookSuccess(BookShelfBean bookShelfBean) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setCurrentItem(0);
        }

        Fragment fragment = fragments[fragments.length - 2];
        if (fragment instanceof BookListFragment) {
            ((BookListFragment) fragment).addBookShelf(bookShelfBean);
        }
        upGroup(bookShelfBean.getGroup());
        bookShelfMenu.setSelection(this.group);
    }

    public void clearBookshelf() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof BookListFragment) {
                ((BookListFragment) fragment).clearBookShelf();
            } else if (fragment instanceof AudioBookFragment) {
                ((AudioBookFragment) fragment).clearBookShelf();
            }
        }
    }

    public void upLayoutType(boolean viewIsList) {
        for (Fragment fragment : fragments) {
            if (fragment instanceof BookListFragment) {
                ((BookListFragment) fragment).updateLayoutType(viewIsList);
            }
        }
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
        Fragment from = fragments[this.group];
        Fragment to = fragments[group];
        if (from != null) {
            transaction.hide(from);
        }

        if (to == null) {
            to = group == Constant.GROUP_AUDIO ? AudioBookFragment.newInstance() : BookListFragment.newInstance(group);
            fragments[group] = to;
            if (to instanceof BookListFragment) {
                ((BookListFragment) to).setItemClickListenerTwo(getAdapterListener());
            }
        }

        if (!to.isAdded()) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.book_list_frame, to, getString(BOOK_GROUPS[group]))
                    .show(to)
                    .commitAllowingStateLoss();
        } else if (((BaseFragment) to).isSupportHidden()) {
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
        refreshFragment(fragments[group]);
    }

    @Override
    public void onRestore() {
        for (Fragment fragment : fragments) {
            refreshFragment(fragment);
        }
    }


    @Override
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

    @Override
    public boolean onBackPressed() {
        if (bookShelfMenu.isExpanded()) {
            bookShelfMenu.collapse();
            return true;
        }
        return false;
    }

    @Override
    public void onReselected() {
        Fragment fragment = fragments[group];
        if (fragment != null) {
            scrollTop(fragment);
        }
    }
}
