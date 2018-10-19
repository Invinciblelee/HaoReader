package com.monke.basemvplib;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;

public abstract class BaseFragment<T extends IPresenter> extends com.trello.rxlifecycle2.components.support.RxFragment implements IView {
    protected View view;
    protected Bundle savedInstanceState;
    protected T mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        initSDK();
        view = createView(inflater, container);
        mPresenter = initInjector();
        if(mPresenter != null) {
            mPresenter.attachView(this);
        }
        initData();
        bindView();
        bindEvent();
        firstRequest();
        return view;
    }

    @Override
    public void onDestroy() {
        if(mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

    /**
     * P层绑定   若无则返回null;
     */
    protected T initInjector(){
        return null;
    }

    /**
     * 事件触发绑定
     */
    protected void bindEvent() {

    }

    /**
     * 控件绑定
     */
    protected void bindView() {

    }

    /**
     * 数据初始化
     */
    protected void initData() {

    }

    /**
     * 首次逻辑操作
     */
    protected void firstRequest() {

    }

    /**
     * 加载布局
     */
    protected abstract View createView(LayoutInflater inflater, ViewGroup container);

    /**
     * 第三方SDK初始化
     */
    protected void initSDK() {

    }
}
