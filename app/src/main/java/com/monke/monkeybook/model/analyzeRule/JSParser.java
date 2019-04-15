package com.monke.monkeybook.model.analyzeRule;


import com.eclipsesource.v8.V8;

final class JSParser {

    JSParser() {

    }

    private V8 JSV8;

    void start() {
        stop();
        JSV8 = V8.createV8Runtime();
    }

    boolean isStarted() {
        return JSV8 != null && !JSV8.isReleased()
                && !JSV8.isUndefined() && !JSV8.isWeak();
    }

    String evalJS(String jsStr, Object java, String result, String baseUrl) {
        if (JSV8 == null) return jsStr;
        JSV8.add("result", result);
        JSV8.add("baseUrl", baseUrl);
        if (java != null) {
            JSV8.registerJavaMethod(java, "ajax", "ajax", new Class[]{String.class});
            JSV8.registerJavaMethod(java, "base64Decode", "base64Decode", new Class[]{String.class});
            JSV8.registerJavaMethod(java, "parseResultContent", "parseResultContent", new Class[]{String.class, String.class});
            JSV8.registerJavaMethod(java, "parseResultUrl", "parseResultUrl", new Class[]{String.class, String.class});
            JSV8.registerJavaMethod(java, "formatResultContent", "formatResultContent", new Class[]{String.class});
        }
        return JSV8.executeStringScript(jsStr);
    }

    void stop() {
        if (JSV8 != null && !JSV8.isReleased()) {
            JSV8.release(true);
            JSV8 = null;
        }
    }

}
