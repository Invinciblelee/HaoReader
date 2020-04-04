package com.monke.monkeybook.view.fragment.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.utils.ScreenUtils;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.activity.ReadBookActivity;
import com.monke.monkeybook.view.adapter.RecentlyViewedAdapter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RecentlyViewedDialog extends AppCompatDialog {


    private TextView mEmptyText;
    private RecentlyViewedAdapter mAdapter;


    public static void show(AppCompatActivity activity){
        if(activity == null || activity.isFinishing()) return;
        RecentlyViewedDialog dialog = new RecentlyViewedDialog();
        dialog.show(activity.getSupportFragmentManager(), "recentlyViewed");
    }

    public RecentlyViewedDialog() {
        setStyle(STYLE_NO_TITLE, R.style.Style_Custom_Dialog_Translucent);
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_recently_viewed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mEmptyText = view.findViewById(R.id.tv_empty);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter = new RecentlyViewedAdapter(getActivity()));
        recyclerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(requireContext(), 160) + ScreenUtils.getStatusBarHeight()));

        mAdapter.setOnItemClickCallback(new RecentlyViewedAdapter.OnItemClickCallback() {
            @Override
            public void onClick(View itemView, BookShelfBean item) {
                ReadBookActivity.startThis((MBaseActivity) getActivity(), item);
                dismissAllowingStateLoss();
            }

            @Override
            public void onLongClick(View itemView, BookShelfBean item) {
                BookDetailActivity.startThis((MBaseActivity) getActivity(), item);
                dismissAllowingStateLoss();
            }
        });

        initData();
    }

    @Override
    protected void onDialogAttachWindow(@NonNull Window window) {
        window.setGravity(Gravity.TOP);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(requireContext(), 160) + ScreenUtils.getStatusBarHeight());
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

}
