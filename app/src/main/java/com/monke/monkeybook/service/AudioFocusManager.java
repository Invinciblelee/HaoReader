package com.monke.monkeybook.service;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.help.RunMediaPlayer;

import static android.content.Context.AUDIO_SERVICE;

public abstract class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "AudioFocusManager";
    private AudioManager audioManager;
    private AudioFocusRequest mFocusRequest;
    private boolean isPausedByFocusLossTransient;

    public AudioFocusManager(Context context) {
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initFocusRequest();
        }
    }

    public boolean requestAudioFocus() {
        RunMediaPlayer.playSilentSound(ContextHolder.getContext());
        final int request;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mFocusRequest != null) {
            request = audioManager.requestAudioFocus(mFocusRequest);
        } else {
            request = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFocusRequest() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build();
    }

    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            // 重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (isPausedByFocusLossTransient) {
                    // 通话结束，恢复播放
                    onFocusGainFromFocusLossTransient();
                }else {
                    // 恢复音量
                    onFocusGain();
                }
                isPausedByFocusLossTransient = false;
                Logger.d(TAG, "重新获得焦点");
                break;
            // 永久丢失焦点，如被其他播放器抢占
            case AudioManager.AUDIOFOCUS_LOSS:
                onFocusLoss();
                abandonAudioFocus();
                Logger.d(TAG, "永久丢失焦点，如被其他播放器抢占");
                break;
            // 短暂丢失焦点，如来电
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                onFocusLossTransient();
                isPausedByFocusLossTransient = true;
                Logger.d(TAG, "短暂丢失焦点，如来电");
                break;
            // 瞬间丢失焦点，如通知
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // 音量减小为一半
                onFocusLossTransientCanDuck();
                Logger.d(TAG, "瞬间丢失焦点，如通知");
                break;
            default:
                break;
        }
    }

    protected abstract void onFocusGainFromFocusLossTransient();

    protected abstract void onFocusGain();

    protected abstract void onFocusLoss();

    protected abstract void onFocusLossTransient();

    protected abstract void onFocusLossTransientCanDuck();
}