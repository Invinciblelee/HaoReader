package com.monke.monkeybook.presenter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.SourceEditContract;
import com.monke.monkeybook.utils.MD5Utils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public class SourceEditPresenterImpl extends BasePresenterImpl<SourceEditContract.View> implements SourceEditContract.Presenter {

    @Override
    public void saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld, boolean debug) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (bookSourceOld != null && !Objects.equals(bookSource.getBookSourceUrl(), bookSourceOld.getBookSourceUrl())) {
                BookSourceManager.delete(bookSourceOld);
            }
            BookSourceManager.add(bookSource);
            e.onNext(true);
        }).subscribeOn(RxExecutors.getDefault())
                .doAfterNext(aBoolean -> {
                    if (aBoolean) {
                        try {
                            ACache cache = ACache.get(mView.getContext());
                            cache.remove(bookSource.getBookSourceUrl());
                            cache.remove(MD5Utils.strToMd5By16(bookSource.getBookSourceUrl()));
                        } catch (Exception ignore) {
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (debug) {
                            mView.toDebug(bookSource);
                        } else {
                            mView.saveSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("书源保存失败");
                    }
                });
    }

    @Override
    public void copySource(BookSourceBean bookSourceBean) {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, mView.getBookSourceStr());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            mView.toast("拷贝成功");
        }
    }

    @Override
    public void pasteSource() {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clipData != null && clipData.getItemCount() > 0) {
            setText(String.valueOf(clipData.getItemAt(0).getText()));
        }
    }

    @Override
    public void setText(String bookSourceStr) {
        Gson gson = new Gson();
        try {
            BookSourceBean bookSourceBean = gson.fromJson(bookSourceStr, BookSourceBean.class);
            mView.setText(bookSourceBean);
        } catch (Exception ignore) {
            mView.toast("数据格式不对");
        }
    }

    @SuppressLint("SetWorldReadable")
    @Override
    public void handleSourceShare() {
        Single.create((SingleOnSubscribe<File>) emitter -> {
            Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(mView.getBookSourceStr(), 800);
            if (bitmap != null) {
                File file = new File(mView.getContext().getExternalCacheDir(), "bookSource.png");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                emitter.onSuccess(file);
            } else if (StringUtils.isNotBlank(mView.getBookSourceName())) {
                File file = new File(mView.getContext().getExternalCacheDir(), mView.getBookSourceName() + ".txt");
                FileOutputStream fOut = new FileOutputStream(file);
                fOut.write(mView.getBookSourceStr().getBytes());
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                emitter.onSuccess(file);
            } else {
                emitter.onError(new IllegalArgumentException("can not generate share file"));
            }
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mView.shareSource(file, file.getName().endsWith(".txt") ? "text/plain" : "image/png");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("分享失败");
                    }
                });
    }

}
