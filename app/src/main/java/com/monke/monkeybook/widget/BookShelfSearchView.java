package com.monke.monkeybook.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnItemClickListenerTwo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookShelfSearchView extends LinearLayout {
    @BindView(R.id.recycler_view)
    RecyclerView rvList;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SearchView searchView;

    private SearchView.SearchAutoComplete searchAutoComplete;

    private BookShelfListAdapter adapter;

    public BookShelfSearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
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

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_search_bookshelf, this, true);
        ButterKnife.bind(this);
        assert toolbar.getNavigationIcon() != null;
        toolbar.getNavigationIcon().mutate();
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
        toolbar.inflateMenu(R.menu.menu_search_view);
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search_bar);
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
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookShelfListAdapter((Activity) getContext());
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

        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) e -> {
            e.onNext(BookshelfHelp.queryBooks(query));
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (!value.isEmpty()) {
                            adapter.replaceAll(value, "1");
                        } else {
                            adapter.clear();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public void addToBookShelfIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && adapter.getItemCount() != 0) {
            adapter.addBook(bookShelfBean);
        }
    }

    public void removeFromBookShelfIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && adapter.getItemCount() != 0) {
            adapter.removeBook(bookShelfBean);
        }
    }

    public void updateBookIfNeed(BookShelfBean bookShelfBean) {
        if (bookShelfBean != null && adapter.getItemCount() != 0) {
            adapter.updateBook(bookShelfBean, false);
        }
    }

    public void setupItemClickListener(OnItemClickListenerTwo itemClickListenerTwo) {
        adapter.setItemClickListener(itemClickListenerTwo);
    }

    public List<BookShelfBean> getBooks() {
        return adapter.getBooks();
    }

    public View getSearchAutoComplete(boolean focus) {
        if (focus) {
            searchAutoComplete.requestFocus();
        }
        return searchAutoComplete;
    }
}
