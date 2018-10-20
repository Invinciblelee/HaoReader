package com.monke.monkeybook.presenter;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.presenter.contract.WelcomeContract;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class WelcomePresenterImpl extends BasePresenterImpl<WelcomeContract.View> implements WelcomeContract.Presenter {


    @Override
    public void initData(Activity activity) {
        if(activity.getIntent().getData() != null){
            mView.onFromOtherCreate();
        }else {
            mView.onNormalCreate();
        }
    }

    /**
     * APP外部打开
     */
    @Override
    public void openBookFromUri(Activity activity) {
        Uri uri = activity.getIntent().getData();
        long start = System.currentTimeMillis();
        Observable.create((ObservableOnSubscribe<String>) e -> {
            e.onNext(FileUtil.getFilePathFromUri(activity, uri));
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(File::new)
                .flatMap((Function<File, ObservableSource<LocBookShelfBean>>) file -> ImportBookModelImpl.getInstance().importBook(file))
                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                    @Override
                    public void onNext(LocBookShelfBean locBookShelfBean) {
                        boolean inBookShelf = !locBookShelfBean.getNew();
                        mView.startReadBook(locBookShelfBean.getBookShelfBean(), inBookShelf, 1000L - (System.currentTimeMillis() - start));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MApplication.getInstance(), "文本打开失败！", Toast.LENGTH_SHORT).show();
                        mView.finish();
                    }
                });
    }

    @Override
    public void detachView() {

    }
}
