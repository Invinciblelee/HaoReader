package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.monke.basemvplib.NetworkUtil;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.utils.ListUtils;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceDialog extends AppCompatDialog implements SearchBookModel.SearchListener {
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;

    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookInfoBean bookInfo;
    private boolean selectCover;
    private OnClickSource onClickSource;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable searchTask = new Runnable() {
        @Override
        public void run() {
            if (rvSource != null) {
                rvSource.startRefresh(false);
                searchBookModel.startSearch(bookInfo.getName());
            }
        }
    };

    public static void show(FragmentManager fragmentManager, BookInfoBean bookInfoBean, boolean selectCover, OnClickSource onClickSource) {
        ChangeSourceDialog dialog = new ChangeSourceDialog();
        Bundle args = new Bundle();
        args.putBoolean("selectCover", selectCover);
        args.putParcelable("bookInfo", bookInfoBean);
        dialog.setArguments(args);
        dialog.onClickSource = onClickSource;
        dialog.show(fragmentManager, "changeSource");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        selectCover = args.getBoolean("selectCover");
        bookInfo = args.getParcelable("bookInfo");

        searchBookModel = new SearchBookModel(requireContext())
                .onlyOnePage()
                .useMy716(true)
                .useShuqi(true)
                .setSearchBookType(selectCover ? null : bookInfo.getBookType())
                .listener(this)
                .setup();
    }

    @Override
    public View onCreateDialogContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_source, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView atvTitle = findViewById(R.id.atv_title);
        ibtStop = findViewById(R.id.ibt_stop);
        rvSource = findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.setOnRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> searchBookModel.stopSearch());

        adapter = new ChangeSourceAdapter(getContext(), selectCover);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(getContext()));
        adapter.setOnItemClickListener((v, item) -> selectSource(item));
        View viewRefreshError = LayoutInflater.from(getContext()).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndrRefreshErrorView(LayoutInflater.from(getContext()).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        if (TextUtils.isEmpty(bookInfo.getAuthor())) {
            atvTitle.setText(bookInfo.getName());
        } else {
            atvTitle.setText(String.format("%s(%s)", bookInfo.getName(), bookInfo.getAuthor()));
        }

        view.post(() -> {
            getSearchBookInDb();
            rvSource.startRefresh(false);
        });
    }

    @Override
    protected void onDialogAttachWindow(@NonNull Window window) {
        window.setGravity(Gravity.CENTER);
        int height = getResources().getDisplayMetrics().heightPixels - ScreenUtils.getStatusBarHeight();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, height);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchBookModel.shutdownSearch();
    }

    @Override
    public void searchSourceEmpty() {
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(true, false);
        rvSource.setEnabled(true);
    }

    @Override
    public void searchBookReset() {
        ibtStop.setVisibility(View.VISIBLE);
        adapter.reSetSourceAdapter();
        rvSource.setEnabled(false);
    }

    @Override
    public void searchBookFinish() {
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(true, false);
        rvSource.setEnabled(true);
    }

    @Override
    public void loadMoreSearchBook(List<SearchBookBean> value) {
        ListUtils.filter(value, searchBookBean -> searchBookBean.isSimilarTo(bookInfo, selectCover));
        addSearchBook(value);
    }

    @Override
    public void searchBookError() {
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(false);
        rvSource.setEnabled(true);
    }

    private void selectSource(SearchBookBean searchBook) {
        dismissAllowingStateLoss();
        if (selectCover) {
            if (onClickSource != null) {
                onClickSource.changeSource(searchBook);
            }
        } else {
            if (!isCurrent(searchBook)) {
                incrementSourceWeightBySelection(searchBook);
                if (onClickSource != null) {
                    onClickSource.changeSource(searchBook);
                }
            }
        }
    }

    private void getSearchBookInDb() {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.BookType.eq(bookInfo.getBookType()),
                            SearchBookBeanDao.Properties.Name.eq(bookInfo.getName()),
                            SearchBookBeanDao.Properties.Author.eq(bookInfo.getAuthor())).list();
            e.onNext(searchBookBeans);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        if (!searchBookBeans.isEmpty()) {
                            for (SearchBookBean searchBookBean : searchBookBeans) {
                                if (isCurrent(searchBookBean)) {
                                    searchBookBean.setIsCurrentSource(true);
                                } else {
                                    searchBookBean.setIsCurrentSource(false);
                                }
                            }
                            adapter.addAllSourceAdapter(searchBookBeans);
                            rvSource.finishRefresh(true, true);
                        } else {
                            if (NetworkUtil.isNetworkAvailable()) {
                                reSearchBook();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                    }
                });
    }

    private void reSearchBook() {
        DbHelper.getInstance().getDaoSession().getSearchBookBeanDao().deleteInTx(adapter.getSearchBookBeans());
        adapter.reSetSourceAdapter();
        handler.removeCallbacks(searchTask);
        handler.post(searchTask);
    }

    private synchronized void addSearchBook(List<SearchBookBean> searchBookBeans) {
        if (!searchBookBeans.isEmpty()) {
            for (SearchBookBean searchBookBean : searchBookBeans) {
                if (isCurrent(searchBookBean)) {
                    searchBookBean.setIsCurrentSource(true);
                } else {
                    searchBookBean.setIsCurrentSource(false);
                }
            }
            handler.post(() -> adapter.addAllSourceAdapter(searchBookBeans));
        }
    }

    private void incrementSourceWeightBySelection(SearchBookBean searchBook) {
        Schedulers.single().createWorker().schedule(() -> {
            BookSourceBean sourceBean = BookSourceManager.getByUrl(searchBook.getTag());
            if (sourceBean != null) {
                sourceBean.increaseWeightBySelection();
                BookSourceManager.save(sourceBean);
            }
        });
    }

    private boolean isCurrent(SearchBookBean searchBookBean) {
        return TextUtils.equals(searchBookBean.getRealNoteUrl(), bookInfo.getNoteUrl())
                && TextUtils.equals(searchBookBean.getTag(), bookInfo.getTag());
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }
}
