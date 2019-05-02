package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.monke.basemvplib.BaseActivity;
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
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.BookInfoActivity;
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

public class ChangeSourceView implements SearchBookModel.SearchListener {
    private TextView atvTitle;
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;

    private MoDialogView moDialogView;
    private OnClickSource onClickSource;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookInfoBean bookInfo;
    private boolean selectCover;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable searchTask = new Runnable() {
        @Override
        public void run() {
            rvSource.startRefresh(false);
            searchBookModel.startSearch(bookInfo.getName());
        }
    };

    public static ChangeSourceView newInstance(BaseActivity activity, MoDialogView moDialogView) {
        return new ChangeSourceView(activity, moDialogView);
    }

    private ChangeSourceView(BaseActivity activity, MoDialogView moDialogView) {
        this.moDialogView = moDialogView;
        this.context = moDialogView.getContext();
        this.selectCover = activity.getClass().equals(BookInfoActivity.class);
        bindView();
        adapter = new ChangeSourceAdapter(context, selectCover);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setOnItemClickListener((view, item) -> selectSource(item));
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_searchbook_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> {
            //刷新失败 ，重试
            reSearchBook();
        });
        rvSource.setNoDataAndrRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_searchbook_no_data, null),
                viewRefreshError);

        moDialogView.setOnDismissListener(() -> searchBookModel.shutdownSearch());
    }

    @Override
    public void searchSourceEmpty() {
        ToastUtils.toast(context, "没有选中任何书源");
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(true, false);
    }

    @Override
    public void resetSearchBook() {
        ibtStop.setVisibility(View.VISIBLE);
        adapter.reSetSourceAdapter();
    }

    @Override
    public void searchBookFinish() {
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(true, false);
    }

    @Override
    public void loadMoreSearchBook(List<SearchBookBean> value) {
        ListUtils.filter(value, searchBookBean -> searchBookBean.isSimilarTo(bookInfo));
        addSearchBook(value);
    }

    @Override
    public void searchBookError() {
        ibtStop.setVisibility(View.INVISIBLE);
        rvSource.finishRefresh(false);
    }

    void showChangeSource(BookInfoBean bookInfoBean, OnClickSource onClickSource) {
        this.onClickSource = onClickSource;
        this.bookInfo = bookInfoBean;
        searchBookModel = new SearchBookModel(context)
                .onlyOnePage()
                .setSearchBookType(bookInfo.getBookType())
                .listener(this)
                .setup();
        if (TextUtils.isEmpty(bookInfo.getAuthor())) {
            atvTitle.setText(bookInfo.getName());
        } else {
            atvTitle.setText(String.format("%s(%s)", bookInfo.getName(), bookInfo.getAuthor()));
        }
        getSearchBookInDb();
        rvSource.startRefresh(false);
    }

    private void selectSource(SearchBookBean searchBook) {
        moDialogView.getMoDialogHUD().dismiss();
        moDialogView.post(() -> {
            if (selectCover) {
                onClickSource.changeSource(searchBook);
            } else {
                if (!isCurrent(searchBook)) {
                    onClickSource.changeSource(searchBook);
                    incrementSourceWeightBySelection(searchBook);
                }
            }
        });
    }

    private void getSearchBookInDb() {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.BookType.eq(bookInfo.getBookType()),
                            SearchBookBeanDao.Properties.Name.eq(bookInfo.getName()),
                            SearchBookBeanDao.Properties.Author.eq(bookInfo.getAuthor())).list();
            e.onNext(searchBookBeans);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
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
            BookSourceBean sourceBean = BookSourceManager.getInstance().getBookSourceByTag(searchBook.getTag());
            if (sourceBean != null) {
                sourceBean.increaseWeightBySelection();
                BookSourceManager.getInstance().saveBookSource(sourceBean);
            }
        });
    }

    private boolean isCurrent(SearchBookBean searchBookBean) {
        return TextUtils.equals(searchBookBean.getRealNoteUrl(), bookInfo.getNoteUrl())
                && TextUtils.equals(searchBookBean.getTag(), bookInfo.getTag());
    }

    private void bindView() {
        moDialogView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moDialogView, true);

        View llContent = moDialogView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        atvTitle = moDialogView.findViewById(R.id.atv_title);
        ibtStop = moDialogView.findViewById(R.id.ibt_stop);
        rvSource = moDialogView.findViewById(R.id.rf_rv_change_source);
        ibtStop.setVisibility(View.INVISIBLE);

        rvSource.setOnRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> searchBookModel.stopSearch());
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }
}
