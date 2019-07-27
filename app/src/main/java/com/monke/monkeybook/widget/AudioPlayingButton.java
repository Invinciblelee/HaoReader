package com.monke.monkeybook.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.utils.ContextUtils;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.view.activity.AudioBookPlayActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioPlayingButton extends FrameLayout {

    @BindView(R.id.iv_image_cover)
    ImageView ivCover;
    @BindView(R.id.btn_pause)
    ImageView btnPause;
    @BindView(R.id.audio_progress)
    CircleProgressBar progressBar;

    private ValueAnimator animator;

    public AudioPlayingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_audio_playing, this);
        ButterKnife.bind(this);

        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    private void initView() {
        ViewCompat.setElevation(this, DensityUtil.dp2px(getContext(), 8));
        ViewCompat.setBackground(this, getResources().getDrawable(R.drawable.shape_audio_bar));
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);


        ivCover.setOnClickListener(this::startPlayerActivity);

        btnPause.setOnClickListener(v -> startPlayerActivity(ivCover));
    }

    private void startPlayerActivity(View v) {
        AppCompatActivity activity = ContextUtils.getCompatActivity(this);

        if (activity instanceof MBaseActivity) {
            AudioBookPlayActivity.startThis((MBaseActivity) activity, v, null);
        }
    }

    public void start() {
        if (!isShown()) return;
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 360f);
            animator.addUpdateListener(animation -> ivCover.setRotation((Float) animation.getAnimatedValue()));
            animator.setDuration(10000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setStartDelay(1000);
            animator.start();
        } else {
            animator.resume();
        }
    }

    public void pause() {
        if (animator != null) {
            animator.pause();
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    public void setPause() {
        btnPause.setSelected(false);
        btnPause.setImageResource(R.drawable.ic_play_white_24dp);
        pause();
    }

    public void setResume() {
        btnPause.setSelected(true);
        btnPause.setImageDrawable(null);
        setVisibility(View.VISIBLE);
        start();
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.INVISIBLE);
    }

    public void setProgress(int progress, int duration) {
        progressBar.setMaxProgress(duration);
        progressBar.setProgress(progress);
    }

    public void setCoverImage(String image) {
        Glide.with(this).load(image)
                .apply(new RequestOptions().dontAnimate().centerCrop()
                        .transform(new CenterCrop(), new CircleCrop())
                        .error(R.drawable.img_cover_default)
                        .placeholder(R.drawable.img_cover_default)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(ivCover);
    }
}
