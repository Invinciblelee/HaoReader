package com.monke.monkeybook.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.web.HttpServer;
import com.monke.monkeybook.web.WebSocketServer;

import java.io.IOException;
import java.net.InetAddress;

public class WebService extends Service {
    private static boolean isRunning = false;
    private HttpServer httpServer;
    private WebSocketServer webSocketServer;
    private static final int notificationId = 19901145;
    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";

    public static void startThis(Activity activity) {
        Intent intent = new Intent(activity, WebService.class);
        intent.setAction(ActionStartService);
        activity.startService(intent);
    }

    public static void stopThis(Context context) {
        if (!isRunning) return;
        try {
            Intent intent = new Intent(context, WebService.class);
            context.stopService(intent);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateNotification("正在启动服务");
        ToastUtils.toast(this, "已启动Web服务");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ActionStartService:
                    upServer();
                    break;
                case ActionDoneService:
                    stopSelf();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void upServer() {
        if (httpServer != null && httpServer.isAlive()) {
            httpServer.stop();
        }
        if (webSocketServer != null && webSocketServer.isAlive()) {
            webSocketServer.stop();
        }
        int port = getPort();
        httpServer = new HttpServer(port);
        webSocketServer = new WebSocketServer(port + 1);
        InetAddress inetAddress = NetworkUtil.getLocalIPAddress();
        if (inetAddress != null) {
            try {
                httpServer.start();
                webSocketServer.start(30000);
                isRunning = true;
                updateNotification(getString(R.string.http_ip, inetAddress.getHostAddress(), port));
            } catch (IOException e) {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (httpServer != null && httpServer.isAlive()) {
            httpServer.stop();
        }
        if (webSocketServer != null && webSocketServer.isAlive()) {
            webSocketServer.stop();
        }
    }

    private PendingIntent getThisServicePendingIntent() {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(ActionDoneService);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getPort() {
        return 1223;
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdWeb)
                .setSmallIcon(R.drawable.ic_wifi_tethering_white_24dp)
                .setOngoing(true)
                .setContentTitle(getString(R.string.web_edit_source))
                .setContentText(content);
        builder.addAction(R.drawable.ic_stop_white_24dp, getString(R.string.cancel), getThisServicePendingIntent());
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        startForeground(notificationId, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
