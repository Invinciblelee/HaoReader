package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.utils.DensityUtil;
import com.monke.monkeybook.view.activity.ReadBookActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2018/1/2.
 * 朗读服务
 */
public class ReadAloudService extends Service {
    public static final int PLAY = 1;
    public static final int STOP = 0;
    public static final int PAUSE = 2;
    public static final int NEXT = 3;
    public static final String ActionMediaButton = "mediaButton";
    public static final String ActionNewReadAloud = "newReadAloud";
    public static final String ActionDoneService = "doneService";
    public static final String ActionPauseService = "pauseService";
    public static final String ActionResumeService = "resumeService";
    private static final String TAG = ReadAloudService.class.getSimpleName();
    private static final String ActionReadActivity = "readActivity";
    private static final String ActionSetTimer = "updateTimer";
    private static final int notificationId = 19901144;
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;
    public static Boolean running = false;
    private TextToSpeech textToSpeech;
    private Boolean ttsInitSuccess = false;
    private Boolean speak = true;
    private Boolean pause = false;
    private List<String> contentList = new ArrayList<>();
    private int nowSpeak;
    private int timerMinute = 0;
    private boolean timerEnable = false;
    private AudioFocusManager focusManager;
    private ScheduledExecutorService mTimer;
    private MediaSessionCompat mediaSessionCompat;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preference;
    private int speechRate;
    private String title;
    private String text;
    private String cover;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        preference = this.getSharedPreferences("CONFIG", 0);
        textToSpeech = new TextToSpeech(this, new TTSListener());
        focusManager = new ReadAloudFocusManager(this);

        initMediaSession();
        initBroadcastReceiver();
        mediaSessionCompat.setActive(true);
        updateMediaSessionPlaybackState();
        updatePlayState();

        AudioBookPlayService.stop(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionDoneService:
                        stopSelf();
                        break;
                    case ActionPauseService:
                        pauseReadAloud(true);
                        break;
                    case ActionResumeService:
                        resumeReadAloud();
                        break;
                    case ActionSetTimer:
                        updateTimer(intent.getIntExtra("minute", 10));
                        break;
                    case ActionNewReadAloud:
                        newReadAloud(intent.getStringExtra("content"),
                                intent.getBooleanExtra("aloudButton", false),
                                intent.getStringExtra("title"),
                                intent.getStringExtra("text")
                                , intent.getStringExtra("cover"));
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void newReadAloud(String content, Boolean aloudButton, String title, String text, String cover) {
        if (content == null) {
            stopSelf();
            return;
        }
        this.cover = cover;
        this.title = title;
        this.text = text;
        nowSpeak = 0;
        contentList.clear();
        String[] splitSpeech = content.split("\n");
        for (String aSplitSpeech : splitSpeech) {
            if (!isEmpty(aSplitSpeech)) {
                contentList.add(aSplitSpeech);
            }
        }
        running = true;
        if (aloudButton || speak) {
            speak = false;
            pause = false;
            playTTS();
        }
    }

    public void playTTS() {
        if (contentList.size() < 1) {
            RxBus.get().post(RxBusTag.ALOUD_STATE, NEXT);
            return;
        }
        if (ttsInitSuccess && !speak && requestFocus()) {
            speak = !speak;
            RxBus.get().post(RxBusTag.ALOUD_STATE, PLAY);
            updatePlayState();
            initSpeechRate();
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "content");
            for (int i = nowSpeak; i < contentList.size(); i++) {
                if (i == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_FLUSH, map);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, null, "content");
                    } else {
                        textToSpeech.speak(contentList.get(i), TextToSpeech.QUEUE_ADD, map);
                    }
                }
            }
        }
    }

    private void initSpeechRate() {
        if (speechRate != preference.getInt("speechRate", 10) && !preference.getBoolean("speechRateFollowSys", true)) {
            speechRate = preference.getInt("speechRate", 10);
            float speechRateF = (float) speechRate / 10;
            textToSpeech.setSpeechRate(speechRateF);
        }
    }

    /**
     * 朗读
     */
    public static void play(Context context, Boolean aloudButton, String content, String title, String text, String cover) {
        Intent readAloudIntent = new Intent(context, ReadAloudService.class);
        readAloudIntent.setAction(ActionNewReadAloud);
        readAloudIntent.putExtra("aloudButton", aloudButton);
        readAloudIntent.putExtra("content", content);
        readAloudIntent.putExtra("title", title);
        readAloudIntent.putExtra("text", text);
        readAloudIntent.putExtra("cover", cover);
        context.startService(readAloudIntent);
    }

    /**
     * @param context 停止
     */
    public static void stop(Context context) {
        if (running) {
            running = false;
            Intent intent = new Intent(context, ReadAloudService.class);
            context.stopService(intent);
        }
    }

    /**
     * @param context 暂停
     */
    public static void pause(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionPauseService);
            context.startService(intent);
        }
    }

    /**
     * @param context 继续
     */
    public static void resume(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionResumeService);
            context.startService(intent);
        }
    }

    public static void setTimer(Context context) {
        if (running) {
            Intent intent = new Intent(context, ReadAloudService.class);
            intent.setAction(ActionSetTimer);
            context.startService(intent);
        }
    }

    /**
     * @param pause true 暂停, false 失去焦点
     */
    private void pauseReadAloud(Boolean pause) {
        this.pause = pause;
        speak = false;
        updatePlayState();
        updateMediaSessionPlaybackState();
        textToSpeech.stop();
        RxBus.get().post(RxBusTag.ALOUD_STATE, PAUSE);
    }

    /**
     * 恢复朗读
     */
    private void resumeReadAloud() {
        updateTimer(0);
        pause = false;
        playTTS();
    }

    private void updateTimer(int minute) {
        timerMinute = timerMinute + minute;
        int maxTimeMinute = 60;
        if (timerMinute > maxTimeMinute) {
            timerEnable = false;
            cancelTimer();
            timerMinute = 0;
            updatePlayState();
        } else if (timerMinute <= 0) {
            if (timerEnable) {
                cancelTimer();
                stopSelf();
            }
        } else {
            timerEnable = true;
            updatePlayState();
            setTimer();
        }
    }

    private void setTimer() {
        if (mTimer == null || mTimer.isShutdown()) {
            mTimer = Executors.newSingleThreadScheduledExecutor();
            mTimer.scheduleAtFixedRate(() -> {
                if (!pause) {
                    Intent setTimerIntent = new Intent(getApplicationContext(), ReadAloudService.class);
                    setTimerIntent.setAction(ActionSetTimer);
                    setTimerIntent.putExtra("minute", -1);
                    startService(setTimerIntent);
                }
            }, 60000, 60000, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

    private PendingIntent getReadBookActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.setAction(actionStr);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 更新通知
     */
    private void updateNotification() {
        final int dimen = DensityUtil.dp2px(this, 128);
        Glide.with(this)
                .asBitmap()
                .load(cover)
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        cancelTimer();
        clearTTS();
        unregisterMediaButton();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        RxBus.get().post(RxBusTag.ALOUD_STATE, STOP);
    }

    private void showNotification(Bitmap cover) {
        final String contentTitle;
        if (timerMinute > 0 && timerMinute <= 60) {
            contentTitle = String.format(Locale.getDefault(), "(%d分钟)%s", timerMinute, this.title);
        } else {
            contentTitle = this.title;
        }
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_aloud_notification);
        remoteViews.setTextViewText(R.id.tv_title, contentTitle);
        remoteViews.setTextViewText(R.id.tv_content, text);
        if (pause) {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.ic_play_white_24dp);
            remoteViews.setOnClickPendingIntent(R.id.btn_pause, getThisServicePendingIntent(ActionResumeService));
        } else {
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.ic_pause_white_24dp);
            remoteViews.setOnClickPendingIntent(R.id.btn_pause, getThisServicePendingIntent(ActionPauseService));
        }

        remoteViews.setOnClickPendingIntent(R.id.btn_stop, getThisServicePendingIntent(ActionDoneService));
        remoteViews.setOnClickPendingIntent(R.id.btn_timer, getThisServicePendingIntent(ActionSetTimer));

        remoteViews.setImageViewBitmap(R.id.iv_cover, cover);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdReadAloud)
                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(contentTitle)
                .setContentText(text)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setCustomBigContentView(remoteViews)
                .setContentIntent(getReadBookActivityPendingIntent(ActionReadActivity));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    private void updateTimer() {
        String timerDesc;
        if (pause) {
            timerDesc = getString(R.string.read_aloud_pause);
        } else if (timerMinute > 0 && timerMinute <= 60) {
            timerDesc = getString(R.string.read_aloud_timer, timerMinute);
        } else {
            timerDesc = getString(R.string.read_aloud_t);
        }
        RxBus.get().post(RxBusTag.ALOUD_TIMER, timerDesc);
    }

    private void updatePlayState() {
        updateTimer();
        updateNotification();
    }

    private void clearTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
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

    /**
     * @return 音频焦点
     */
    private boolean requestFocus() {
        return focusManager != null && focusManager.requestAudioFocus();
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
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    pauseReadAloud(true);
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
                        .setState(speak ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                                nowSpeak, 1)
                        .build());
    }

    private void toTTSSetting() {
        //跳转到文字转语音设置界面
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    public class MyBinder extends Binder {
        public ReadAloudService getService() {
            return ReadAloudService.this;
        }
    }

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    RxBus.get().post(RxBusTag.ALOUD_MSG, getString(R.string.tts_fix));
                    //先停止朗读服务方便用户设置好后的重试
                    ReadAloudService.stop(ReadAloudService.this);
                    //跳转到文字转语音设置界面
                    toTTSSetting();
                } else {
                    textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
                    ttsInitSuccess = true;
                    playTTS();
                }
            } else {
                RxBus.get().post(RxBusTag.ALOUD_MSG, getString(R.string.tts_init_failed));
                stopSelf();
            }
        }
    }

    /**
     * 朗读监听
     */
    private class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {
            RxBus.get().post(RxBusTag.ALOUD_INDEX, nowSpeak);
            updateMediaSessionPlaybackState();
        }

        @Override
        public void onDone(String s) {
            nowSpeak = nowSpeak + 1;
            if (nowSpeak >= contentList.size()) {
                RxBus.get().post(RxBusTag.ALOUD_STATE, NEXT);
            }
        }

        @Override
        public void onError(String s) {
            pauseReadAloud(true);
            RxBus.get().post(RxBusTag.ALOUD_STATE, PAUSE);
        }
    }

    private class ReadAloudFocusManager extends AudioFocusManager {

        private ReadAloudFocusManager(Context context) {
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
            if (!pause) {
                pauseReadAloud(false);
            }
        }

        @Override
        protected void onFocusLossTransient() {
            if (!pause) {
                pauseReadAloud(false);
            }
        }

        @Override
        protected void onFocusLossTransientCanDuck() {
            if (!pause) {
                resumeReadAloud();
            }
        }
    }


}
