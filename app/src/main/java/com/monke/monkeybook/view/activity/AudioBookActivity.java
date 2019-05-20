package com.monke.monkeybook.view.activity;

import android.animation.ValueAnimator;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.presenter.AudioBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.AudioBookContract;
import com.monke.monkeybook.service.AudioBookPlayService;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.widget.theme.AppCompat;
import com.monke.monkeybook.widget.CircleProgressBar;
import com.monke.monkeybook.widget.refreshview.SwipeRefreshLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioBookActivity extends MBaseActivity<AudioBookContract.Presenter> implements AudioBookContract.View, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;
    @BindView(R.id.iv_image_cover)
    ImageView ivCover;
    @BindView(R.id.view_audio_running)
    View runningView;
    @BindView(R.id.btn_pause)
    ImageView btnPause;
    @BindView(R.id.audio_progress)
    CircleProgressBar progressBar;

    private ValueAnimator animator;

    private BookShelfListAdapter bookListAdapter;

    @Override
    protected AudioBookContract.Presenter initInjector() {
        return new AudioBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_audio_book);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setCoverImage(null);

        rvBookshelf.setHasFixedSize(true);

        int bookPx = mPresenter.getBookshelfPx();
        bookListAdapter = new BookShelfListAdapter(getContext(), 4, bookPx);
        rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));

        if (bookPx == 2) {
            MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookListAdapter.getItemTouchCallbackListener());
        }

        rvBookshelf.setAdapter(bookListAdapter);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void bindEvent() {
        ivCover.setOnClickListener(v -> AudioBookPlayActivity.startThis(AudioBookActivity.this, v, null));


        btnPause.setOnClickListener(v -> {
            AudioBookPlayActivity.startThis(this, ivCover, null);
        });

        bookListAdapter.setItemClickListener(new OnBookItemClickListenerTwo() {
            @Override
            public void onClick(View view, BookShelfBean bookShelf) {
                AudioBookPlayActivity.startThis(AudioBookActivity.this, ivCover, bookShelf);
            }

            @Override
            public void onLongClick(View view, BookShelfBean bookShelf) {
                BookDetailActivity.startThis(AudioBookActivity.this, bookShelf);
            }
        });
    }

    @Override
    protected void firstRequest() {
        RxBus.get().register(this);
        if (AudioBookPlayService.running) {
            runningView.setVisibility(View.VISIBLE);
            AudioBookPlayService.start(this);
        } else {
            runningView.setVisibility(View.INVISIBLE);
        }

        mPresenter.loadAudioBooks(false);
    }

    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.colorBarText));
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.audio_book);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRotationAnim();
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.AUDIO_PLAY)})
    public void onPlayEvent(AudioPlayInfo info) {
        switch (info.getAction()) {
            case AudioBookPlayService.ACTION_ATTACH:
                setCoverImage(info.getBookInfoBean().getRealCoverUrl());
                setProgress(info.getProgress(), info.getDuration());
                if (info.isPause()) {
                    setPause();
                } else {
                    setResume();
                }
                runningView.setVisibility(View.VISIBLE);
                break;
            case AudioBookPlayService.ACTION_START:
                setResume();
                break;
            case AudioBookPlayService.ACTION_PAUSE:
                setPause();
                break;
            case AudioBookPlayService.ACTION_RESUME:
                setResume();
                break;
            case AudioBookPlayService.ACTION_PROGRESS:
                setProgress(info.getProgress(), info.getDuration());
                break;
            case AudioBookPlayService.ACTION_STOP:
                runningView.setVisibility(View.INVISIBLE);
                stopRotationAnim();
                break;
        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.stopRefreshing();
        mPresenter.loadAudioBooks(true);
    }

    @Override
    public void showAudioBooks(List<BookShelfBean> bookShelfBeans) {
        boolean isEmptyBefore = bookListAdapter.getItemCount() == 0;

        bookListAdapter.replaceAll(bookShelfBeans);

        if (!isEmptyBefore) {
            rvBookshelf.scrollToPosition(0);
        } else {
            startLayoutAnimationIfNeed();
        }
    }

    @Override
    public void addBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.addBook(bookShelfBean);
    }

    @Override
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.removeBook(bookShelfBean);
    }

    @Override
    public void updateBook(BookShelfBean bookShelfBean, boolean b) {
        bookListAdapter.updateBook(bookShelfBean, b);
    }

    @Override
    public void sortBookShelf() {
        bookListAdapter.sort();
    }

    @Override
    public void refreshFinish() {
        refreshLayout.stopRefreshing();
    }

    private void startRotationAnim() {
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

    private void stopRotationAnim() {
        if (animator != null) {
            animator.pause();
        }
    }

    private void setPause() {
        btnPause.setSelected(false);
        btnPause.setImageResource(R.drawable.ic_play_white_24dp);
        stopRotationAnim();
    }

    private void setResume() {
        btnPause.setSelected(true);
        btnPause.setImageDrawable(null);
        startRotationAnim();
    }

    private void setProgress(int progress, int duration) {
        progressBar.setMaxProgress(duration);
        progressBar.setProgress(progress);
    }

    private void startLayoutAnimationIfNeed() {
        if (mPresenter.getNeedAnim()) {
            if (rvBookshelf.getLayoutAnimation() == null) {
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.anim_bookshelf_layout);
                rvBookshelf.setLayoutAnimation(animation);
            } else {
                rvBookshelf.startLayoutAnimation();
            }
        }
    }

    private void setCoverImage(String image) {
        Glide.with(AudioBookActivity.this).load(image)
                .apply(new RequestOptions().dontAnimate().centerCrop()
                        .transforms(new CenterCrop(), new CircleCrop())
                        .error(R.drawable.img_cover_default)
                        .placeholder(R.drawable.img_cover_default)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(ivCover);
    }
}
