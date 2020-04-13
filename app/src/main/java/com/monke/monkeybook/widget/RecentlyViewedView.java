package com.monke.monkeybook.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.adapter.RecentlyViewedAdapter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RecentlyViewedView extends RelativeLayout {


    private TextView mEmptyText;
    private RecentlyViewedAdapter mAdapter;

    private View mMainView;

    public RecentlyViewedView(Context context) {
        super(context);
    }

    public RecentlyViewedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.dialog_recently_viewed, this);
        onViewCreated(context);
    }


    public void onViewCreated(Context context) {
        mMainView = findViewById(R.id.view_main);
        mEmptyText = findViewById(R.id.tv_empty);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter = new RecentlyViewedAdapter(context));

        mAdapter.setOnItemClickCallback(new RecentlyViewedAdapter.OnItemClickCallback() {
            @Override
            public void onClick(View itemView, BookShelfBean item) {
                ReadBookActivity.startThis((MBaseActivity) context, item);
                hide();
            }

            @Override
            public void onLongClick(View itemView, BookShelfBean item) {
                BookDetailActivity.startThis((MBaseActivity) context, item);
                hide();
            }
        });

        View maskView = findViewById(R.id.view_masking);
        maskView.setOnClickListener(v -> togglePop());

        setVisibility(INVISIBLE);

        initData();
    }

    private void initData(){
        Observable.create((ObservableOnSubscribe<List<BookShelfBean>>) emitter -> {
            emitter.onNext(BookshelfHelp.queryBooks(100));
            emitter.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> bookShelfBeans) {
                        mEmptyText.setVisibility(bookShelfBeans.isEmpty()?View.VISIBLE:View.GONE);
                        mAdapter.setItems(bookShelfBeans);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mEmptyText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void togglePop() {
        final boolean pendingShow = !isShown();

        if (pendingShow) {
            setVisibility(View.VISIBLE);
        }

        float start = pendingShow ? -mMainView.getHeight() : 0;
        float end = pendingShow ? 0 : -mMainView.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mMainView, "translationY", start, end);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!pendingShow) {
                    setVisibility(View.INVISIBLE);
                }
            }
        });
        animator.start();
    }


    public void show(){
        if(!isShown()){
            post(this::togglePop);
        }
    }

    public void hide(){
        if(isShown()){
            togglePop();
        }
    }

}
