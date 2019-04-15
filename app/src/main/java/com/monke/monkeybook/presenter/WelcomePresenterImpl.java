package com.monke.monkeybook.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.model.ImportBookModelImpl;
import com.monke.monkeybook.presenter.contract.WelcomeContract;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.RxUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class WelcomePresenterImpl extends BasePresenterImpl<WelcomeContract.View> implements WelcomeContract.Presenter {

    private static final long START_DELAY = 800;

    @Override
    public void initData(Activity activity) {
        final Intent intent = activity.getIntent();
        if (intent.getData() != null) {
            mView.onStartFromUri();
        } else {
            // 避免从桌面启动程序后，会重新实例化入口类的activity
            if (!activity.isTaskRoot()) {
                final String intentAction = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                    mView.finish();
                    return;
                }
            }

            if (mView.getPreferences().getBoolean(mView.getContext().getString(R.string.pk_default_read), false)) {
                openBookFromRecent();
            } else {
                mView.onStartNormal(START_DELAY);
            }
        }
    }

    @Override
    public void openBookFromRecent() {
        long start = System.currentTimeMillis();
        Single.create((SingleOnSubscribe<BookShelfBean>) e -> {
            BookShelfBean bookShelfBean = null;
            String noteUrl = ReadBookControl.getInstance().getLastNoteUrl();
            if (!TextUtils.isEmpty(noteUrl)) {
                bookShelfBean = BookshelfHelp.queryBookByUrl(noteUrl);
            }
            e.onSuccess(bookShelfBean == null ? new BookShelfBean() : bookShelfBean);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookShelfBean bookShelfBean) {
                        long delay = START_DELAY - (System.currentTimeMillis() - start);
                        if (!TextUtils.isEmpty(bookShelfBean.getNoteUrl())) {
                            mView.startReadBookAct(bookShelfBean, true, false, delay);
                        } else {
                            mView.onStartNormal(delay);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        long delay = START_DELAY - (System.currentTimeMillis() - start);
                        mView.onStartNormal(delay);
                    }
                });
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
                        mView.startReadBookAct(locBookShelfBean.getBookShelfBean(), inBookShelf, true, START_DELAY - (System.currentTimeMillis() - start));
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("文本打开失败");
                        mView.finish();
                    }
                });
    }

    @Override
    public void detachView() {

    }
}
