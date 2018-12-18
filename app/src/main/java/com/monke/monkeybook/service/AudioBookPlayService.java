package com.monke.monkeybook.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.monke.monkeybook.BitIntentDataManager;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.AudioSniffer;

import java.io.IOException;

public class AudioBookPlayService extends Service {

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private BookShelfBean bookShelfBean;

    public static void start(Context context, BookShelfBean bookShelfBean) {
        Intent intent = new Intent(context, AudioBookPlayService.class);
        intent.setAction("start");
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelfBean.copy());
        context.startService(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (intent.getAction()) {
                case "start":
                    String key = intent.getStringExtra("data_key");
                    bookShelfBean = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
                    BitIntentDataManager.getInstance().cleanData(key);
                    ensureChapterList();
                    break;
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void ensureChapterList() {
        AudioSniffer sniffer = new AudioSniffer(AudioBookPlayService.this, bookShelfBean.getTag());
        sniffer.setOnSniffListener(new AudioSniffer.OnSniffListener() {
            @Override
            public void onResult(String url) {
                play(url);
            }

            @Override
            public void onError() {

            }
        });
        sniffer.start("http://www.qqjt5.com/py/21161-1-0.html");
//        WebBookModelImpl.getInstance().getChapterList(bookShelfBean)
//                .subscribeOn(Schedulers.newThread())
//                .doOnNext(bookShelfBean -> {
//                    // 存储章节到数据库
//                    bookShelfBean.setHasUpdate(false);
//                    bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
//                    if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
//                        BookshelfHelp.saveBookToShelf(bookShelfBean);
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleObserver<BookShelfBean>() {
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(BookShelfBean bookShelfBean) {
//                        if (!bookShelfBean.realChapterListEmpty()) {
//                            AudioSniffer sniffer = new AudioSniffer(AudioBookPlayService.this, bookShelfBean.getTag());
//                            sniffer.setOnSniffListener(new AudioSniffer.OnSniffListener() {
//                                @Override
//                                public void onResult(String url) {
//                                    play(url);
//                                }
//
//                                @Override
//                                public void onError() {
//
//                                }
//                            });
//                            Log.e("TAG", bookShelfBean.getChapter(1).getDurChapterUrl());
//                            sniffer.start(bookShelfBean.getChapter(1).getDurChapterUrl());
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//                });
    }


    private void play(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSelf();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
