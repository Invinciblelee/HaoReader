package com.monke.basemvplib.rxjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.schedulers.Schedulers;

public class RxExecutors {

    private static final String THREAD_NAME_PREFIX = "RxNewThreadScheduler";
    private static final RxThreadFactory THREAD_FACTORY;

    private static final int THREAD_NUM = 6;

    private volatile static Scheduler DEFAULT;

    private RxExecutors() {

    }

    static {
        THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX);
    }

    public static Scheduler newScheduler(int nThreads) {
        ExecutorService service = Executors.newFixedThreadPool(nThreads, THREAD_FACTORY);
        return Schedulers.from(service);
    }

    public static Scheduler newScheduler() {
        return newScheduler(THREAD_NUM);
    }


    public static Scheduler getDefault() {
        if (DEFAULT == null) {
            ExecutorService service = Executors.newFixedThreadPool(THREAD_NUM, THREAD_FACTORY);
            DEFAULT = Schedulers.from(service);
        }
        return DEFAULT;
    }

    public static void setDefault(Scheduler scheduler) {
        if (DEFAULT != scheduler) {
            DEFAULT = scheduler;
        }
    }
}
