//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.view.fragment.ChoiceBookFragment;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChoiceBookActivity extends MBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.view_choice_book_category_overlay)
    View categoryOverlay;
    @BindView(R.id.flex_box_book_category)
    GridView categoryBox;

    private FindKindGroupBean groupBean;

    private CategoryAdapter categoryAdapter;

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

            addToCategoryView(groupBean.getChildren());

            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    setCategorySelected(position);
                }
            });
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
            case R.id.action_more:
                toggleCategoryPop();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (categoryOverlay.isShown()) {
            toggleCategoryPop();
            return;
        }
        finish();
    }

    @OnClick(R.id.view_masking)
    public void onClick(View view) {
        toggleCategoryPop();
    }

    private void addToCategoryView(List<FindKindBean> kindBeans) {
        if (kindBeans == null || kindBeans.isEmpty()) return;
        categoryAdapter = new CategoryAdapter(this, kindBeans);
        categoryBox.setAdapter(categoryAdapter);

        categoryBox.setOnItemClickListener((parent, view, position, id) -> {
            viewPager.setCurrentItem(position);
            toggleCategoryPop();
        });
    }

    private void setCategorySelected(int position) {
        if (categoryAdapter != null) {
            categoryAdapter.setSelected(position);
        }
    }

    private void toggleCategoryPop() {
        final boolean pendingShow = !categoryOverlay.isShown();

        if (pendingShow) {
            categoryOverlay.setVisibility(View.VISIBLE);
        }

        float start = pendingShow ? -categoryBox.getHeight() : 0;
        float end = pendingShow ? 0 : -categoryBox.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(categoryBox, "translationY", start, end);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!pendingShow) {
                    categoryOverlay.setVisibility(View.INVISIBLE);
                }
            }
        });
        animator.start();
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

    private static class CategoryAdapter extends BaseAdapter {

        private final List<FindKindBean> findKindBeans;

        private final Context context;

        private int lastPosition;

        CategoryAdapter(Context context, List<FindKindBean> findKindBeans) {
            this.context = context;
            this.findKindBeans = findKindBeans;
        }

        @Override
        public int getCount() {
            return findKindBeans == null ? 0 : findKindBeans.size();
        }

        @Override
        public FindKindBean getItem(int position) {
            return findKindBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_choice_book_category, null);
            }
            TextView tvText = convertView.findViewById(R.id.tv_text);
            tvText.setText(getItem(position).getKindName());
            GradientDrawable drawable = (GradientDrawable) tvText.getBackground();
            if (lastPosition == position) {
                int selectedColor = ContextCompat.getColor(context, R.color.colorBarText);
                tvText.setTextColor(selectedColor);
                drawable.setStroke(DensityUtil.dp2px(context, 2), selectedColor);
            } else {
                int normalColor = ContextCompat.getColor(context, R.color.white_translucent);
                tvText.setTextColor(normalColor);
                drawable.setStroke(DensityUtil.dp2px(context, 2), normalColor);
            }
            return convertView;
        }

        void setSelected(int position) {
            if (lastPosition != position) {
                lastPosition = position;
                notifyDataSetChanged();
            }
        }
    }
}