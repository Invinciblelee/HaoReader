package com.monke.monkeybook.bean;

import java.util.List;

public class DownloadInfo {

    private String action;
    private DownloadBookBean downloadBookBean;
    private List<DownloadBookBean> downloadBookBeans;

    public DownloadInfo(String action) {
        this.action = action;
    }

    public DownloadInfo(String action, DownloadBookBean downloadBookBean) {
        this.action = action;
        this.downloadBookBean = downloadBookBean;
    }

    public DownloadInfo(String action, List<DownloadBookBean> downloadBookBeans) {
        this.action = action;
        this.downloadBookBeans = downloadBookBeans;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public DownloadBookBean getDownloadBookBean() {
        return downloadBookBean;
    }

    public void setDownloadBookBean(DownloadBookBean downloadBookBean) {
        this.downloadBookBean = downloadBookBean;
    }

    public List<DownloadBookBean> getDownloadBookBeans() {
        return downloadBookBeans;
    }

    public void setDownloadBookBeans(List<DownloadBookBean> downloadBookBeans) {
        this.downloadBookBeans = downloadBookBeans;
    }
}
