package com.monke.monkeybook.widget.page;

import android.util.Log;

import java.util.List;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtChapter {

    public enum Intent {
        NEXT, PREV, NONE
    }

    private Intent intent;

    private int id;
    private List<TxtPage> txtPages;
    private int status;
    private String errorMsg;

    private boolean isOpened;
    private TxtPage currentPage;
    private TxtPage lastPage;

    TxtChapter(int id, int status) {
        this.id = id;
        setStatus(status);
        isOpened = false;
    }

    int getId() {
        return this.id;
    }

    int getPosition() {
        return currentPage == null ? 0 : currentPage.position;
    }

    int getLastPosition() {
        return lastPage == null ? -1 : lastPage.position;
    }

    void setPosition(int position) {
        if (!isEmpty()) {
            position = Math.max(0, position);
            position = Math.min(position, txtPages.size() - 1);
            currentPage = txtPages.get(position);
            if (intent == Intent.NONE) {
                lastPage = currentPage;
            } else {
                lastPage = null;
            }
        }
    }

    void setIntent(Intent intent) {
        this.intent = intent;

        if (intent == Intent.PREV) {
            setPosition(size() - 1);
        } else if (intent == Intent.NEXT) {
            setPosition(0);
        }
    }

    Intent getIntent() {
        return intent;
    }

    void setTxtPages(List<TxtPage> txtPages) {
        this.txtPages = txtPages;
        if (isEmpty()) {
            setStatus(PageStatus.STATUS_CONTENT_EMPTY);
        } else {
            setStatus(PageStatus.STATUS_FINISH);

            if (intent == Intent.PREV) {
                setPosition(txtPages.size() - 1);
            } else if (intent == Intent.NEXT) {
                setPosition(0);
            }
        }
    }

    void setStatus(int status) {
        if (this.status != status) {
            this.status = status;
            this.errorMsg = PageStatus.getStatusPrompt(status);
        }
    }

    int getStatus() {
        return status;
    }

    String getErrorMsg() {
        return errorMsg;
    }

    void setOpened() {
        isOpened = true;
    }

    boolean isNotOpened() {
        return !isOpened;
    }

    TxtPage getCurrentPage() {
        return currentPage;
    }

    TxtPage getLastPage() {
        return lastPage;
    }

    void pageCancel() {
        currentPage = lastPage;
    }

    /**
     * 获取正文
     */
    public String getContent(int pagePos) {
        if (pagePos < 0 || pagePos >= size()) {
            return null;
        }
        TxtPage txtPage = txtPages.get(pagePos);
        StringBuilder s = new StringBuilder();
        for (int i = 0, lines = txtPage.lines.size(); i < lines; i++) {
            s.append(txtPage.lines.get(i));
        }
        return s.toString();
    }

    /**
     * 获取上一个页面
     */
    boolean prevPage() {
        int pos = getPosition() - 1;
        if (pos >= 0) {
            lastPage = currentPage;
            currentPage = txtPages.get(pos);
            return true;
        }
        return false;
    }

    /**
     * 获取下一的页面
     */
    boolean nextPage() {
        int pos = getPosition() + 1;
        if (pos < size()) {
            lastPage = currentPage;
            currentPage = txtPages.get(pos);
            return true;
        }
        return false;
    }

    /**
     * 获取当前章节页数
     */
    int size() {
        return txtPages == null ? 0 : txtPages.size();
    }

    boolean isEmpty() {
        return size() == 0;
    }

    boolean hasNext() {
        return getPosition() + 1 < size();
    }
}
