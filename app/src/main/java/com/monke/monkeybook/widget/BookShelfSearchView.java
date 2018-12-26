package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookShelfSearchView extends LinearLayout {
    @BindView(R.id.recycler_view)
    RecyclerView rvList;
    @BindView(R.id.appBar)
    View appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SearchView searchView;

    private SearchView.SearchAutoComplete searchAutoComplete;

    private BookShelfListAdapter adapter;

    private String query;
    private IQuery iQuery;

    public BookShelfSearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        super.onMeasure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && adapter.getItemCount() != 0) {
            searchView.clearFocus();
        }
    }

    public void applyWindowInsets(Rect insets) {
        appBar.setPadding(0, insets.top, 0, 0);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_search_bookshelf, this, true);
        ButterKnife.bind(this);
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.menu_color_default));
        toolbar.inflateMenu(R.menu.menu_search_view);
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        AppCompat.useCustomIconForSearchView(searchView, getResources().getString(R.string.searchShelfBook));
        searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryBooks(newText);
                return false;
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(context));
        adapter = new BookShelfListAdapter(context, -1, 1);
        rvList.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> {
            ViewParent parent = BookShelfSearchView.this.getParent();
            if (parent instanceof DrawerLayout) {
                ((DrawerLayout) parent).closeDrawers();
            }
        });
    }

    private void queryBooks(String query) {
        if (TextUtils.isEmpty(query)) {
            adapter.clear();
            return;
        }

        this.query = query;

        if (iQuery != null) {
            iQuery.query(query);
        }
    }

    public void setIQuery(IQuery query) {
        this.iQuery = query;
    }

    public void setOnItemClickListener(OnBookItemClickListenerTwo itemClickListenerTwo) {
        adapter.setItemClickListener(itemClickListenerTwo);
    }

    public void clear() {
        searchView.setQuery(null, false);
        adapter.clear();
    }

    public void showQueryBooks(List<BookShelfBean> value) {
        adapter.replaceAll(value);
    }

    public View getSearchAutoComplete(boolean focus) {
        if (focus) {
            searchAutoComplete.requestFocus();
        }
        return searchAutoComplete;
    }

    public void removeBookShelfIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && adapter.getItemCount() != 0) {
            adapter.removeBook(bookShelfBean);
        }
    }

    public void addBookShelfIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null
                && !TextUtils.isEmpty(searchView.getQuery())
                && searchView.getQuery().toString().contains(this.query)) {
            adapter.addBook(bookShelfBean);
        }
    }

    public void updateBookShelfIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && adapter.getItemCount() != 0) {
            adapter.updateBook(bookShelfBean, false);
        }
    }

    public interface IQuery {
        void query(String query);
    }

}
