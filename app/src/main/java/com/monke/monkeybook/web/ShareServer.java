package com.monke.monkeybook.web;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;

import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class ShareServer extends NanoHTTPD {

    private Callback callback;

    public ShareServer(int port, Callback callback) {
        super(port);
        this.callback = callback;
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newFixedLengthResponse(Assistant.toJson(callback.getSources()));
    }

    public interface Callback {
        List<BookSourceBean> getSources();
    }
}
