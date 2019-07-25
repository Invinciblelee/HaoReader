//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.monke.basemvplib.AppActivityManager;
import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.mediacache.HttpProxyCacheServer;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

import io.reactivex.plugins.RxJavaPlugins;

public class MApplication extends Application {

    public final static boolean DEBUG = BuildConfig.DEBUG;
    public final static String channelIdDownload = "channel_download";
    public final static String channelIdReadAloud = "channel_read_aloud";
    public final static String channelIdAudioBook = "channel_audio_book";
    public final static String channelIdWeb = "channel_web";

    private static String versionName;
    private static int versionCode;

    private HttpProxyCacheServer proxyCacheServer;


    public static int getVersionCode() {
        return versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static HttpProxyCacheServer getProxyCacheServer(Context context) {
        MApplication app = (MApplication) context.getApplicationContext();
        return app.getProxyCacheServer();
    }


    private HttpProxyCacheServer getProxyCacheServer() {
        if (proxyCacheServer == null) {
            proxyCacheServer = new HttpProxyCacheServer.Builder(this)
                    .cacheDirectory(new File(Constant.AUDIO_CACHE_PATH))
                    .maxCacheSize(1024 * 1024 * 1024)
                    .build();
        }
        return proxyCacheServer;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = 0;
            versionName = "0.0.0";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIdDownload();
            createChannelIdReadAloud();
            createChannelIdAudioBook();
            createChannelIdWeb();
        }

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (DEBUG) {
                throwable.printStackTrace();
            }
        });

        ContextHolder.initialize(this);

        CrashReport.initCrashReport(getApplicationContext(), Constant.BUGLY_APP_ID, DEBUG);

        Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        boolean nightTheme = AppConfigHelper.get().getPreferences().getBoolean("nightTheme", false);
        AppCompatDelegate.setDefaultNightMode(nightTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        registerActivityCallback();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdDownload() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdDownload,
                getString(R.string.download_offline),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdReadAloud() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdReadAloud,
                getString(R.string.read_aloud),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdAudioBook() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdAudioBook,
                getString(R.string.audio_book),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdWeb() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdWeb,
                getString(R.string.web_menu),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }

    private void registerActivityCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                AppActivityManager.getInstance().add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                AppActivityManager.getInstance().remove(activity);
            }
        });
    }
}
