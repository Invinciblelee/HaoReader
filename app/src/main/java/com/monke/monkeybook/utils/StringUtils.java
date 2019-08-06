package com.monke.monkeybook.utils;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by newbiechen on 17-4-22.
 * 对文字操作的工具类
 */

public class StringUtils {
    private static final String TAG = "StringUtils";
    private static final int HOUR_OF_DAY = 24;
    private static final int DAY_OF_YESTERDAY = 2;
    private static final int TIME_UNIT = 60;

    public static final Comparator<String> STRING_COMPARATOR = StringUtils::naturalCompare;


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

    //将时间转换成日期
    public static String dateConvert(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }

    //将日期转换成昨天、今天、明天
    public static String dateConvert(String source, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = format.parse(source);
            long curTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            //将MISC 转换成 sec
            long difSec = Math.abs((curTime - date.getTime()) / 1000);
            long difMin = difSec / 60;
            long difHour = difMin / 60;
            long difDate = difHour / 60;
            int oldHour = calendar.get(Calendar.HOUR);
            //如果没有时间
            if (oldHour == 0) {
                //比日期:昨天今天和明天
                if (difDate == 0) {
                    return "今天";
                } else if (difDate < DAY_OF_YESTERDAY) {
                    return "昨天";
                } else {
                    DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    return convertFormat.format(date);
                }
            }

            if (difSec < TIME_UNIT) {
                return difSec + "秒前";
            } else if (difMin < TIME_UNIT) {
                return difMin + "分钟前";
            } else if (difHour < HOUR_OF_DAY) {
                return difHour + "小时前";
            } else if (difDate < DAY_OF_YESTERDAY) {
                return "昨天";
            } else {
                DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return convertFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String millis2String(final long millis, @NonNull final DateFormat format) {
        return format.format(new Date(millis));
    }

    public static String toFirstCapital(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    /**
     * 将文本中的半角字符，转换成全角字符
     *
     * @param input
     * @return
     */
    public static String halfToFull(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i] > 32 && c[i] < 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    //功能：字符串全角转换为半角
    public static String fullToHalf(String input) {
        char[] c = input.toCharArray();
        for (int i = 0, length = c.length; i < length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    private final static HashMap<Character, Integer> ChnMap = getChnMap();

    private static HashMap<Character, Integer> getChnMap() {
        String cnStr = "零一二三四五六七八九十";
        HashMap<Character, Integer> map = new HashMap<>();
        char[] c = cnStr.toCharArray();
        for (int i = 0; i <= 10; i++) {
            map.put(c[i], i);
        }
        map.put('百', 100);
        map.put('千', 1000);
        map.put('万', 10000);
        map.put('亿', 100000000);
        return map;
    }

    // 修改自 https://binux.blog/2011/03/python-tools-chinese-digit/
    public static int chineseNumToInt(String chNum) {
        int result = 0;
        int tmp = 0;
        int billion = 0;
        char[] cn = chNum.toCharArray();

        // "一零二五" 形式
        if (cn.length > 1 && chNum.matches("^[零一二三四五六七八九]$")) {
            for (int i = 0; i < cn.length; i++) {
                cn[i] = (char) (48 + ChnMap.get(cn[i]));
            }
            return Integer.parseInt(new String(cn));
        }

        // "一千零二十五", "一千二" 形式
        try {
            for (int i = 0; i < cn.length; i++) {
                int tmpNum = ChnMap.get(cn[i]);
                if (tmpNum == 100000000) {
                    result += tmp;
                    result *= tmpNum;
                    billion = billion * 100000000 + result;
                    result = 0;
                    tmp = 0;
                } else if (tmpNum == 10000) {
                    result += tmp;
                    result *= tmpNum;
                    tmp = 0;
                } else if (tmpNum >= 10) {
                    if (tmp == 0)
                        tmp = 1;
                    result += tmpNum * tmp;
                    tmp = 0;
                } else {
                    if (i >= 2 && i == cn.length - 1 && ChnMap.get(cn[i - 1]) > 10)
                        tmp = tmpNum * ChnMap.get(cn[i - 1]) / 10;
                    else
                        tmp = tmp * 10 + tmpNum;
                }
            }
            result += tmp + billion;
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int stringToInt(String str) {
        if (str != null) {
            String num = fullToHalf(str).replaceAll("\\s", "");
            try {
                return Integer.parseInt(num);
            } catch (Exception e) {
                num = num.replaceAll("两", "二").replaceAll("〇", "零");
                return chineseNumToInt(num);
            }
        }
        return -1;
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    /**
     * delimiter 分隔符
     * elements 需要连接的字符数组
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        // 空指针判断
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);


        // Number of elements not likely worth Arrays.stream overhead.
        // 此处用到了StringJoiner(JDK 8引入的类）
        // 先构造一个以参数delimiter为分隔符的StringJoiner对象
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            // 拼接字符
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public static boolean isContainNumber(String company) {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(company);
        return m.find();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static String base64Decode(String str) {
        if (isBlank(str)) {
            return "";
        }
        byte[] bytes = Base64.decode(str, Base64.DEFAULT);
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes);
        }
    }

    public static String base64Encode(String str) {
        if (isBlank(str)) {
            return "";
        }
        byte[] bytes = Base64.encode(str.getBytes(), Base64.DEFAULT);
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes);
        }
    }

    public static String getBaseUrl(String url) {
        if (url == null || !url.startsWith("http")) return null;
        int index = url.indexOf("/", 9);
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }

    public static String valueOf(Object object) {
        return object == null ? "" : object.toString();
    }

    public static boolean startWithIgnoreCase(String src, String obj) {
        if (src == null || obj == null) return false;
        if (obj.length() > src.length()) return false;
        return src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }


    public static String nonNull(String string) {
        return string == null ? "" : string;
    }

    public static boolean isBlank(String text) {
        return trim(text).length() == 0;
    }

    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    public static String checkBlank(String text, String defVal) {
        if (isBlank(text)) {
            return defVal == null ? "" : defVal;
        }
        return text;
    }

    public static String checkNull(String text, String defVal) {
        if (text == null) {
            return defVal == null ? "" : defVal;
        }
        return text;
    }

    public static String trim(String string) {
        if(string == null || string.length() == 0) return "";
        int start = 0, len = string.length();
        int end = len - 1;
        while ((start < end) && ((string.charAt(start) <= ' ') || (string.charAt(start) == '　'))) {
            ++start;
        }
        while ((start < end) && ((string.charAt(end) <= ' ') || (string.charAt(end) == '　'))) {
            --end;
        }
        if (end < len) ++end;
        return ((start > 0) || (end < len)) ? string.substring(start, end) : string;
    }

    // String数字转int数字的高效方法(利用ASCII值判断)
    public static int parseInt(String string) {
        if (isBlank(string)) return -1;
        int r = 0;
        char n;
        for (int i = 0, l = string.length(); i < l; i++) {
            n = string.charAt(i);
            if (n >= '0' && n <= '9') {
                r = r * 10 + (n - 0x30); //'0-9'的ASCII值为0x30-0x39
            }
        }
        return r;
    }

    public static boolean containsIgnoreCase(String base, String constraint) {
        if (base == null) {
            return false;
        }
        return base.toLowerCase().contains(constraint == null ? "" : constraint.toLowerCase());
    }

    public static boolean isJsonType(String text) {
        boolean result = false;
        if (isNotBlank(text)) {
            text = trim(text);
            if (text.startsWith("{") && text.endsWith("}")) {
                result = true;
            } else if (text.startsWith("[") && text.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonObject(String text) {
        boolean result = false;
        if (isNotBlank(text)) {
            text = trim(text);
            if (text.startsWith("{") && text.endsWith("}")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonArray(String text) {
        boolean result = false;
        if (isNotBlank(text)) {
            text = trim(text);
            if (text.startsWith("[") && text.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static String wrapJsonArray(String text) {
        if (isJsonArray(text)) {
            return text;
        }
        if (isJsonObject(text)) {
            return "[" + text + "]";
        }
        return text;
    }

}
