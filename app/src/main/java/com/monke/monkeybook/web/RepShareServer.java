package com.monke.monkeybook.web;

import com.google.gson.Gson;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class RepShareServer extends NanoHTTPD {

    private Callback callback;

    public RepShareServer(int port, Callback callback) {
        super(port);
        this.callback = callback;
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newFixedLengthResponse(new Gson().toJson(callback.ReplaceRule()));
    }

    public interface Callback {
        List<ReplaceRuleBean> ReplaceRule();
    }
}
