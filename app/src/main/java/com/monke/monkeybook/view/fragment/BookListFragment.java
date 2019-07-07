package com.monke.monkeybook.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.presenter.BookListPresenterImpl;
import com.monke.monkeybook.presenter.contract.BookListContract;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.adapter.BookShelfGridAdapter;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.BaseBookListAdapter;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookListFragment extends BaseFragment<BookListContract.Presenter> implements BookListContract.View {

    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;

    private Unbinder unbinder;

    private BaseBookListAdapter<?> bookListAdapter;
    private OnBookItemClickListenerTwo itemClickListenerTwo;
    private ItemTouchHelper itemTouchHelper;
    private MyItemTouchHelpCallback itemTouchHelpCallback;


    private boolean isRecreate;

    public static BookListFragment newInstance(int group) {
        Bundle args = new Bundle();
        args.putInt("group", group);
        BookListFragment fragment = new BookListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRecreate = savedInstanceState != null;
    }

    @Override
    protected BookListContract.Presenter initInjector() {
        return new BookListPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_book_list, container, false);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected void initData() {
        mPresenter.initData(this);
    }

    @Override
    protected void firstRequest() {
        mPresenter.queryBookShelf(false);
    }

    @Override
    protected void bindView() {
        unbinder = ButterKnife.bind(this, view);

        rvBookshelf.setHasFixedSize(true);

        int bookPx = mPresenter.getBookshelfPx();
        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        if (mPresenter.viewIsList()) {
            rvBookshelf.setPadding(0, padding, 0, padding);
            bookListAdapter = new BookShelfListAdapter(getContext(), mPresenter.getGroup(), bookPx);
            rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            rvBookshelf.setPadding(padding, padding, padding, padding);
            bookListAdapter = new BookShelfGridAdapter(getContext(), mPresenter.getGroup(), bookPx);
            rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }

        if (itemClickListenerTwo != null) {
            bookListAdapter.setItemClickListener(itemClickListenerTwo);
        }

        updateBookPx(bookPx);

        rvBookshelf.setAdapter(bookListAdapter);
    }

    @Override
    public List<BookShelfBean> getShowingBooks() {
        return bookListAdapter == null ? null : bookListAdapter.getBooks();
    }

    @Override
    public void refreshBookShelf(boolean update) {
        updateLayoutType(mPresenter.viewIsList());
        mPresenter.queryBookShelf(update);
    }

    @Override
    public void addAllBookShelf(List<BookShelfBean> bookShelfBeanList) {
        boolean isEmptyBefore = bookListAdapter.getItemCount() == 0;

        bookListAdapter.replaceAll(bookShelfBeanList);

        if (!isEmptyBefore) {
            rvBookshelf.scrollToPosition(0);
        } else {
            startLayoutAnimation();
        }
    }

    @Override
    public void clearBookShelf() {
        bookListAdapter.clear();
    }

    @Override
    public void startLayoutAnimation() {
        if (mPresenter.getNeedAnim()) {
            if (rvBookshelf.getLayoutAnimation() == null) {
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.anim_bookshelf_layout);
                rvBookshelf.setLayoutAnimation(animation);
            } else {
                rvBookshelf.startLayoutAnimation();
            }
        } else {
            if (rvBookshelf.getLayoutAnimation() != null) {
                rvBookshelf.setLayoutAnimation(null);
            }
        }
    }


    @Override
    public void updateBook(BookShelfBean bookShelfBean, boolean sort) {
        bookListAdapter.updateBook(bookShelfBean, sort);
    }

    @Override
    public void addBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.addBook(bookShelfBean);
    }

    @Override
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.removeBook(bookShelfBean);
    }

    @Override
    public void sortBookShelf() {
        bookListAdapter.sort();
    }

    @Override
    public void updateBookPx(int bookPx) {
        if (bookPx == 2) {
            if (itemTouchHelper == null) {
                itemTouchHelpCallback = new MyItemTouchHelpCallback();
                itemTouchHelpCallback.setDragEnable(true);
                itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            }
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookListAdapter.getItemTouchCallbackListener());
        } else if (itemTouchHelper != null) {
            itemTouchHelper.attachToRecyclerView(null);
            itemTouchHelpCallback.setOnItemTouchCallbackListener(null);
        }
        bookListAdapter.setBookshelfPx(bookPx);
    }

    @Override
    public void updateLayoutType(boolean viewIsList) {
        final List<BookShelfBean> books = bookListAdapter.getBooks();

        int bookPx = mPresenter.getBookshelfPx();
        int padding = getResources().getDimensionPixelSize(R.dimen.half_card_item_margin);
        if (viewIsList) {
            rvBookshelf.setPadding(0, padding, 0, padding);
            bookListAdapter = new BookShelfListAdapter(getContext(), mPresenter.getGroup(), bookPx);
            rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            rvBookshelf.setPadding(padding, padding, padding, padding);
            bookListAdapter = new BookShelfGridAdapter(getContext(), mPresenter.getGroup(), bookPx);
            rvBookshelf.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }

        if (itemClickListenerTwo != null) {
            bookListAdapter.setItemClickListener(itemClickListenerTwo);
        }

        updateBookPx(bookPx);

        rvBookshelf.setAdapter(bookListAdapter);
        bookListAdapter.replaceAll(books);
    }

    @Override
    public SharedPreferences getPreferences() {
        return AppConfigHelper.get().getPreferences();
    }

    @Override
    public void refreshError(String error) {
        toast(error);
    }

    @Override
    public void toast(String msg) {
        ToastUtils.toast(Objects.requireNonNull(getContext()), msg);
    }

    @Override
    public boolean isRecreate() {
        return isRecreate;
    }

    //*****************************************************************************************************************************************//

    void setItemClickListenerTwo(OnBookItemClickListenerTwo itemClickListenerTwo) {
        this.itemClickListenerTwo = itemClickListenerTwo;
        if (bookListAdapter != null) {
            bookListAdapter.setItemClickListener(this.itemClickListenerTwo);
        }
    }

    void scrollToTop(){
        rvBookshelf.scrollToPosition(0);
    }
}
