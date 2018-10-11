//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

public class LocBookShelfBean {
    private boolean isNew;
    private BookShelfBean bookShelfBean;

    public LocBookShelfBean(boolean isNew,BookShelfBean bookShelfBean){
        this.isNew = isNew;
        this.bookShelfBean = bookShelfBean;
    }

    public boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }

    public BookShelfBean getBookShelfBean() {
        return bookShelfBean;
    }

    public void setBookShelfBean(BookShelfBean bookShelfBean) {
        this.bookShelfBean = bookShelfBean;
    }
}
