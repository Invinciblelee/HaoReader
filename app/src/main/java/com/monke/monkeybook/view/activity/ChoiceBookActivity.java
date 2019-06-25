//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.view.fragment.ChoiceBookFragment;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChoiceBookActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private FindKindGroupBean groupBean;

    public static void startThis(MBaseActivity activity, FindKindGroupBean groupBean) {
        Intent intent = new Intent(activity, ChoiceBookActivity.class)
                .putExtra("group", groupBean);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_choice);
    }

    @Override
    protected void initData() {
        groupBean = getIntent().getParcelableExtra("group");
        if (groupBean == null) {
            finish();
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        if (groupBean != null) {
            viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), groupBean.getChildren()));
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    //设置ToolBar
    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(groupBean == null ? null : groupBean.getGroupName());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choice_book_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static class PagerAdapter extends FragmentStatePagerAdapter {

        private List<FindKindBean> kindBeans;

        PagerAdapter(@NonNull FragmentManager fm, List<FindKindBean> findKindBeans) {
            super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.kindBeans = findKindBeans;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            FindKindBean kindBean = kindBeans.get(position);
            return ChoiceBookFragment.newInstance(kindBean.getTag(), kindBean.getKindUrl());
        }

        @Override
        public int getCount() {
            return kindBeans == null ? 0 : kindBeans.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return kindBeans.get(position).getKindName();
        }
    }
}