package com.monke.monkeybook.web;

import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.analyzeRule.assit.Assistant;

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
        return newFixedLengthResponse(Assistant.toJson(callback.ReplaceRule()));
    }

    public interface Callback {
        List<ReplaceRuleBean> ReplaceRule();
    }
}
