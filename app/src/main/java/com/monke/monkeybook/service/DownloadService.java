//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.DownloadInfo;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.impl.IDownloadTask;
import com.monke.monkeybook.model.task.DownloadTaskImpl;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {
    public static final String cancelAction = "cancelAction";
    public static final String addDownloadAction = "addDownload";
    public static final String removeDownloadAction = "removeDownloadAction";
    public static final String progressDownloadAction = "progressDownloadAction";
    public static final String obtainDownloadListAction = "obtainDownloadListAction";
    public static final String finishDownloadAction = "finishDownloadAction";

    private static final int EVENT_START = -1;
    private static final int EVENT_COMPLETE = 0;
    private static final int EVENT_ERROR = 1;
    private static final int EVENT_CANCEL = 2;

    private NotificationManagerCompat managerCompat;

    public static boolean running = false;

    private ExecutorService executor;
    private Scheduler scheduler;
    private int threadsNum;

    private final SparseArray<IDownloadTask> downloadTasks = new SparseArray<>();

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        threadsNum = AppConfigHelper.get().getInt(this.getString(R.string.pk_threads_num), 4);
        executor = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executor);
        managerCompat = NotificationManagerCompat.from(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        executor.shutdown();
        managerCompat.cancelAll();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                finishSelf();
            } else {
                switch (action) {
                    case addDownloadAction:
                        running = true;
                        DownloadBookBean downloadBook = intent.getParcelableExtra("downloadBook");
                        if (downloadBook != null) {
                            addDownload(downloadBook);
                        }
                        break;
                    case removeDownloadAction:
                        if (intent.hasExtra("noteUrl")) {
                            removeDownload(intent.getStringExtra("noteUrl"));
                        } else {
                            removeDownload(intent.getIntExtra("taskId", -1));
                        }
                        break;
                    case cancelAction:
                        cancelDownload();
                        break;
                    case obtainDownloadListAction:
                        refreshDownloadList();
                        break;

                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addDownload(DownloadBookBean downloadBook) {
        if (checkDownloadTaskExist(downloadBook)) {
            toast("下载任务已存在");
            return;
        }
        new DownloadTaskImpl(downloadBook) {
            @Override
            public void onDownloadPrepared(DownloadBookBean downloadBook) {
                addDownload(this);

                sendDownloadBook(addDownloadAction, downloadBook);
                toastOnEvent(EVENT_START, downloadBook);
            }

            @Override
            public void onDownloadProgress(String bookName, ChapterBean chapterBean) {
                isProgress(getId(), getWhen(), bookName, chapterBean);
            }

            @Override
            public void onDownloadChange(DownloadBookBean downloadBook) {
                sendDownloadBook(progressDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadError(DownloadBookBean downloadBook) {
                toastOnEvent(EVENT_ERROR, downloadBook);
                deleteDownload(this, true);
            }

            @Override
            public void onDownloadComplete(DownloadBookBean downloadBook) {
                toastOnEvent(EVENT_COMPLETE, downloadBook);
                deleteDownload(this, true);
            }

            @Override
            public void onDownloadCancel(DownloadBookBean downloadBook) {
                toastOnEvent(EVENT_CANCEL, downloadBook);
                deleteDownload(this, true);
            }
        };


    }

    private void toastOnEvent(int action, DownloadBookBean downloadBook) {
        switch (action) {
            case EVENT_START:
                toast(String.format(Locale.getDefault(), "%s：下载任务添加成功", downloadBook.getName()));
                break;
            case EVENT_COMPLETE:
                if (downloadBook.isValid()) {
                    toast(String.format(Locale.getDefault(), "%s：共下载%d章", downloadBook.getName(), downloadBook.getSuccessCount()));
                } else {
                    toast(String.format(Locale.getDefault(), "%s：所有章节已缓存", downloadBook.getName()));
                }
                break;
            case EVENT_ERROR:
                toast(String.format(Locale.getDefault(), "%s：下载失败", downloadBook.getName()));
                break;
            case EVENT_CANCEL:
                toast(String.format(Locale.getDefault(), "%s：下载取消", downloadBook.getName()));
                break;
        }
    }

    private void cancelDownload() {
        synchronized (downloadTasks) {
            for (int i = 0; i < downloadTasks.size(); i++) {
                IDownloadTask downloadTask = downloadTasks.valueAt(i);
                cancelDownload(downloadTask, false);
            }
            downloadTasks.clear();
        }
        finishSelf();
    }

    private void addDownload(IDownloadTask downloadTask) {
        if (downloadTask == null) {
            return;
        }

        synchronized (downloadTasks) {
            if (downloadTasks.size() == 0) {
                managerCompat.cancelAll();
            }

            downloadTasks.put(downloadTask.getId(), downloadTask);

            if (canStartNextTask()) {
                downloadTask.startDownload(scheduler);
            }
        }
    }

    private void cancelDownload(IDownloadTask downloadTask, boolean callEvent) {
        if (deleteDownload(downloadTask, false)) {
            downloadTask.stopDownload(callEvent);
        }
    }

    private boolean deleteDownload(IDownloadTask downloadTask, boolean startNext) {
        synchronized (downloadTasks) {
            if (downloadTask != null) {
                downloadTasks.remove(downloadTask.getId());
                managerCompat.cancel(downloadTask.getId());

                if (startNext) {
                    startNextTaskAfterRemove(downloadTask.getDownloadBook());
                }

                return true;
            }
        }

        return false;
    }

    private void removeDownload(int id) {
        if (id == -1) {
            return;
        }

        synchronized (downloadTasks) {
            for (int i = downloadTasks.size() - 1; i >= 0; i--) {
                IDownloadTask downloadTask = downloadTasks.valueAt(i);
                if (downloadTask.getId() == id) {
                    cancelDownload(downloadTask, true);
                    break;
                }
            }
        }
    }

    private void removeDownload(String noteUrl) {
        if (noteUrl == null) {
            return;
        }

        synchronized (downloadTasks) {
            for (int i = downloadTasks.size() - 1; i >= 0; i--) {
                IDownloadTask downloadTask = downloadTasks.valueAt(i);
                if (noteUrl.equals(downloadTask.getDownloadBook().getNoteUrl())) {
                    cancelDownload(downloadTask, true);
                    break;
                }
            }
        }
    }

    private boolean containsDownload(int id) {
        synchronized (downloadTasks) {
            return downloadTasks.indexOfKey(id) >= 0;
        }
    }

    private void refreshDownloadList() {
        ArrayList<DownloadBookBean> downloadBookBeans = new ArrayList<>();
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            DownloadBookBean downloadBook = downloadTask.getDownloadBook();
            if (downloadBook != null) {
                downloadBookBeans.add(downloadBook);
            }
        }
        if (!downloadBookBeans.isEmpty()) {
            sendDownloadBooks(downloadBookBeans);
        }
    }

    private void startNextTaskAfterRemove(DownloadBookBean downloadBook) {
        sendDownloadBook(removeDownloadAction, downloadBook);
        if (downloadTasks.size() == 0) {
            finishSelf();
        } else {
            startNextTask();
        }
    }

    private void startNextTask() {
        if (!canStartNextTask()) {
            return;
        }
        for (int i = 0; i < downloadTasks.size(); i++) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (!downloadTask.isDownloading()) {
                downloadTask.startDownload(scheduler);
                break;
            }
        }
    }


    private boolean canStartNextTask() {
        int downloading = 0;
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (downloadTask.isDownloading()) {
                downloading += 1;
            }
        }
        return downloading < threadsNum;
    }


    private boolean checkDownloadTaskExist(DownloadBookBean downloadBook) {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (Objects.equals(downloadTask.getDownloadBook(), downloadBook)) {
                return true;
            }
        }
        return false;
    }


    private void sendDownloadBook(String action, DownloadBookBean downloadBook) {
        DownloadInfo downloadInfo = new DownloadInfo(action, downloadBook);
        RxBus.get().post(RxBusTag.BOOK_DOWNLOAD, downloadInfo);
    }

    private void sendDownloadBooks(ArrayList<DownloadBookBean> downloadBooks) {
        DownloadInfo downloadInfo = new DownloadInfo(obtainDownloadListAction, downloadBooks);
        RxBus.get().post(RxBusTag.BOOK_DOWNLOAD, downloadInfo);
    }

    private void toast(String msg) {
        ToastUtils.toast(this, msg);
    }

    private PendingIntent getRemovePendingIntent(int notificationId) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.removeDownloadAction);
        intent.putExtra("taskId", notificationId);
        return PendingIntent.getService(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void isProgress(int notificationId, long when, String bookName, ChapterBean downloadChapterBean) {
        if (!containsDownload(notificationId)) {
            managerCompat.cancel(notificationId);
            return;
        }
        Intent mainIntent = new Intent(this, DownloadActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download_white_24dp)
                //通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                //点击通知后自动清除
                .setAutoCancel(false)
                .setWhen(when)//保持位置不变
                .setContentTitle("正在下载：" + bookName)
                .setContentText(downloadChapterBean.getDisplayDurChapterName() == null ? "  " : downloadChapterBean.getDisplayDurChapterName())
                .setContentIntent(mainPendingIntent);
        builder.addAction(R.drawable.ic_stop_white_24dp, getString(R.string.cancel), getRemovePendingIntent(notificationId));
        //发送通知
        managerCompat.notify(notificationId, builder.build());
    }

    private void finishSelf() {
        RxBus.get().post(RxBusTag.BOOK_DOWNLOAD, new DownloadInfo(finishDownloadAction));
        stopSelf();
    }

    public static void addDownload(Context context, DownloadBookBean downloadBook) {
        if (downloadBook == null) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(addDownloadAction);
        intent.putExtra("downloadBook", downloadBook);
        context.startService(intent);
    }

    public static void removeDownload(Context context, String noteUrl) {
        if (noteUrl == null || !running) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(removeDownloadAction);
        intent.putExtra("noteUrl", noteUrl);
        context.startService(intent);
    }

    public static void cancelDownload(Context context) {
        if (!running) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(cancelAction);
        context.startService(intent);
    }

    public static void obtainDownloadList(Context context) {
        if (!running) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(obtainDownloadListAction);
        context.startService(intent);
    }

}