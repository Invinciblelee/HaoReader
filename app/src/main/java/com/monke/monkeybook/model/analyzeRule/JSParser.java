package com.monke.monkeybook.model.analyzeRule;


import com.eclipsesource.v8.V8;

final class JSParser {

    JSParser() {

    }

    private V8 JSV8;

    void start() {
        stop();
        try {
            JSV8 = V8.createV8Runtime();
        } catch (Exception ignore) {
        }
    }

    String evalJS(String jsStr, Object java, String result, String baseUrl) {
        try {
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
        } catch (Exception ignore) {
        }
        return jsStr;
    }

    void stop() {
        if (JSV8 != null) {
            try {
                JSV8.release(true);
            } catch (Exception ignore) {
            }
        }
    }

}
