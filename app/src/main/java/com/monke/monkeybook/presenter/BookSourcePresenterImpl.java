package com.monke.monkeybook.presenter;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.DocumentHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.BookSourceContract;
import com.monke.monkeybook.service.CheckSourceService;
import com.monke.monkeybook.utils.StringUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public class BookSourcePresenterImpl extends BasePresenterImpl<BookSourceContract.View> implements BookSourceContract.Presenter {
    private BookSourceBean delBookSource;
    private Snackbar progressSnackBar;

    @Override
    public void saveData(BookSourceBean bookSourceBean) {
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
            e.onNext(BookSourceManager.getAll());
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void saveData(List<BookSourceBean> bookSourceBeans) {
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            for (int i = 1; i <= bookSourceBeans.size(); i++) {
                bookSourceBeans.get(i - 1).setSerialNumber(i);
            }
            DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(bookSourceBeans);
            e.onNext(BookSourceManager.getAll());
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void initData() {
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            e.onNext(BookSourceManager.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookSourceBean>>() {
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        mView.resetData(bookSourceBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delData(BookSourceBean bookSourceBean) {
        this.delBookSource = bookSourceBean;
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            BookSourceManager.delete(bookSourceBean);
            e.onNext(BookSourceManager.getAll());
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookSourceBean>>() {
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        mView.resetData(bookSourceBeans);
                        mView.getSnackBar(delBookSource.getBookSourceName() + "已删除")
                                .setDuration(BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("恢复", view -> restoreBookSource(delBookSource))
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar("删除失败");
                    }
                });
    }

    @Override
    public void delData(List<BookSourceBean> bookSourceBeans) {
        if (bookSourceBeans == null || bookSourceBeans.isEmpty()) {
            return;
        }

        mView.showLoading("正在删除选中书源");
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            BookSourceManager.deleteAll(bookSourceBeans);
            e.onNext(BookSourceManager.getAll());
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookSourceBean>>() {
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        mView.resetData(bookSourceBeans);
                        mView.dismissHUD();
                        mView.showSnackBar("删除成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.dismissHUD();
                        mView.showSnackBar("删除失败");
                    }
                });
    }

    private void restoreBookSource(BookSourceBean bookSourceBean) {
        Observable.create((ObservableOnSubscribe<List<BookSourceBean>>) e -> {
            BookSourceManager.add(bookSourceBean);
            e.onNext(BookSourceManager.getAll());
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookSourceBean>>() {
                    @Override
                    public void onNext(List<BookSourceBean> bookSourceBeans) {
                        mView.resetData(bookSourceBeans);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void refresh() {
        String query = mView.getQuery();
        if (StringUtils.isNotBlank(query)) {
            String term = "%" + query + "%";
            List<BookSourceBean> sourceBeanList = DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().queryBuilder()
                    .whereOr(BookSourceBeanDao.Properties.BookSourceName.like(term),
                            BookSourceBeanDao.Properties.BookSourceGroup.like(term),
                            BookSourceBeanDao.Properties.BookSourceUrl.like(term))
                    .orderRaw(BookSourceManager.getSort())
                    .list();
            mView.resetData(sourceBeanList);
        } else {
            mView.resetData(BookSourceManager.getAll());
        }
    }

    @Override
    public void importBookSource(File file) {
        DocumentFile documentFile = DocumentFile.fromFile(file);
        String json = DocumentHelper.readString(documentFile);
        if (!isEmpty(json)) {
            mView.showLoading("正在导入书源");
            BookSourceManager.importOne(json)
                    .flatMap(aBoolean -> {
                        if (aBoolean) {
                            return Observable.just(BookSourceManager.getAll());
                        }
                        return Observable.error(new Exception("import source failed"));
                    })
                    .subscribe(getImportObserver());
        } else {
            mView.showSnackBar("文件读取失败");
        }
    }

    @Override
    public void importBookSource(String sourceUrl) {
        mView.showLoading("正在导入书源");
        BookSourceManager.importFromNet(sourceUrl)
                .flatMap(aBoolean -> {
                    if (aBoolean) {
                        return Observable.just(BookSourceManager.getAll());
                    }
                    return Observable.error(new Exception("import source failed"));
                })
                .subscribe(getImportObserver());
    }

    private SimpleObserver<List<BookSourceBean>> getImportObserver() {
        return new SimpleObserver<List<BookSourceBean>>() {
            @Override
            public void onNext(List<BookSourceBean> bookSourceBeans) {
                mView.resetData(bookSourceBeans);
                mView.dismissHUD();
                mView.showSnackBar("书源导入成功");
            }

            @Override
            public void onError(Throwable e) {
                mView.dismissHUD();
                mView.showSnackBar("书源导入失败");
            }
        };
    }

    private String getProgressStr(int state) {
        return String.format(mView.getContext().getString(R.string.check_book_source) + mView.getContext().getString(R.string.progress_show),
                state, BookSourceManager.getCount());
    }

    @Override
    public void checkBookSource() {
        CheckSourceService.start(mView.getContext());
    }

    /////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    /////////////////////RxBus////////////////////////

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHECK_SOURCE_STATE)})
    public void upCheckSourceState(Integer state) {
        refresh();

        if (state == -1) {
            mView.showSnackBar("校验完成");
        } else {
            if (progressSnackBar == null) {
                progressSnackBar = mView.getSnackBar(getProgressStr(state)).setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
                progressSnackBar.setAction(mView.getContext().getString(R.string.cancel), view -> CheckSourceService.stop(mView.getContext()));
            } else {
                progressSnackBar.setText(getProgressStr(state));
            }
            if (!progressSnackBar.isShown()) {
                progressSnackBar.show();
            }
        }
    }
}
