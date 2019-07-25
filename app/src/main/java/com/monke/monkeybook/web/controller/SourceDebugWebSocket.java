package com.monke.monkeybook.web.controller;

import android.os.AsyncTask;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;
import com.monke.monkeybook.model.content.Debug;
import com.monke.monkeybook.utils.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.model.analyzeRule.assit.AnalyzeGlobal.MAP_TYPE;

public class SourceDebugWebSocket extends NanoWSD.WebSocket {
    private CompositeDisposable compositeDisposable;

    public SourceDebugWebSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        RxBus.get().register(this);
        compositeDisposable = new CompositeDisposable();
        Observable.interval(10, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        try {
                            ping(new byte[]{aLong.byteValue()});
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        if (!StringUtils.isJsonType(message.getTextPayload())) return;
        Map<String, String> debugBean = Assistant.fromJson(message.getTextPayload(), MAP_TYPE);
        String tag = debugBean.get("tag");
        String key = debugBean.get("key");
        Debug.newDebug(tag, key, compositeDisposable, new Debug.CallBack() {
            @Override
            public void printLog(String msg) {
                AsyncTask.execute(() -> {
                    try {
                        send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void printError(String msg) {
                AsyncTask.execute(() -> {
                    try {
                        send(msg);
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }

            @Override
            public void finish() {
                AsyncTask.execute(() -> {
                    try {
                        send("finish");
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }
        });
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Subscribe(thread = EventThread.IO, tags = {@Tag("printDebugLog")})
    public void printDebugLog(String msg) {
        try {
            send(msg);
        } catch (IOException e) {
            Debug.SOURCE_DEBUG_TAG = null;
        }
    }

}
