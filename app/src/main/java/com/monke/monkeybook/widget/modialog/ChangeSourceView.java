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
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.SearchBookBeanDao;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterHelp;
import com.monke.monkeybook.model.SearchBookModel;
import com.monke.monkeybook.utils.ListUtil;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.view.activity.BookInfoActivity;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;
import com.monke.monkeybook.widget.refreshview.RefreshRecyclerView;

import java.util.ArrayList;
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

public class ChangeSourceView {
    private TextView atvTitle;
    private ImageButton ibtStop;
    private RefreshRecyclerView rvSource;

    private MoDialogView moDialogView;
    private OnClickSource onClickSource;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookInfoBean book;
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
        searchBookModel = new SearchBookModel(activity, new SearchBookModel.OnSearchListener() {

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
            public boolean checkExists(SearchBookBean searchBook) {
                List<SearchBookBean> searchBooks = adapter.getSearchBookBeans();
                for (SearchBookBean temp : searchBooks) {
                    if (TextUtils.equals(temp.getTag(), searchBook.getTag())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                addSearchBook(ListUtil.filter(value, searchBookBean -> ChangeSourceView.this.test(searchBookBean, book)));
            }

            @Override
            public void searchBookError() {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(false);
            }

            @Override
            public int getItemCount() {
                return adapter.getICount();
            }
        }, !TextUtils.equals(ACache.get(activity).getAsString("useMy716"), "False"));

        moDialogView.setOnDismissListener(() -> searchBookModel.shutdownSearch());
    }

    void showChangeSource(BookShelfBean bookShelf, OnClickSource onClickSource) {
        this.onClickSource = onClickSource;
        book = bookShelf.getBookInfoBean();
        if (TextUtils.isEmpty(book.getAuthor())) {
            atvTitle.setText(book.getName());
        } else {
            atvTitle.setText(String.format("%s(%s)", book.getName(), book.getAuthor()));
        }
        rvSource.startRefresh();
        moDialogView.post(this::getSearchBookInDb);
    }

    private void selectSource(SearchBookBean searchBook) {
        moDialogView.getMoDialogHUD().dismiss();
        if (!selectCover) {
            if (!searchBook.getIsCurrentSource()) {
                onClickSource.changeSource(searchBook);
                saveInfoBySelection(searchBook);
            }
        } else {
            onClickSource.changeSource(searchBook);
        }
    }

    private void getSearchBookInDb() {
        Observable.create((ObservableOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(book.getName()), SearchBookBeanDao.Properties.Author.eq(book.getAuthor())).list();
            e.onNext(searchBookBeans == null ? new ArrayList<>() : searchBookBeans);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.size() > 0) {
                            for (SearchBookBean searchBookBean : searchBookBeans) {
                                if (Objects.equals(searchBookBean.getTag(), book.getTag())) {
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
        int id = (int) System.currentTimeMillis();
        searchBookModel.startSearch(id, book.getName());
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            for (SearchBookBean searchBookBean : value) {
                if (test(searchBookBean, book)) {
                    if (TextUtils.equals(searchBookBean.getTag(), book.getTag())) {
                        searchBookBean.setIsCurrentSource(true);
                    } else {
                        searchBookBean.setIsCurrentSource(false);
                    }
                    adapter.addSourceAdapter(searchBookBean);

                    saveInfoIfChanged(searchBookBean);
                    break;
                }
            }
        }
    }

    private void saveInfoIfChanged(SearchBookBean searchBookBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getInstance().getmDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);

            BookSourceBean bookSourceBean = BookshelfHelp.getBookSourceByTag(searchBookBean.getTag());
            if (book.getChapterList().size() > 0 && bookSourceBean != null) {
                int lastChapter = ChapterHelp.guessChapterNum(searchBookBean.getLastChapter());
                if (lastChapter > book.getChapterList().size()) {
                    bookSourceBean.increaseWeight(100);
                    BookshelfHelp.saveBookSource(bookSourceBean);
                }
            }
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean bool) {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void saveInfoBySelection(SearchBookBean searchBook) {
        Observable.create((ObservableOnSubscribe<SearchBookBean>) e -> {
            BookSourceBean sourceBean = BookshelfHelp.getBookSourceByTag(searchBook.getTag());
            if (sourceBean != null) {
                sourceBean.increaseWeightBySelection();
                BookshelfHelp.saveBookSource(sourceBean);
            }
            e.onNext(searchBook);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<SearchBookBean>() {
                    @Override
                    public void onNext(SearchBookBean searchBookBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
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
        ibtStop.setOnClickListener(v -> searchBookModel.stopSearch(true));
    }

    /**
     * 换源确定
     */
    public interface OnClickSource {
        void changeSource(SearchBookBean searchBookBean);
    }
}
