package com.monke.monkeybook.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.monke.basemvplib.BaseFragment;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.BookShelfHolder;
import com.monke.monkeybook.widget.theme.AppCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChapterDrawerFragment extends BaseFragment {

    @BindView(R.id.content_view)
    View contentView;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.toolbar_tab)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tv_title)
    AppCompatTextView tvTitle;

    SearchView searchView;

    private final BaseChapterListFragment[] fragments = new BaseChapterListFragment[2];

    public static ChapterDrawerFragment newInstance() {
        Bundle args = new Bundle();
        ChapterDrawerFragment fragment = new ChapterDrawerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_tab_chapterlist, container, false);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this, view);
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorMenuText));
//        inflateMenu();

        tabLayout.setupWithViewPager(viewPager);
        fragments[0] = ChapterListFragment.newInstance();
        fragments[1] = BookmarkListFragment.newInstance();
        final String[] titles = new String[2];
        titles[0] = getString(R.string.category);
        titles[1] = getString(R.string.bookmark);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), titles, fragments));

        BookShelfHolder.get().observe(this, bookShelfBean -> {
            if (tvTitle != null) {
                tvTitle.setText(bookShelfBean.getBookInfoBean().getName());
            }
        });
    }

    @Override
    protected void bindEvent() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Fragment fragment = fragments[0];
                    if (fragment instanceof ChapterListFragment) {
                        ((ChapterListFragment) fragment).scrollToTarget();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        BookShelfHolder.get().unsubscribe(this);
        super.onDestroyView();
    }

    private void inflateMenu() {
        toolbar.inflateMenu(R.menu.menu_search_view);
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.search));
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(() -> {
            tabLayout.setVisibility(VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(view -> tabLayout.setVisibility(GONE));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int tab = tabLayout.getSelectedTabPosition();
                fragments[tab].getFilter().filter(newText);
                return false;
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final String[] titles;
        private final Fragment[] fragments;

        private ViewPagerAdapter(@NonNull FragmentManager fm, String[] titles, Fragment[] fragments) {
            super(fm);
            this.titles = titles;
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments == null ? 0 : fragments.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

}
