package com.monke.monkeybook.presenter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.SourceEditContract;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Objects;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public class SourceEditPresenterImpl extends BasePresenterImpl<SourceEditContract.View> implements SourceEditContract.Presenter {

    @Override
    public void saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld, boolean debug) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (bookSourceOld != null && !Objects.equals(bookSource.getBookSourceUrl(), bookSourceOld.getBookSourceUrl())) {
                DbHelper.getInstance().getDaoSession().getBookSourceBeanDao().delete(bookSourceOld);
            }
            BookSourceManager.add(bookSource);
            e.onNext(true);
        }).subscribeOn(Schedulers.single())
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
                        mView.showSnackBar("书源保存失败");
                    }
                });
    }

    @Override
    public void copySource(BookSourceBean bookSourceBean) {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, mView.getBookSourceStr());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            mView.showSnackBar("拷贝成功");
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
            mView.showSnackBar("数据格式不对");
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
            } else {
                emitter.onError(new IllegalArgumentException("string data covert to bitmap failed！"));
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mView.shareSource(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackBar("分享失败");
                    }
                });
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }

}
