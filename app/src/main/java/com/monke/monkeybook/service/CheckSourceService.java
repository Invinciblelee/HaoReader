package com.monke.monkeybook.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.OkHttpHelper;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.model.WebBookModelImpl;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;
import com.monke.monkeybook.view.activity.BookSourceActivity;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.help.RxBusTag.CHECK_SOURCE_STATE;

public class CheckSourceService extends Service {
    private static final int notificationId = 19901133;
    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";
    private static final String ActionOpenActivity = "openActivity";

    private List<BookSourceBean> bookSourceBeanList;
    private int threadsNum;
    private int checkIndex;
    private CompositeDisposable compositeDisposable;
    private ExecutorService executorService;
    private Scheduler scheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        threadsNum = AppConfigHelper.get().getInt(this.getString(R.string.pk_threads_num), 6);
        executorService = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
        bookSourceBeanList = BookSourceManager.getInstance().getAllBookSource();
        updateNotification(0);
        startCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionDoneService:
                        doneService();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 启动服务
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, CheckSourceService.class);
        intent.setAction(ActionStartService);
        context.startService(intent);
    }

    /**
     * 停止服务
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, CheckSourceService.class);
        context.stopService(intent);
    }

    private void doneService() {
        RxBus.get().post(CHECK_SOURCE_STATE, -1);
        compositeDisposable.dispose();
        stopSelf();
    }

    /**
     * 更新通知
     */
    private void updateNotification(int state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_network_check_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                .setContentTitle(getString(R.string.check_book_source))
                .setContentText(String.format(getString(R.string.progress_show), state, bookSourceBeanList.size()))
                .setContentIntent(getActivityPendingIntent(ActionOpenActivity));
        builder.addAction(R.drawable.ic_stop_white_24dp, getString(R.string.cancel), getThisServicePendingIntent(ActionDoneService));
        builder.setProgress(bookSourceBeanList.size(), state, false);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    private PendingIntent getActivityPendingIntent(String actionStr) {
        Intent intent = new Intent(this, BookSourceActivity.class);
        intent.setAction(actionStr);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getThisServicePendingIntent(String actionStr) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(actionStr);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startCheck() {
        if (bookSourceBeanList != null && bookSourceBeanList.size() > 0) {
            RxBus.get().post(CHECK_SOURCE_STATE, 0);
            checkIndex = -1;
            for (int i = 0; i < threadsNum; i++) {
                nextCheck();
            }
        }
    }

    private synchronized void nextCheck() {
        checkIndex++;
        if (checkIndex > threadsNum) {
            RxBus.get().post(CHECK_SOURCE_STATE, checkIndex - threadsNum);
            updateNotification(checkIndex - threadsNum);
        }

        if (checkIndex < bookSourceBeanList.size()) {
            CheckSource checkSource = new CheckSource(bookSourceBeanList.get(checkIndex));
            checkSource.startCheck();
        } else {
            if (checkIndex >= bookSourceBeanList.size() + threadsNum - 1) {
                doneService();
            }
        }
    }

    private class CheckSource {
        CheckSource checkSource;
        BookSourceBean sourceBean;

        CheckSource(BookSourceBean sourceBean) {
            checkSource = this;
            this.sourceBean = sourceBean;
        }

        private void startCheck() {
            if (!TextUtils.isEmpty(sourceBean.getCheckUrl())) {
                try {
                    new URL(sourceBean.getCheckUrl());
                    BookShelfBean bookShelfBean = new BookShelfBean();
                    bookShelfBean.setTag(sourceBean.getBookSourceUrl());
                    bookShelfBean.setNoteUrl(sourceBean.getCheckUrl());
                    bookShelfBean.setFinalDate(System.currentTimeMillis());
                    bookShelfBean.setDurChapter(0);
                    bookShelfBean.setDurChapterPage(0);
                    WebBookModelImpl.getInstance().getBookInfo(bookShelfBean)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .timeout(30, TimeUnit.SECONDS)
                            .subscribe(getObserver());
                } catch (Exception exception) {
                    sourceBean.setBookSourceGroup("失效");
                    BookSourceManager.getInstance().addBookSource(sourceBean);
                    BookSourceManager.getInstance().refreshBookSource();
                    nextCheck();
                }
            } else {
                try {
                    new URL(sourceBean.getBookSourceUrl());
                    OkHttpHelper.getInstance()
                            .createService(sourceBean.getBookSourceUrl(), IHttpGetApi.class)
                            .getWebContent(sourceBean.getBookSourceUrl(), AnalyzeHeaders.getMap(null))
                            .subscribeOn(scheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .timeout(30, TimeUnit.SECONDS)
                            .subscribe(getObserver());
                } catch (Exception e) {
                    sourceBean.setBookSourceGroup("失效");
                    BookSourceManager.getInstance().addBookSource(sourceBean);
                    BookSourceManager.getInstance().refreshBookSource();
                    nextCheck();
                }
            }
        }

        private Observer<Object> getObserver() {
            return new Observer<Object>() {
                @Override
                public void onSubscribe(Disposable d) {
                    compositeDisposable.add(d);
                }

                @Override
                public void onNext(Object value) {
                    if (Objects.equals(sourceBean.getBookSourceGroup(), "失效")) {
                        sourceBean.setBookSourceGroup("");
                        BookSourceManager.getInstance().addBookSource(sourceBean);
                        BookSourceManager.getInstance().refreshBookSource();
                    }
                    nextCheck();
                }

                @Override
                public void onError(Throwable e) {
                    sourceBean.setBookSourceGroup("失效");
                    sourceBean.setSerialNumber(10000 + checkIndex);
                    BookSourceManager.getInstance().addBookSource(sourceBean);
                    BookSourceManager.getInstance().refreshBookSource();
                    nextCheck();
                }

                @Override
                public void onComplete() {
                    checkSource = null;
                }
            };
        }
    }
}
