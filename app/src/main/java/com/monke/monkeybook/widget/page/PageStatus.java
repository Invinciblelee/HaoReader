package com.monke.monkeybook.widget.page;

public class PageStatus {

    // 当前页面的状态
    public static final int STATUS_PREPARE_CATEGORY = 1; // 准备目录
    public static final int STATUS_LOADING = 2;         // 正在加载
    public static final int STATUS_FINISH = 3;          // 加载完成
    public static final int STATUS_UNKNOWN_ERROR = 4;   // 未知错误
    public static final int STATUS_NETWORK_ERROR = 5;   // 网络错误
    public static final int STATUS_CONTENT_TIMEOUT = 6;   // 加载超时
    public static final int STATUS_CONTENT_EMPTY = 7;   // 空数据
    public static final int STATUS_CONTENT_ERROR = 8;   // 正文加载失败
    public static final int STATUS_PARING = 9;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 10;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 11;  // 获取到的目录为空
    public static final int STATUS_CATEGORY_ERROR = 12; //目录获取失败
    public static final int STATUS_HY = 13;            // 换源
    public static final int STATUS_HY_ERROR = 14;      // 换源失败
    public static final int STATUS_CHANGE_CHARSET = 15; //设置编码
    public static final int STATUS_SOURCE_NOT_FIND = 16; //没有找到书源

    private PageStatus() {
    }

    static String getStatusPrompt(int status) {
        String tip;
        switch (status) {
            case STATUS_UNKNOWN_ERROR:
                tip = String.format("加载失败\n%s", "出现未知错误");
                break;
            case STATUS_NETWORK_ERROR:
                tip = String.format("加载失败\n%s", "网络连接不可用");
                break;
            case STATUS_CONTENT_TIMEOUT:
                tip = String.format("加载失败\n%s", "正文内容获取超时");
                break;
            case STATUS_CONTENT_ERROR:
                tip = String.format("加载失败\n%s", "无法获取正文内容");
                break;
            case STATUS_HY_ERROR:
                tip = String.format("换源失败\n%s", "请重新选择书源");
                break;
            case STATUS_PARSE_ERROR:
                tip = String.format("排版失败\n%s", "文件解析错误");
                break;
            case STATUS_CATEGORY_ERROR:
                tip = String.format("加载失败\n%s", "无法获取目录列表");
                break;
            case STATUS_SOURCE_NOT_FIND:
                tip = String.format("加载失败\n%s", "没有找到当前书源");
                break;
            case STATUS_PREPARE_CATEGORY:
                tip = "正在准备目录...";
                break;
            case STATUS_CONTENT_EMPTY:
                tip = "章节内容为空";
                break;
            case STATUS_PARING:
                tip = "正在排版请等待...";
                break;
            case STATUS_CATEGORY_EMPTY:
                tip = "目录列表为空";
                break;
            case STATUS_HY:
                tip = "正在换源请等待...";
                break;
            case STATUS_CHANGE_CHARSET:
                tip = "正在设置编码...";
                break;
            default:
                tip = "正在拼命加载中...";
        }
        return tip;
    }

}
