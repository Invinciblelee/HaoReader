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
    private NotificationManagerCompat managerCompat;

    public static boolean running = false;

    private ExecutorService executor;
    private Scheduler scheduler;
    private int threadsNum;

    private SparseArray<IDownloadTask> downloadTasks = new SparseArray<>();

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
                downloadTasks.put(getId(), this);

                if (canStartNextTask()) {
                    startDownload(scheduler, threadsNum);
                }
                sendUpDownloadBook(addDownloadAction, downloadBook);
                longToast(String.format(Locale.getDefault(), "%s：下载任务添加成功", downloadBook.getName()));
            }

            @Override
            public void onDownloadProgress(String bookName, ChapterBean chapterBean) {
                isProgress(getId(), getWhen(), bookName, chapterBean);
            }

            @Override
            public void onDownloadChange(DownloadBookBean downloadBook) {
                sendUpDownloadBook(progressDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadError(DownloadBookBean downloadBook) {
                downloadTasks.remove(getId());
                managerCompat.cancel(getId());

                toast(String.format(Locale.getDefault(), "%s：下载失败", downloadBook.getName()));

                startNextTaskAfterRemove(downloadBook);
            }

            @Override
            public void onDownloadComplete(DownloadBookBean downloadBook) {
                if (downloadTasks.indexOfValue(this) >= 0) {
                    if (downloadBook.getSuccessCount() == 0) {
                        toast(String.format(Locale.getDefault(), "%s：取消下载", downloadBook.getName()));
                    } else {
                        longToast(String.format(Locale.getDefault(), "%s：共下载%d章", downloadBook.getName(), downloadBook.getSuccessCount()));
                    }
                } else if (!downloadBook.isValid()) {
                    toast(String.format(Locale.getDefault(), "%s：所有章节已缓存", downloadBook.getName()));
                }

                downloadTasks.remove(getId());
                managerCompat.cancel(getId());

                startNextTaskAfterRemove(downloadBook);
            }
        };
    }

    private void cancelDownload() {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            downloadTask.stopDownload();
        }
        downloadTasks.clear();
        finishSelf();
    }

    private void removeDownload(int id) {
        if (id == -1) {
            return;
        }

        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (downloadTask.getId() == id) {
                downloadTask.stopDownload();
                break;
            }
        }
    }

    private void removeDownload(String noteUrl) {
        if (noteUrl == null) {
            return;
        }

        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (noteUrl.equals(downloadTask.getDownloadBook().getNoteUrl())) {
                downloadTask.stopDownload();
                break;
            }
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
            sendUpDownloadBooks(downloadBookBeans);
        }
    }

    private void startNextTaskAfterRemove(DownloadBookBean downloadBook) {
        sendUpDownloadBook(removeDownloadAction, downloadBook);
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
                downloadTask.startDownload(scheduler, threadsNum);
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


    private void sendUpDownloadBook(String action, DownloadBookBean downloadBook) {
        DownloadInfo downloadInfo = new DownloadInfo(action, downloadBook);
        RxBus.get().post(RxBusTag.BOOK_DOWNLOAD, downloadInfo);
    }

    private void sendUpDownloadBooks(ArrayList<DownloadBookBean> downloadBooks) {
        DownloadInfo downloadInfo = new DownloadInfo(obtainDownloadListAction, downloadBooks);
        RxBus.get().post(RxBusTag.BOOK_DOWNLOAD, downloadInfo);
    }

    private void toast(String msg) {
        ToastUtils.toast(this, msg);
    }

    private void longToast(String msg) {
        ToastUtils.longToast(this, msg);
    }

    private PendingIntent getRemovePendingIntent(int notificationId) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.removeDownloadAction);
        intent.putExtra("taskId", notificationId);
        return PendingIntent.getService(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void isProgress(int notificationId, long when, String bookName, ChapterBean downloadChapterBean) {
        if (!running) {
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
                .setContentText(downloadChapterBean.getDurChapterName() == null ? "  " : downloadChapterBean.getDurChapterName())
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