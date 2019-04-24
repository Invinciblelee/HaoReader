package com.monke.monkeybook.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.AudioBookPlayModelImpl;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.AudioBookPlayActivity;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioBookPlayService extends Service {

    private static final String TAG = AudioBookPlayService.class.getSimpleName();

    public static final String ACTION_ATTACH = "ACTION_ATTACH";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PULL = "ACTION_PULL";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_PREPARE = "ACTION_PREPARE";
    public static final String ACTION_PROGRESS = "ACTION_PROGRESS";
    public static final String ACTION_LOADING = "ACTION_LOADING";
    public static final String ACTION_TIMER = "ACTION_TIMER";
    public static final String ACTION_TIMER_PROGRESS = "ACTION_TIMER_PROGRESS";
    public static final String ACTION_ADD_SHELF = "ACTION_ADD_SHELF";
    public static final String ACTION_STOP_NOT_IN_SHELF = "ACTION_STOP_NOT_IN_SHELF";

    private static final int notificationId = 19901122;

    public static boolean running;

    private BookShelfBean bookShelfBean;

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isPrepared;
    private boolean isPause;
    private boolean isError;
    private int targetPosition;

    private int progress;
    private int duration;

    private int timerMinute;
    private int timerUntilFinish;

    private AudioBookPlayModelImpl mModel;

    private ScheduledExecutorService mTimer;
    private ScheduledExecutorService mAlertTimer;

    public static void start(Context context, BookShelfBean bookShelfBean) {
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_START);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelfBean == null ? null : bookShelfBean.copy());
        context.startService(intent);
    }

    public static void play(Context context, ChapterBean chapterBean) {
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra("chapter", chapterBean);
        context.startService(intent);
    }

    public static void addShelf(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_ADD_SHELF);
        context.startService(intent);
    }

    public static void pull(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PULL);
        context.startService(intent);
    }

    public static void pause(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    public static void resume(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_RESUME);
        context.startService(intent);
    }

    public static void next(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_NEXT);
        context.startService(intent);
    }

    public static void previous(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PREVIOUS);
        context.startService(intent);
    }

    public static void seek(Context context, int progress) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PROGRESS);
        intent.putExtra("position", progress);
        context.startService(intent);
    }

    public static void timer(Context context, int timerMinute) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_TIMER);
        intent.putExtra("minute", timerMinute);
        context.startService(intent);
    }

    public static void stop(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    public static void stopIfNotShelfBook(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_STOP_NOT_IN_SHELF);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        RxBus.get().register(this);
        running = true;
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    onActionStart(intent);
                    break;
                case ACTION_PULL:
                    pullAudioInfo();
                    break;
                case ACTION_PLAY:
                    ChapterBean chapterBean = intent.getParcelableExtra("chapter");
                    if (mModel != null) {
                        mediaPlayer.reset();
                        mModel.playChapter(chapterBean, true);
                    }
                    break;
                case ACTION_ADD_SHELF:
                    if (mModel != null) {
                        mModel.addToShelf();
                    }
                    break;
                case ACTION_NEXT:
                    nextPlay();
                    break;
                case ACTION_PREVIOUS:
                    previousPlay();
                    break;
                case ACTION_PAUSE:
                    pausePlay();
                    break;
                case ACTION_RESUME:
                    resumePlay();
                    break;
                case ACTION_PROGRESS:
                    int position = intent.getIntExtra("position", 0);
                    seekTo(position);
                    break;
                case ACTION_TIMER:
                    timerMinute = intent.getIntExtra("minute", -1);
                    timerUntilFinish = timerMinute;
                    if (timerMinute == -1) {
                        cancelAlarmTimer();
                    } else {
                        setAlarmTimer();
                    }
                    break;
                case ACTION_TIMER_PROGRESS:
                    int minute = intent.getIntExtra("minute", -1);
                    updateAlarmTimer(minute);
                    break;
                case ACTION_STOP:
                    stopPlay();
                    break;
                case ACTION_STOP_NOT_IN_SHELF:
                    if (mModel != null) {
                        if (!mModel.inBookShelf()) {
                            stopPlay();
                        }
                    } else {
                        stopPlay();
                    }
                    break;
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionStart(Intent intent) {
        running = true;
        final String key = intent.getStringExtra("data_key");
        final BookShelfBean bookShelf = BitIntentDataManager.getInstance().getData(key, null);
        BitIntentDataManager.getInstance().cleanData(key);
        if (bookShelf != null) {
            if (bookShelfBean != null) {
                if (TextUtils.equals(bookShelf.getNoteUrl(), bookShelfBean.getNoteUrl())) {
                    restart();
                    return;
                }
                bookShelfBean = bookShelf;
                initModel();
            } else {
                bookShelfBean = bookShelf;
                initModel();
            }
        } else {
            restart();
        }
        updateNotification();
    }

    private void initModel() {
        resetPlay();

        mModel = createModel(bookShelfBean);
        mModel.registerPlayCallback(new IAudioBookPlayModel.PlayCallback() {

            @Override
            public void onStart() {
                sendBroadcast(ACTION_LOADING, AudioPlayInfo.loading(true));
            }

            @Override
            public void onPrepare(ChapterBean chapterBean) {
                updateNotification();
                sendBroadcast(ACTION_PREPARE, AudioPlayInfo.start(chapterBean));
            }

            @Override
            public void onPlay(ChapterBean chapterBean) {
                targetPosition = chapterBean.getStart();
                startPlay(chapterBean.getDurChapterPlayUrl());
                updateNotification();
            }

            @Override
            public void onError(Throwable throwable) {
                if (NetworkUtil.isNetworkAvailable()) {
                    ToastUtils.toast(AudioBookPlayService.this, "播放失败，无法获取播放链接");
                } else {
                    ToastUtils.toast(AudioBookPlayService.this, "网络连接失败");
                }
                sendWhenError();
            }
        });

        initChapterList();
    }

    private void initChapterList() {
        mModel.ensureChapterList(new IAudioBookPlayModel.Callback<BookShelfBean>() {
            @Override
            public void onSuccess(BookShelfBean data) {
                sendBroadcast(ACTION_START, AudioPlayInfo.start(timerMinute, data.getBookInfoBean().getRealCoverUrl(), data.getDurChapter(), data.getChapterList()));
            }

            @Override
            public void onError(Throwable error) {
                if (NetworkUtil.isNetworkAvailable()) {
                    ToastUtils.toast(AudioBookPlayService.this, "播放目录获取失败");
                } else {
                    ToastUtils.toast(AudioBookPlayService.this, "网络连接失败");
                }
                sendWhenError();
            }
        });
    }

    private void initMediaPlayer() {
        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            isError = false;

            if (targetPosition != 0) {
                mp.seekTo(targetPosition);
                targetPosition = 0;
            }

            mp.start();

            if (isPause) {
                isPause = false;
                setPause(false);
            }

            cancelProgressTimer();
            setProgressTimer();

            sendBroadcast(ACTION_LOADING, AudioPlayInfo.loading(false));
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            if (isPrepared) {
                //有时无法自动播放下一章，稍微延迟一下
                handler.postDelayed(this::nextPlay, 100L);
            }
        });

        mediaPlayer.setOnInfoListener((mp, what, extra) -> {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                sendBroadcast(ACTION_LOADING, AudioPlayInfo.loading(true));
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                sendBroadcast(ACTION_LOADING, AudioPlayInfo.loading(false));
            }
            return false;
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Logger.d(TAG, "audio error --> " + what + "  " + extra);
            isPrepared = false;
            if (mModel != null && mModel.retryPlay()) {
                ToastUtils.toast(AudioBookPlayService.this, "播放失败，正在重试");
            } else {
                ToastUtils.toast(AudioBookPlayService.this, "播放失败");
                sendWhenError();
            }
            return false;
        });
    }

    private void restart() {
        if (bookShelfBean == null) return;
        Logger.d(TAG, "audio --> restart");
        sendWhenAttach();
        pullAudioInfo();
        resumePlay();
    }

    private void pullAudioInfo() {
        if (bookShelfBean != null && !bookShelfBean.realChapterListEmpty()) {
            Logger.d(TAG, "audio --> pullAudioInfo");
            AudioPlayInfo info = AudioPlayInfo.pull(timerMinute, timerUntilFinish, bookShelfBean.getDurChapterName(), bookShelfBean.getBookInfoBean().getRealCoverUrl(), bookShelfBean.getChapterList());
            info.setPause(isPause);
            info.setDurChapterIndex(bookShelfBean.getDurChapter());
            if (isPrepared) {
                info.setProgress(mediaPlayer.getCurrentPosition());
                info.setDuration(mediaPlayer.getDuration());
            }
            sendBroadcast(ACTION_PULL, info);
        }
    }

    private void sendWhenError() {
        isError = true;
        isPause = true;
        setPause(true);
        sendBroadcast(ACTION_LOADING, AudioPlayInfo.loading(false));
    }

    private void sendWhenAttach() {
        if (bookShelfBean != null) {
            sendBroadcast(ACTION_ATTACH, AudioPlayInfo.attach(bookShelfBean.getBookInfoBean().getName(), bookShelfBean.getBookInfoBean().getRealCoverUrl(), bookShelfBean.getNoteUrl()));
        }
    }

    private void resetPlay() {
        if (mModel != null) {
            mModel.saveProgress(progress, duration);
            mModel.destroy();
        }

        isPrepared = false;
        isPause = false;
        mediaPlayer.reset();
        sendWhenAttach();
    }

    private void nextPlay() {
        if (isPrepared) {
            mediaPlayer.reset();
            isPrepared = false;
        }

        if (mModel != null) {
            if (!mModel.hasNext()) {
                ToastUtils.toast(this, "没有下一章了");
            } else {
                if (isPause) {
                    isPause = false;
                    setPause(false);
                }
                Logger.d(TAG, "audio --> nextPlay");
                mModel.playNext();
            }
        }
    }

    private void previousPlay() {
        if (isPrepared) {
            mediaPlayer.reset();
            isPrepared = false;
        }

        if (mModel != null) {
            if (!mModel.hasPrevious()) {
                ToastUtils.toast(this, "没有上一章了");
            } else {
                if (isPause) {
                    isPause = false;
                    setPause(false);
                }
                Logger.d(TAG, "audio --> previousPlay");
                mModel.playPrevious();
            }
        }
    }

    private void setPause(boolean pause) {
        if (pause) {
            sendBroadcast(ACTION_PAUSE, AudioPlayInfo.empty());
        } else {
            sendBroadcast(ACTION_RESUME, AudioPlayInfo.empty());
        }
        updateNotification();
    }

    private void pausePlay() {
        if (!isPrepared) return;

        if (!isPause) {
            isPause = true;
            setPause(true);
            mediaPlayer.pause();
        }
    }

    private void resumePlay() {
        if (retryOnError()) return;

        if (!isPrepared) return;

        if (isPause) {
            isPause = false;
            setPause(false);
            mediaPlayer.start();
        }
    }

    private boolean retryOnError() {
        if (mModel == null) return false;

        if (!mModel.isPrepared()) {
            initChapterList();
            return true;
        }

        if (isError) {//失败后重试
            isPause = false;
            setPause(false);
            mModel.retryPlay();
            return true;
        }
        return false;
    }

    private void seekTo(int position) {
        if (!isPrepared) return;

        mediaPlayer.seekTo(position);
    }

    private void startPlay(String url) {
        try {
            Logger.d(TAG, "audio --> play: " + url);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Logger.e(TAG, "startPlay", e);
            ToastUtils.toast(this, "播放失败");
            sendWhenError();
        }
    }

    private void stopPlay() {
        if (mModel != null) {
            mModel.saveProgress(progress, duration);
        }
        sendBroadcast(ACTION_STOP, AudioPlayInfo.empty());
        stopSelf();

        running = false;
    }

    private void setProgressTimer() {
        if (mTimer == null || mTimer.isShutdown()) {
            mTimer = Executors.newSingleThreadScheduledExecutor();
            mTimer.scheduleAtFixedRate(() -> {
                if (mediaPlayer.isPlaying()) {
                    duration = mediaPlayer.getDuration();
                    progress = mediaPlayer.getCurrentPosition();
                    sendBroadcast(ACTION_PROGRESS, AudioPlayInfo.play(progress, duration));
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelProgressTimer() {
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

    private void setAlarmTimer() {
        if (mAlertTimer == null || mAlertTimer.isShutdown()) {
            mAlertTimer = Executors.newSingleThreadScheduledExecutor();
        }

        mAlertTimer.scheduleAtFixedRate(() -> {
            Intent intent = new Intent(AudioBookPlayService.this, AudioBookPlayService.class);
            intent.setAction(ACTION_TIMER_PROGRESS);
            intent.putExtra("minute", -1);
            startService(intent);

        }, 60 * 1000, 60 * 1000, TimeUnit.MILLISECONDS);

        sendBroadcast(ACTION_TIMER_PROGRESS, AudioPlayInfo.timerDown(timerUntilFinish));
        updateNotification();
    }

    private void updateAlarmTimer(int minute) {
        timerUntilFinish = timerMinute + minute;
        int maxTimeMinute = 60;
        if (timerUntilFinish > maxTimeMinute) {
            cancelAlarmTimer();
            timerUntilFinish = 0;
        } else if (timerUntilFinish <= 0) {
            stopPlay();
        } else {
            setAlarmTimer();
        }
    }

    private void cancelAlarmTimer() {
        if (mAlertTimer != null) {
            mAlertTimer.shutdown();
        }
        updateNotification();
    }

    private void updateNotification() {
        if (bookShelfBean == null) {
            return;
        }

        final int dimen = DensityUtil.dp2px(this, 128);
        Glide.with(this)
                .asBitmap()
                .load(bookShelfBean.getBookInfoBean().getRealCoverUrl())
                .into(new RequestFutureTarget<Bitmap>(handler, dimen, dimen) {
                    @Override
                    public synchronized void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        showNotification(resource);
                    }

                    @Override
                    public synchronized void onLoadFailed(@Nullable Drawable errorDrawable) {
                        showNotification(BitmapFactory.decodeResource(getResources(), R.drawable.img_cover_default));
                    }

                });
    }

    private synchronized void showNotification(Bitmap cover) {
        final String contentTitle;
        if (timerUntilFinish > 0) {
            contentTitle = String.format(Locale.getDefault(), "(%d分钟)%s", timerUntilFinish, bookShelfBean.getBookInfoBean().getName());
        } else {
            contentTitle = bookShelfBean.getBookInfoBean().getName();
        }
        final String contentText = bookShelfBean.getDurChapterName();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_audio_notification);
        remoteViews.setTextViewText(R.id.tv_title, contentTitle);
        remoteViews.setTextViewText(R.id.tv_content, TextUtils.isEmpty(contentText) ? "即将为您播放" : contentText);
        if (isPause) {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.ic_play_white_24dp);
            remoteViews.setOnClickPendingIntent(R.id.btn_pause, getThisServicePendingIntent(ACTION_RESUME));
        } else {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.ic_pause_white_24dp);
            remoteViews.setOnClickPendingIntent(R.id.btn_pause, getThisServicePendingIntent(ACTION_PAUSE));
        }

        remoteViews.setOnClickPendingIntent(R.id.btn_previous, getThisServicePendingIntent(ACTION_PREVIOUS));
        remoteViews.setOnClickPendingIntent(R.id.btn_next, getThisServicePendingIntent(ACTION_NEXT));
        remoteViews.setOnClickPendingIntent(R.id.btn_timer, getAudioActivityPendingIntent(true));

        remoteViews.setImageViewBitmap(R.id.iv_cover, cover);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdAudioBook)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setCustomBigContentView(remoteViews)
                .setContentIntent(getAudioActivityPendingIntent(false));
        startForeground(notificationId, builder.build());
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getAudioActivityPendingIntent(boolean showTimer) {
        Intent intent = new Intent(this, AudioBookPlayActivity.class);
        intent.putExtra("showTimer", showTimer);
        return PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private AudioBookPlayModelImpl createModel(BookShelfBean bookShelfBean) {
        return new AudioBookPlayModelImpl(bookShelfBean);
    }

    private void sendBroadcast(String action, AudioPlayInfo info) {
        if (info != null) {
            info.setAction(action);
            RxBus.get().post(RxBusTag.AUDIO_PLAY, info);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        running = false;
        cancelProgressTimer();
        cancelAlarmTimer();
        mediaPlayer.release();
        if (mModel != null) {
            mModel.destroy();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelf) {
        if (bookShelfBean != null && TextUtils.equals(bookShelfBean.getNoteUrl(), bookShelf.getNoteUrl())) {
            stopPlay();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_SHELF)})
    public void updateBookShelf(BookShelfBean bookShelf) {
        if (bookShelfBean != null && TextUtils.equals(bookShelfBean.getNoteUrl(), bookShelf.getNoteUrl())) {
            bookShelfBean = bookShelf;
            mModel.updateBookShelf(bookShelfBean);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_INFO)})
    public void updateBookInfo(BookShelfBean bookShelf) {
        if (bookShelfBean != null && TextUtils.equals(bookShelfBean.getNoteUrl(), bookShelf.getNoteUrl())) {
            bookShelfBean.setBookInfoBean(bookShelf.getBookInfoBean());
            mModel.updateBookShelf(bookShelfBean);
            sendWhenAttach();
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.MEDIA_BUTTON)})
    public void onMediaButton(String command) {
        if (isPrepared) {
            if (isPause) {
                resumePlay();
            } else {
                pausePlay();
            }
        }
    }
}
