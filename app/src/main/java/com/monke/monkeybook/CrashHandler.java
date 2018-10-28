package com.monke.monkeybook;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FileHelp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * 应用异常处理类
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /**
     * 文件名
     */
    private static final String FILE_NAME = "crash";

    /**
     * 文件名后缀
     */
    private static final String FILE_NAME_SUFFIX = ".trace";

    private UncaughtExceptionHandler mDefaultCrashHandler;
    private WeakReference<Context> mContext;

    private static class InstanceHolder {
        private static CrashHandler SINGLETON = new CrashHandler();
    }

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return InstanceHolder.SINGLETON;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        //得到系统的应用异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前应用异常处理器改为默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = new WeakReference<>(context.getApplicationContext());
    }


    /**
     * 这个是最关键的函数，当系统中有未被捕获的异常，系统将会自动调用 uncaughtException 方法
     *
     * @param thread 为出现未捕获异常的线程
     * @param ex     为未捕获的异常 ，可以通过e 拿到异常信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //导入异常信息到SD卡中
        try {
            dumpExceptionToSDCard(ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //这里可以上传异常信息到服务器，便于开发人员分析日志从而解决Bug
        uploadExceptionToServer();

        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 将异常信息写入SD卡
     */
    private void dumpExceptionToSDCard(Throwable e) throws Exception {
        //如果SD卡不存在或无法使用，则无法将异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }
        File dir = FileHelp.getFolder(Constant.APP_CRASH_PATH);
        //得到当前年月日时分秒
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        //在定义的Crash文件夹下创建文件
        File file = new File(dir, FILE_NAME + time + FILE_NAME_SUFFIX);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        //写入时间
        pw.println(time);
        //写入手机信息
        dumpPhoneInfo(pw);
        pw.println();//换行
        e.printStackTrace(pw);
        pw.close();//关闭输入流
    }

    /**
     * 获取手机各项信息
     */
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        //得到包管理器
        PackageManager pm = mContext.get().getPackageManager();
        //得到包对象
        PackageInfo pi = pm.getPackageInfo(mContext.get().getPackageName(), PackageManager.GET_ACTIVITIES);
        //写入APP版本号
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print("_");
        pw.println(pi.versionCode);
        //写入 Android 版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
        //CPU架构
        pw.print("CPU ABI: ");
        pw.println(Arrays.toString(Build.SUPPORTED_ABIS));
    }

    /**
     * 将错误信息上传至服务器
     */
    private void uploadExceptionToServer() {

    }
}
