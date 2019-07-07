package com.monke.basemvplib;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;

public abstract class BaseActivity<T extends IPresenter> extends AppCompatActivity implements IView {
    protected Bundle savedInstanceState;
    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        initSDK();
        onCreateActivity();
        mPresenter = initInjector();
        attachView();
        initData();
        bindView();
        setupActionBar();
        bindEvent();
        firstRequest();
    }

    /**
     * 首次逻辑操作
     */
    protected void firstRequest() {

    }

    /**
     *
     * 事件触发绑定
     */
    protected void bindEvent() {

    }

    /**
     * 控件绑定
     */
    protected void bindView() {

    }

    protected void setupActionBar(){

    }

    /**
     * P层绑定V层
     */
    private void attachView() {
        if (null != mPresenter) {
            mPresenter.attachView(this);
        }
    }

    /**
     * P层解绑V层
     */
    private void detachView() {
        if (null != mPresenter) {
            mPresenter.detachView();
        }
    }

    /**
     * SDK初始化
     */
    protected void initSDK() {

    }

    /**
     * 数据初始化
     */
    protected void initData(){

    }

    /**
     * P层绑定   若无则返回null;
     */
    protected T initInjector(){
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    protected void onCreateActivity(){
    }



    @Override
    protected void onDestroy() {
        detachView();
        super.onDestroy();
    }

    ////////////////////////////////启动Activity转场动画/////////////////////////////////////////////

    public Context getContext() {
        return this;
    }
}