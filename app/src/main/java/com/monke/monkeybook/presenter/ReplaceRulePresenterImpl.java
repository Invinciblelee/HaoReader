package com.monke.monkeybook.presenter;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.DocumentHelper;
import com.monke.monkeybook.model.ReplaceRuleManager;
import com.monke.monkeybook.presenter.contract.ReplaceRuleContract;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by GKF on 2017/12/18.
 * 替换规则
 */

public class ReplaceRulePresenterImpl extends BasePresenterImpl<ReplaceRuleContract.View> implements ReplaceRuleContract.Presenter {

    @Override
    public void detachView() {
        super.detachView();
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            int i = 0;
            for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
                i++;
                replaceRuleBean.setSerialNumber(i + 1);
            }
            ReplaceRuleManager.saveAll(replaceRuleBeans);
            e.onNext(ReplaceRuleManager.getAll());
            e.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(ReplaceRuleBean replaceRuleBean) {
        mView.showLoading("正在删除");
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManager.delete(replaceRuleBean);
            e.onNext(ReplaceRuleManager.getAll());
            e.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh(replaceRuleBeans);
                        mView.getSnackBar(replaceRuleBean.getReplaceSummary() + "已删除")
                                .setDuration(BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("恢复", view -> restoreData(replaceRuleBean))
                                .show();
                        mView.dismissLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.dismissLoading();
                    }
                });
    }

    @Override
    public void delData(List<ReplaceRuleBean> replaceRuleBeans) {
        mView.showLoading("正在删除选中规则");
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManager.deleteAll(replaceRuleBeans);
            e.onNext(ReplaceRuleManager.getAll());
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.toast("删除成功");
                        mView.refresh(replaceRuleBeans);
                        mView.dismissLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("删除失败");
                        mView.dismissLoading();
                    }
                });
    }

    @Override
    public void editData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManager.save(replaceRuleBean);
            e.onNext(ReplaceRuleManager.getAll());
            e.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh(replaceRuleBeans);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void restoreData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManager.save(replaceRuleBean);
            e.onNext(ReplaceRuleManager.getAll());
            e.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh(replaceRuleBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void importDataS(File file) {
        mView.showLoading("正在导入规则");
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            DocumentFile documentFile = DocumentFile.fromFile(file);
            String json = DocumentHelper.readString(documentFile);
            emitter.onNext(json);
            emitter.onComplete();
        }).flatMap(json -> ReplaceRuleManager.importFromNet(json)
                .flatMap(aBoolean -> {
                    if (aBoolean) {
                        return Observable.just(ReplaceRuleManager.getAll());
                    } else {
                        return Observable.error(new Exception("import rule failed"));
                    }
                }))
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh(ReplaceRuleManager.getAll());
                        mView.toast("规则导入成功");
                        mView.dismissLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("规则导入失败");
                        mView.dismissLoading();
                    }
                });
    }


    @Override
    public void importDataS(String sourceUrl) {
        ReplaceRuleManager.importFromNet(sourceUrl)
                .flatMap(aBoolean -> {
                    if (aBoolean) {
                        return Observable.just(ReplaceRuleManager.getAll());
                    } else {
                        return Observable.error(new Exception("import rule failed"));
                    }
                })
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh(replaceRuleBeans);
                        mView.toast("规则导入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("规则导入失败");
                    }
                });
    }

}
