package com.monke.monkeybook.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import com.monke.basemvplib.NetworkUtil;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.AudioBookPlayModelImpl;
import com.monke.monkeybook.model.content.exception.BookSourceException;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;
import com.monke.monkeybook.utils.DensityUtil;
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
    public static final String ACTION_CHANGE_SOURCE = "ACTION_CHANGE_SOURCE";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_REFRESH_CHAPTER = "ACTION_REFRESH_CHAPTER";
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
    public static final String ACTION_SEEK_ENABLED = "ACTION_SEEK_ENABLED";

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private static final int notificationId = 19901122;

    public static boolean running;

    private BookShelfBean bookShelfBean;

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private MediaSessionCompat mediaSessionCompat;
    private BroadcastReceiver broadcastReceiver;
    private AudioFocusManager focusManager;

    private boolean isPrepared;
    private boolean isPause;
    private boolean isLoading;
    private boolean isError;

    private int targetPosition;
    private int progress;
    private int duration;

    private int timerMinute;
    private int timerUntilFinish;

    private AudioBookPlayModelImpl mModel;

    private ScheduledExecutorService mTimer;
    private ScheduledExecutorService mAlertTimer;

    public static void start(Context context, BookShelfBean bookShelfBean, boolean resume) {
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_START);
        intent.putExtra("resume", resume);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelfBean == null ? null : bookShelfBean.copy());
        context.startService(intent);
    }

    public static void start(Context context) {
        start(context, null, false);
    }

    public static void changeSource(Context context, SearchBookBean searchBookBean) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_CHANGE_SOURCE);
        intent.putExtra("searchBook", searchBookBean);
        context.startService(intent);
    }

    public static void play(Context context, ChapterBean chapterBean) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra("chapter", chapterBean);
        context.startService(intent);
    }

    public static void refresh(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_REFRESH_CHAPTER);
        context.startService(intent);
    }

    public static void addShelf(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_ADD_SHELF);
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

    public static void stopNotShelfExists(Context context) {
        if (!running) return;
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction(ACTION_STOP_NOT_IN_SHELF);
        context.startService(intent);
    }

    public static void stop(Context context) {
        if (!running) return;
        try {
            Intent intent = new Intent(context, AudioBookPlayService.class);
            context.stopService(intent);
        } catch (Exception ignore) {
        }
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
        initMediaSession();
        initBroadcastReceiver();
        updateMediaSessionPlaybackState();
        updateNotification();

        focusManager = new AudioPlayFocusManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (intent.getAction()) {
                    case ACTION_START:
                        onActionStart(intent);
                        break;
                    case ACTION_CHANGE_SOURCE:
                        SearchBookBean searchBookBean = intent.getParcelableExtra("searchBook");
                        changeSource(searchBookBean);
                        break;
                    case ACTION_PLAY:
                        ChapterBean chapterBean = intent.getParcelableExtra("chapter");
                        if (mModel != null && mModel.isPrepared()) {
                            resetPlayer(true);
                            mModel.playChapter(chapterBean, true);
                        }
                        break;
                    case ACTION_REFRESH_CHAPTER:
                        if (mModel != null && mModel.isPrepared()) {
                            resetPlayer(true);
                            mModel.resetChapter();
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
                        if (timerMinute == -1) {
                            cancelAlarmTimer();
                        } else {
                            timerUntilFinish = timerMinute;
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
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionStart(Intent intent) {
        running = true;
        final boolean resume = intent.getBooleanExtra("resume", true);
        final String key = intent.getStringExtra("data_key");
        final BookShelfBean bookShelf = BitIntentDataManager.getInstance().getData(key, null);
        BitIntentDataManager.getInstance().cleanData(key);
        if (bookShelf != null) {
            if (bookShelfBean != null) {
                if (!bookShelfBean.realChapterListEmpty() && TextUtils.equals(bookShelf.getNoteUrl(), bookShelfBean.getNoteUrl())) {
                    sendWhenAttach(resume);
                    return;
                }
                bookShelfBean = bookShelf;
                initModel();
            } else {
                bookShelfBean = bookShelf;
                initModel();
            }
        } else {
            sendWhenAttach(resume);
        }
        updateNotification();
    }

    private void initModel() {
        resetModel();

        mModel = createModel(bookShelfBean);
        mModel.registerPlayCallback(new IAudioBookPlayModel.PlayCallback() {

            @Override
            public void onStart() {
                setPause(false);
                setLoading(true);
            }

            @Override
            public void onPrepare(ChapterBean chapterBean) {
                AudioPlayInfo info = AudioPlayInfo.start(chapterBean);
                info.setProgress(progress);
                info.setDuration(duration);
                sendEvent(ACTION_PREPARE, info);
                updateNotification();
            }

            @Override
            public void onPlay(ChapterBean chapterBean) {
                targetPosition = chapterBean.getStart();
                startPlay(chapterBean.getDurChapterPlayUrl());
                updateNotification();
            }

            @Override
            public void onError(Throwable error) {
                toastError(error, "播放失败，无法获取播放链接");
                sendWhenError();
            }
        });

        initChapterList();
    }

    private void initChapterList() {
        mModel.ensureChapterList(new IAudioBookPlayModel.Callback<BookShelfBean>() {
            @Override
            public void onSuccess(BookShelfBean data) {
                sendEvent(ACTION_START, AudioPlayInfo.start(timerMinute, data.getDurChapter(), data.getChapterList()));
            }

            @Override
            public void onError(Throwable error) {
                toastError(error, "播放目录获取失败");
                sendWhenError();
            }
        });
    }

    private void changeSource(SearchBookBean searchBookBean) {
        if (mModel != null && searchBookBean != null) {
            mModel.changeSource(searchBookBean, new IAudioBookPlayModel.Callback<BookShelfBean>() {
                @Override
                public void onSuccess(BookShelfBean data) {
                    bookShelfBean = data;
                    resetPlayer(true);
                    setPause(false);
                    sendEvent(ACTION_ATTACH, AudioPlayInfo.attach(data.getBookInfoBean()));
                    sendEvent(ACTION_START, AudioPlayInfo.start(timerMinute, data.getDurChapter(), data.getChapterList()));
                }

                @Override
                public void onError(Throwable error) {
                    toastError(error, "换源失败，请重新换源");
                    setLoading(false);
                }
            });
        }
    }

    private void initMediaPlayer() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            isError = false;

            if (targetPosition != 0) {
                mp.seekTo(targetPosition);
                targetPosition = 0;
            }

            if (requestFocus()) {
                mp.start();
            }

            setPause(false);

            cancelProgressTimer();
            setProgressTimer();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isPrepared && !nextPlay()) {
                setPause(true);
            }
        });

        mediaPlayer.setOnInfoListener((mp, what, extra) -> {
            Logger.d(TAG, "audio info --> " + what + "  " + extra);
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                setLoading(true);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                setLoading(false);
            }
            return true;
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Logger.d(TAG, "audio error --> " + what + "  " + extra);
            isPrepared = false;
            if (what != -38) {
                toastError(null, "播放失败，请重试");
            }
            sendWhenError();
            return true;
        });
    }

    /**
     * 初始化MediaSession
     */
    private void initMediaSession() {
        ComponentName mComponent = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mComponent);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(this, 0,
                mediaButtonIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mediaSessionCompat = new MediaSessionCompat(this, TAG, mComponent, mediaButtonReceiverPendingIntent);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return MediaButtonIntentReceiver.handleIntent(mediaButtonEvent);
            }
        });
        mediaSessionCompat.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
        mediaSessionCompat.setActive(true);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    pausePlay();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void updateMediaSessionPlaybackState() {
        mediaSessionCompat.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(isPause ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_PLAYING,
                                progress, 1)
                        .build());
    }

    private void sendWhenError() {
        isError = true;
        setPause(true);
        setLoading(false);
        sendEvent(ACTION_SEEK_ENABLED, AudioPlayInfo.seekEnabled(false));
    }

    private void sendWhenAttach(boolean resume) {
        if (resume) {
            resumePlay();
        }
        if (bookShelfBean != null) {
            AudioPlayInfo info = AudioPlayInfo.attach(bookShelfBean.getBookInfoBean());
            info.setPause(isPause);
            info.setLoading(isLoading);
            info.setTimerMinute(timerMinute);
            info.setTimerMinuteUntilFinish(timerUntilFinish);
            if (!bookShelfBean.realChapterListEmpty()) {
                info.setProgress(progress);
                info.setDuration(duration);
                info.setChapterBeans(bookShelfBean.getChapterList());
                info.setDurChapterIndex(bookShelfBean.getDurChapter());
            }
            sendEvent(ACTION_ATTACH, info);
        }
    }

    private void toastError(Throwable e, String errorMsg) {
        if (e instanceof BookSourceException) {
            ToastUtils.toast(AudioBookPlayService.this, e.getMessage());
        } else if (NetworkUtil.isNetworkAvailable()) {
            ToastUtils.toast(AudioBookPlayService.this, errorMsg);
        } else {
            ToastUtils.toast(AudioBookPlayService.this, "网络连接失败");
        }
    }

    private boolean requestFocus() {
        return focusManager != null && focusManager.requestAudioFocus();
    }

    private void resetModel() {
        if (mModel != null) {
            mModel.saveProgress(progress, duration);
            mModel.destroy();
        }
        sendWhenAttach(false);
        resetPlayer(true);
    }

    private void resetPlayer(boolean resetProgress) {
        if (resetProgress) {
            progress = 0;
            duration = 0;
        }
        isPrepared = false;
        mediaPlayer.reset();
        sendEvent(ACTION_SEEK_ENABLED, AudioPlayInfo.seekEnabled(false));

    }

    private boolean nextPlay() {
        if (mModel == null) return false;

        if (!mModel.hasNext()) {
            ToastUtils.toast(this, "没有下一章了");
            return false;
        } else {
            resetPlayer(true);

            if (isPause) {
                setPause(false);
            }
            Logger.d(TAG, "audio --> nextPlay");
            mModel.playNext();
            return true;
        }
    }

    private boolean previousPlay() {
        if (mModel == null) return false;

        if (!mModel.hasPrevious()) {
            ToastUtils.toast(this, "没有上一章了");
            return false;
        } else {
            resetPlayer(true);

            if (isPause) {
                setPause(false);
            }
            Logger.d(TAG, "audio --> previousPlay");
            mModel.playPrevious();
            return true;
        }
    }

    private void setLoading(boolean loading) {
        if (isLoading != loading) {
            isLoading = loading;
            sendEvent(ACTION_LOADING, AudioPlayInfo.loading(isLoading));
        }
    }

    private void setPause(boolean pause) {
        if (isPause != pause) {
            isPause = pause;
            if (pause) {
                sendEvent(ACTION_PAUSE, AudioPlayInfo.empty());
            } else {
                sendEvent(ACTION_RESUME, AudioPlayInfo.empty());
            }
            updateNotification();
            updateMediaSessionPlaybackState();
        }
    }

    private void pausePlay() {
        if (!isPrepared) return;

        if (!isPause) {
            setPause(true);
            mediaPlayer.pause();
        }
    }

    private void resumePlay() {
        if (retryOnError()) return;

        if (!isPrepared) return;

        if (isPause && requestFocus()) {
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
            setPause(false);
            resetPlayer(false);
            mModel.retryPlay(progress);//按上次进度继续播放
            return true;
        }
        return false;
    }

    private void seekTo(int position) {
        if (!isPrepared || isError) return;
        if (isPause) {
            resumePlay();
        }
        mediaPlayer.seekTo(position);
    }

    private void startPlay(String url) {
        try {
            Logger.d(TAG, "audio --> progress: " + url);
            mediaPlayer.reset();
            if (useCacheSource() && mModel.inBookShelf()) {
                String proxyUrl = MApplication.getProxyCacheServer(this).getProxyUrl(url);
                mediaPlayer.setDataSource(proxyUrl);
            } else {
                mediaPlayer.setDataSource(url);
            }
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Logger.e(TAG, "startPlay", e);
            ToastUtils.toast(this, "播放失败");
            sendWhenError();
        }
    }

    private void stopPlay() {
        stopSelf();
        running = false;
    }

    private boolean useCacheSource() {
        return AppConfigHelper.get().getBoolean(getString(R.string.pk_audio_cache), false);
    }

    private void setProgressTimer() {
        if (mTimer == null || mTimer.isShutdown()) {
            mTimer = Executors.newSingleThreadScheduledExecutor();
            mTimer.scheduleAtFixedRate(() -> {
                if (mediaPlayer.isPlaying()) {
                    int dur = mediaPlayer.getDuration();
                    int pro = mediaPlayer.getCurrentPosition();
                    if (dur != 0) {
                        duration = dur;
                        progress = pro;
                        sendEvent(ACTION_PROGRESS, AudioPlayInfo.progress(progress, duration));
                    }
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
        }

        sendEvent(ACTION_SEEK_ENABLED, AudioPlayInfo.seekEnabled(true));
        setLoading(false);
        updateNotification();
    }

    private void cancelProgressTimer() {
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

    private void setAlarmTimer() {
        if (mAlertTimer == null || mAlertTimer.isShutdown()) {
            mAlertTimer = Executors.newSingleThreadScheduledExecutor();
            mAlertTimer.scheduleAtFixedRate(() -> {
                Intent intent = new Intent(AudioBookPlayService.this, AudioBookPlayService.class);
                intent.setAction(ACTION_TIMER_PROGRESS);
                intent.putExtra("minute", -1);
                startService(intent);
            }, 60000, 60000, TimeUnit.MILLISECONDS);
        }

        sendEvent(ACTION_TIMER_PROGRESS, AudioPlayInfo.timerDown(timerUntilFinish));
        updateNotification();
    }

    private void updateAlarmTimer(int minute) {
        timerUntilFinish = timerUntilFinish + minute;
        int maxTimeMinute = 90;
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
        final String coverUrl = bookShelfBean == null ? null : bookShelfBean.getBookInfoBean().getRealCoverUrl();
        final int dimen = DensityUtil.dp2px(this, 128);
        Glide.with(this)
                .asBitmap()
                .load(coverUrl)
                .into(new RequestFutureTarget<Bitmap>(dimen, dimen) {
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

    private void showNotification(Bitmap cover) {
        final String contentTitle;
        final String name = bookShelfBean == null ? getString(R.string.app_name) : bookShelfBean.getBookInfoBean().getName();
        if (timerUntilFinish > 0) {
            contentTitle = String.format(Locale.getDefault(), "(%d分钟)%s", timerUntilFinish, name);
        } else {
            contentTitle = name;
        }
        final String contentText = bookShelfBean == null ? null : bookShelfBean.getDisplayDurChapterName();
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
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, getThisServicePendingIntent(ACTION_STOP));

        remoteViews.setImageViewBitmap(R.id.iv_cover, cover);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdAudioBook)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(contentTitle)
                .setContentText(TextUtils.isEmpty(contentText) ? "即将为您播放" : contentText)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setCustomBigContentView(remoteViews)
                .setContentIntent(getAudioActivityPendingIntent());
        startForeground(notificationId, builder.build());
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getAudioActivityPendingIntent() {
        Intent intent = new Intent(this, AudioBookPlayActivity.class);
        intent.putExtra("resume", false);
        return PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private AudioBookPlayModelImpl createModel(BookShelfBean bookShelfBean) {
        return new AudioBookPlayModelImpl(bookShelfBean);
    }

    private void sendEvent(String action, AudioPlayInfo info) {
        if (info != null) {
            info.setAction(action);
            RxBus.get().post(RxBusTag.AUDIO_PLAY, info);
        }
    }

    private void unregisterMediaButton() {
        if (mediaSessionCompat != null) {
            mediaSessionCompat.setCallback(null);
            mediaSessionCompat.setActive(false);
            mediaSessionCompat.release();
        }

        if (focusManager != null) {
            focusManager.abandonAudioFocus();
        }
    }

    @Override
    public void onDestroy() {
        RxBus.get().unregister(this);
        if (mModel != null) {
            mModel.saveProgress(progress, duration);
            mModel.destroy();
        }
        running = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        cancelProgressTimer();
        cancelAlarmTimer();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        unregisterMediaButton();
        sendEvent(ACTION_STOP, AudioPlayInfo.empty());
        super.onDestroy();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.HAD_REMOVE_BOOK)})
    public void removeBookShelf(BookShelfBean bookShelf) {
        if (bookShelfBean != null && !bookShelf.isFlag() && TextUtils.equals(bookShelfBean.getNoteUrl(), bookShelf.getNoteUrl())) {
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
            sendWhenAttach(false);
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.CHANGE_SOURCE)})
    public void changeSource(BookShelfBean bookShelf) {
        if (bookShelfBean != null) {
            bookShelfBean = bookShelf;
            initModel();
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

    private class AudioPlayFocusManager extends AudioFocusManager {

        private AudioPlayFocusManager(Context context) {
            super(context);
        }

        @Override
        protected void onFocusGainFromFocusLossTransient() {

        }

        @Override
        protected void onFocusGain() {

        }

        @Override
        protected void onFocusLoss() {
            pausePlay();
        }

        @Override
        protected void onFocusLossTransient() {
            pausePlay();
        }

        @Override
        protected void onFocusLossTransientCanDuck() {
            resumePlay();
        }
    }
}
