package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.basemvplib.BaseActivity;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.utils.ListUtil;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.activity.BookInfoActivity;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.List;
import java.util.Objects;

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

        searchBookModel = new SearchBookModel(activity, !TextUtils.equals(ACache.get(activity).getAsString("useMy716"), "False"), this);
        searchBookModel.onlyOnePage();
    }


    @Override
    public void searchSourceEmpty() {
        Toast.makeText(context, "没有选中任何书源", Toast.LENGTH_SHORT).show();
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
        if (TextUtils.isEmpty(bookInfo.getAuthor())) {
            atvTitle.setText(bookInfo.getName());
        } else {
            atvTitle.setText(String.format("%s(%s)", bookInfo.getName(), bookInfo.getAuthor()));
        }
        rvSource.startRefresh();
        moDialogView.post(this::getSearchBookInDb);
    }

    private void selectSource(SearchBookBean searchBook) {
        moDialogView.getMoDialogHUD().dismiss();
        if (!selectCover) {
            if (!searchBook.getIsCurrentSource()) {
                onClickSource.changeSource(searchBook);
                incrementSourceWeightBySelection(searchBook);
            }
        } else {
            onClickSource.changeSource(searchBook);
        }
    }

    private void getSearchBookInDb() {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookInfo.getName()), SearchBookBeanDao.Properties.Author.eq(bookInfo.getAuthor())).list();
            e.onNext(ListUtil.removeDuplicate(searchBookBeans, (o1, o2) -> o1.getTag().compareTo(o2.getTag())));
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        if (!searchBookBeans.isEmpty()) {
                            for (SearchBookBean searchBookBean : searchBookBeans) {
                                if (Objects.equals(searchBookBean.getTag(), bookInfo.getTag())) {
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
        rvSource.startRefresh();
        DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().deleteInTx(adapter.getSearchBookBeans());
        adapter.reSetSourceAdapter();
        searchBookModel.startSearch(bookInfo.getName());
    }

    private synchronized void addSearchBook(List<SearchBookBean> searchBookBeans) {
        final List<SearchBookBean> newDataS = ListUtil.filter(searchBookBeans, searchBookBean -> ChangeSourceView.this.test(searchBookBean, bookInfo));
        if (!newDataS.isEmpty()) {
            for (SearchBookBean searchBookBean : newDataS) {
                if (TextUtils.equals(searchBookBean.getTag(), bookInfo.getTag())) {
                    searchBookBean.setIsCurrentSource(true);
                } else {
                    searchBookBean.setIsCurrentSource(false);
                }
            }
            rvSource.post(() -> adapter.addAllSourceAdapter(newDataS));
        }
    }

    private void incrementSourceWeightBySelection(SearchBookBean searchBook) {
        Schedulers.single().createWorker().schedule(() -> {
            BookSourceBean sourceBean = BookshelfHelp.getBookSourceByTag(searchBook.getTag());
            if (sourceBean != null) {
                sourceBean.increaseWeightBySelection();
                BookshelfHelp.saveBookSource(sourceBean);
            }
        });
    }

    private boolean test(SearchBookBean searchBookBean, BookInfoBean book) {
        return TextUtils.equals(searchBookBean.getName(), book.getName())
                && TextUtils.equals(searchBookBean.getAuthor(), book.getAuthor());
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

        rvSource.setBaseRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> searchBookModel.stopSearch());
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }
}
