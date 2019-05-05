package com.monke.monkeybook.model.analyzeRule;


import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

final class JSParser {

    private static final String TAG = JSParser.class.getSimpleName();

    private JSParser() {

    }

    static String evalStringScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        return StringUtils.valueOf(evalObjectScript(jsStr, java, result, baseUrl));
    }

    static List<String> evalArrayScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        final Object object = evalObjectScript(jsStr, java, result, baseUrl);
        final List<String> resultList = new ArrayList<>();
        if (object instanceof List) {
            for (Object obj : (List) object) {
                resultList.add(StringUtils.valueOf(obj));
            }
        } else {
            resultList.add(StringUtils.valueOf(object));
        }
        return resultList;
    }

    static Object evalObjectScript(String jsStr, SimpleBindings bindings) {
        try {
            return Global.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }

    private static Object evalObjectScript(String jsStr, JavaExecutor java, String result, String baseUrl) {
        try {
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("java", java);
            bindings.put("result", result);
            bindings.put("baseUrl", baseUrl);
            return Global.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }

}
