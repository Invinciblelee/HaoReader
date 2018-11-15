package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BigImageActivity extends MBaseActivity {

    @BindView(R.id.fl_content)
    View flContent;
    @BindView(R.id.iv_big_image)
    ImageView bigImage;

    private String imageUrl;

    public static void startThis(MBaseActivity activity, String url, View shareView){
        Intent intent = new Intent(activity, BigImageActivity.class);
        intent.putExtra("image", url);
        activity.startActivityByAnim(intent, shareView, "big_image");
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void initImmersionBar() {
        mImmersionBar.fullScreen(true)
                .transparentBar()
                .init();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_big_image);
    }

    @Override
    protected void initData() {
        imageUrl = getIntent().getStringExtra("image");
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        Glide.with(this)
                .load(imageUrl)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(bigImage);

        flContent.setOnClickListener(v -> finishByAnim(android.R.anim.fade_in, android.R.anim.fade_out));
    }
}
