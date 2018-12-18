package com.monke.monkeybook.view.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class AudioBookPlayActivity extends MBaseActivity {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_blur_cover)
    ImageView ivBlurCover;
    @BindView(R.id.iv_circle_cover)
    ImageView ivCover;


    public static void startThis(MBaseActivity context, View transitionView) {
        Intent intent = new Intent(context, AudioBookPlayActivity.class);
        context.startActivityByAnim(intent, transitionView, transitionView.getTransitionName());
    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        mImmersionBar.statusBarDarkFont(false);
        mImmersionBar.init();
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_audio_book_player);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        showCoverImage(null);

    }

    @Override
    protected void firstRequest() {
//        startRotationAnim();
    }

    private void startRotationAnim() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
        animator.addUpdateListener(animation -> ivCover.setRotation((Float) animation.getAnimatedValue()));
        animator.setDuration(30000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(Integer.MAX_VALUE);
        animator.setStartDelay(1000);
        animator.start();
    }

    private void showCoverImage(String image) {
        if (!this.isFinishing()) {
            Glide.with(this).load(R.drawable.img_cover_default)
                    .apply(new RequestOptions().dontAnimate().centerCrop()
                            .transforms(new CenterCrop(), new CircleCrop())
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .placeholder(R.drawable.shape_bg_circle_image)
                            .error(R.drawable.shape_bg_circle_image)).into(ivCover);

            Glide.with(this).load(image)
                    .apply(new RequestOptions()
                            .dontAnimate()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .placeholder(R.drawable.img_cover_gs)
                            .error(R.drawable.img_cover_gs))
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .into(ivBlurCover);
        }
    }
}
