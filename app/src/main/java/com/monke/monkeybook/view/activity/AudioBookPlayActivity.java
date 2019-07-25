package com.monke.monkeybook.view.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.service.AudioBookPlayService;
import com.monke.monkeybook.view.fragment.dialog.ChangeSourceDialog;
import com.monke.monkeybook.view.popupwindow.AudioChapterPop;
import com.monke.monkeybook.view.popupwindow.AudioTimerPop;
import com.monke.monkeybook.view.popupwindow.CheckAddShelfPop;
import com.monke.monkeybook.widget.theme.AppCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.monke.basemvplib.NetworkUtil.isNetworkAvailable;

public class AudioBookPlayActivity extends MBaseActivity implements View.OnClickListener, AudioChapterPop.OnChapterSelectListener, AudioTimerPop.OnTimeSelectListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_blur_cover)
    ImageView ivBlurCover;
    @BindView(R.id.iv_circle_cover)
    ImageView ivCover;
    @BindView(R.id.tv_progress)
    TextView tvProgress;
    @BindView(R.id.seekbar)
    SeekBar seekBar;
    @BindView(R.id.tv_duration)
    TextView tvDuration;
    @BindView(R.id.btn_timer)
    View btnTimer;
    @BindView(R.id.btn_previous)
    View btnPrevious;
    @BindView(R.id.btn_pause)
    ImageView btnPause;
    @BindView(R.id.btn_next)
    View btnNext;
    @BindView(R.id.btn_catalog)
    View btnCatalog;
    @BindView(R.id.loading_progress)
    ProgressBar progressBar;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private ValueAnimator animator;

    private AudioChapterPop audioChapterPop;
    private AudioTimerPop audioTimerPop;
    private CheckAddShelfPop checkAddShelfPop;

    private BookInfoBean bookInfoBean;

    public static void startThis(MBaseActivity activity, View transitionView, BookShelfBean bookShelf) {
        Intent intent = new Intent(activity, AudioBookPlayActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf == null ? null : bookShelf.copy());
        if (transitionView != null) {
            activity.startActivityByAnim(intent, transitionView, transitionView.getTransitionName());
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    public void initImmersionBar() {
        mImmersionBar.transparentStatusBar();

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

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
        setCoverImage(null);
        setButtonEnabled(false);
        setMediaButtonEnabled(false);
        seekBar.setEnabled(false);

        setTitle(null);

        audioChapterPop = new AudioChapterPop(this, this);
        audioTimerPop = new AudioTimerPop(this, this);
    }

    @Override
    protected void setupActionBar() {
        AppCompat.setToolbarNavIconTint(toolbar, getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void bindEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setProgress(progress, seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioBookPlayService.seek(AudioBookPlayActivity.this, seekBar.getProgress());
            }
        });

        btnTimer.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnCatalog.setOnClickListener(this);
    }

    @Override
    protected void firstRequest() {
        RxBus.get().register(this);

        final boolean resume = getIntent().getBooleanExtra("resume", true);
        final String key = getIntent().getStringExtra("data_key");
        BookShelfBean bookShelfBean = BitIntentDataManager.getInstance().getData(key, null);
        AudioBookPlayService.start(this, bookShelfBean, resume);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_audio_play_activity, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItemImpl item = (MenuItemImpl) menu.getItem(i);
            AppCompat.setTint(item, getResources().getColor(R.color.colorBarText));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_change_source) {
            changeSource();
        } else if (item.getItemId() == R.id.action_chapter_refresh) {
            AudioBookPlayService.refresh(this);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRotationAnim();
        RxBus.get().unregister(this);
        AudioBookPlayService.stopNotShelfExists(this);
    }

    @Override
    public void onBackPressed() {
        if (bookInfoBean != null) {
            if (!BookshelfHelp.isInBookShelf(bookInfoBean.getNoteUrl())) {
                showAddShelfPop(bookInfoBean.getName());
                return;
            }
        }

        supportFinishAfterTransition();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSelected(ChapterBean chapterBean) {
        AudioBookPlayService.play(this, chapterBean);
    }

    @Override
    public void onSelected(int timerMinute) {
        AudioBookPlayService.timer(this, timerMinute);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                if (v.isSelected()) {
                    AudioBookPlayService.pause(this);
                } else {
                    AudioBookPlayService.resume(this);
                }
                break;
            case R.id.btn_previous:
                AudioBookPlayService.previous(this);
                break;
            case R.id.btn_next:
                AudioBookPlayService.next(this);
                break;

            case R.id.btn_catalog:
                audioChapterPop.showAtLocation(ivBlurCover, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.btn_timer:
                audioTimerPop.showAtLocation(ivBlurCover, Gravity.BOTTOM, 0, 0);
                break;
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.AUDIO_PLAY)})
    public void onPlayEvent(AudioPlayInfo info) {
        String action = info.getAction();
        switch (action) {
            case AudioBookPlayService.ACTION_ATTACH:
                bookInfoBean = info.getBookInfoBean();
                setTitle(bookInfoBean.getName());
                setCoverImage(bookInfoBean.getRealCoverUrl());
                setAlarmTimer(info.getTimerMinute());
                setChapters(info.getChapterBeans(), info.getDurChapterIndex());
                setButtonEnabled(info.isChapterNotEmpty());
                setMediaButtonEnabled(true);
                setProgress(info.getProgress(), info.getDuration());
                showLoading(info.isLoading());
                if (info.isPause()) {
                    setPause();
                } else {
                    setResume();
                }
                break;
            case AudioBookPlayService.ACTION_LOADING:
                showLoading(info.isLoading());
                break;
            case AudioBookPlayService.ACTION_START:
                setChapters(info.getChapterBeans(), info.getDurChapterIndex());
                setButtonEnabled(info.isChapterNotEmpty());
                setResume();
                break;
            case AudioBookPlayService.ACTION_PREPARE:
                updateIndex(info.getDurChapter().getDurChapterIndex());
                setSubTitle(info.getDurChapter().getDisplayDurChapterName());
                setProgress(info.getProgress(), info.getDuration());
                setMediaButtonEnabled(true);
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
            case AudioBookPlayService.ACTION_SEEK_ENABLED:
                seekBar.setEnabled(info.isSeekEnabled());
                break;
            case AudioBookPlayService.ACTION_STOP:
                finish();
                break;
        }
    }

    private void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void setSubTitle(String title) {
        if (title != null) {
            tvTitle.setText(title);
        }
    }

    private void setProgress(int progress, int duration) {
        seekBar.setMax(duration);
        seekBar.setProgress(progress);
        if (duration > 0) {
            seekBar.setEnabled(true);
        }
        tvProgress.setText(dateFormat.format(new Date(progress)));
        tvDuration.setText(dateFormat.format(new Date(duration)));
    }

    private void setChapters(List<ChapterBean> chapterBeans, int durChapter) {
        audioChapterPop.setDataSet(chapterBeans);
        audioChapterPop.upIndex(durChapter);
        if (chapterBeans != null && !chapterBeans.isEmpty()) {
            ChapterBean chapterBean = chapterBeans.get(durChapter);
            setSubTitle(chapterBean.getDisplayDurChapterName());
        }
    }

    private void updateIndex(int durChapter) {
        audioChapterPop.upIndex(durChapter);
    }

    private void setAlarmTimer(int timer) {
        audioTimerPop.upIndexByValue(timer);
    }

    private void setPause() {
        btnPause.setSelected(false);
        btnPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        stopRotationAnim();
    }

    private void setResume() {
        btnPause.setSelected(true);
        btnPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        startRotationAnim();
    }

    private void setButtonEnabled(boolean enabled) {
        btnTimer.setEnabled(enabled);
        btnCatalog.setEnabled(enabled);
    }

    private void setMediaButtonEnabled(boolean enabled) {
        btnPrevious.setEnabled(enabled);
        btnPause.setEnabled(enabled);
        btnNext.setEnabled(enabled);
    }

    private void showLoading(boolean showProgress) {
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.INVISIBLE);
    }

    private void startRotationAnim() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 360f);
            animator.addUpdateListener(animation -> ivCover.setRotation((Float) animation.getAnimatedValue()));
            animator.setDuration(30000);
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

    private void setCoverImage(String image) {
        Glide.with(AudioBookPlayActivity.this).load(image)
                .apply(new RequestOptions().dontAnimate().centerCrop()
                        .centerCrop()
                        .error(R.drawable.img_cover_default)
                        .placeholder(R.drawable.img_cover_default))
                .into(ivCover);

        Glide.with(this).load(image)
                .apply(new RequestOptions()
                        .dontAnimate()
                        .centerCrop()
                        .placeholder(R.drawable.img_cover_gs)
                        .error(R.drawable.img_cover_gs))
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                .into(ivBlurCover);
    }

    private void showAddShelfPop(String bookName) {
        if (checkAddShelfPop == null) {
            checkAddShelfPop = new CheckAddShelfPop(this, bookName,
                    new CheckAddShelfPop.OnItemClickListener() {
                        @Override
                        public void clickExit() {
                            finish();
                        }

                        @Override
                        public void clickAddShelf() {
                            AudioBookPlayService.addShelf(AudioBookPlayActivity.this);
                            checkAddShelfPop.dismiss();
                        }
                    }, true);
        }
        if (!checkAddShelfPop.isShowing()) {
            checkAddShelfPop.showAtLocation(ivBlurCover, Gravity.CENTER, 0, 0);
        }
    }

    /**
     * 换源
     */
    private void changeSource() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法换源");
            return;
        }

        if (bookInfoBean == null) {
            return;
        }

        ChangeSourceDialog.show(getSupportFragmentManager(), bookInfoBean, false, searchBookBean -> {
            AudioBookPlayService.changeSource(AudioBookPlayActivity.this, searchBookBean);
        });
    }
}
