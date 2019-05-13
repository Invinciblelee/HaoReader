package com.monke.monkeybook.model.analyzeRule.assit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.monke.monkeybook.help.Logger;
import com.monke.monkeybook.model.analyzeRule.JavaExecutor;
import com.monke.monkeybook.utils.StringUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public final class Global {

    private static final String TAG = Global.class.getSimpleName();

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    public static final Comparator<String> STRING_COMPARATOR = Global::naturalCompare;

    private Global() {
    }

    private static int atoi(String str) {
        //这里要小心，需要判断有效性
        if (str == null || str.length() == 0) {
            return 0;
        }
        int nlen = str.length();
        double sum = 0;
        int sign = 1;
        int j = 0;
        //剔除空格
        while (str.charAt(j) == ' ') {
            j++;
        }
        //判断正数和负数
        if (str.charAt(j) == '+') {
            sign = 1;
            j++;
        } else if (str.charAt(j) == '-') {
            sign = -1;
            j++;
        }

        for (int i = j; i < nlen; i++) {
            char current = str.charAt(i);
            if (current >= '0' && current <= '9') {
                sum = sum * 10 + (current - '0');
            } else {
                break;//碰到非数字，退出转换
            }
        }

        sum = sum * sign;
        //这里要小心，需要判断范围
        if (sum > Integer.MAX_VALUE) {
            sum = Integer.MAX_VALUE;
        } else if (sum < Integer.MIN_VALUE) {
            sum = Integer.MIN_VALUE;
        }
        return (int) sum;
    }

    private static int naturalCompare(String o1, String o2) {
        int i = 0, j = 0;
        StringBuilder temp1 = new StringBuilder();
        StringBuilder temp2 = new StringBuilder();
        int num1, num2;
        int length = Math.min(o1.length(), o2.length());
        while (i < length && j < length) {
            temp1.setLength(0);
            temp2.setLength(0);
            if (o1.charAt(i) > '9' || o1.charAt(i) < '0' || o2.charAt(j) > '9' || o2.charAt(j) < '0') {
                if (o1.charAt(i) == o2.charAt(j)) {
                    i++;
                    j++;
                    continue;
                } else if (o1.charAt(i) > o2.charAt(j)) {
                    return 1;
                } else {
                    return -1;
                }
            }
            while (i < o1.length() && o1.charAt(i) <= '9' && o1.charAt(i) >= '0') {
                temp1.append(o1.charAt(i));
                i++;
            }
            while (j < o2.length() && o2.charAt(j) <= '9' && o2.charAt(j) >= '0') {
                temp2.append(o2.charAt(j));
                j++;
            }
            num1 = atoi(temp1.toString());
            num2 = atoi(temp2.toString());
            if (num1 == num2) {
                if (temp1.length() < temp2.length()) {
                    return 1;
                } else if (temp1.length() > temp2.length()) {
                    return -1;
                }
            } else if (num1 > num2) {
                return 1;
            } else {
                return -1;
            }
        }
        return o1.length() > o2.length() ? 1 : -1;
    }

    public static boolean canConvertToJson(Object object) {
        boolean result = false;
        if (object instanceof List || object instanceof Map) {
            result = true;
        } else {
            String str = StringUtils.valueOf(object);
            if (StringUtils.isNotBlank(str)) {
                str = str.trim();
                if (str.startsWith("{") && str.endsWith("}")) {
                    result = true;
                } else if (str.startsWith("[") && str.endsWith("]")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public static List<Object> evalArrayScript(String jsStr, JavaExecutor java, Object result, String baseUrl) {
        final Object object = evalObjectScript(jsStr, java, result, baseUrl);
        final List<Object> resultList = new ArrayList<>();
        if (object instanceof List) {
            resultList.addAll((List) object);
        } else if (object != null) {
            resultList.add(object);
        }
        return resultList;
    }

    public static Object evalObjectScript(String jsStr, SimpleBindings bindings) {
        try {
            return Global.SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            Logger.e(TAG, jsStr, e);
        }
        return null;
    }

    public static Object evalObjectScript(String jsStr, JavaExecutor java, Object result, String baseUrl) {
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
