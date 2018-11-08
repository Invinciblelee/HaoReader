package com.monke.monkeybook.help;

import com.monke.monkeybook.bean.BookShelfBean;

public class BookShelfDataHolder {

    private boolean inBookShelf;
    private BookShelfBean bookShelf;

    private volatile static BookShelfDataHolder mInstance;

    private BookShelfDataHolder() {

    }

    public static BookShelfDataHolder getInstance() {
        if (mInstance == null) {
            synchronized (BookShelfDataHolder.class) {
                if (mInstance == null) {
                    mInstance = new BookShelfDataHolder();
                }
            }
        }
        return mInstance;
    }


    public void setBookShelf(BookShelfBean bookShelf) {
        this.bookShelf = bookShelf;
    }

    public BookShelfBean getBookShelf() {
        return this.bookShelf;
    }

    public boolean isInBookShelf() {
        return inBookShelf;
    }

    public void setInBookShelf(boolean inBookShelf) {
        this.inBookShelf = inBookShelf;
    }

    public void cleanData() {
        this.bookShelf = null;
    }
}
