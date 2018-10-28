package com.monke.monkeybook.widget.page;

import android.text.TextUtils;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.FileHelp;

public class PageLoaderFactory {

    private PageLoaderFactory() {

    }


    public static PageLoader createPageLoader(PageView pageView, BookShelfBean book) {
        if (book == null) {
            throw new IllegalArgumentException("can not create PageLoader for an invalid book");
        }

        if (TextUtils.equals(book.getTag(), BookShelfBean.LOCAL_TAG)) {
            String fileSuffix = FileHelp.getFileSuffix(book.getNoteUrl());
            if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_TXT)) {
                return new TXTPageLoader(pageView, book);
            } else if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_EPUB)) {
                return new EPUBPageLoader(pageView, book);
            } else if (fileSuffix.equalsIgnoreCase(FileHelp.SUFFIX_PDF)) {
                return new PDFPageLoader(pageView, book);
            } else {
                throw new IllegalArgumentException("create pageLoader failed, unsupported file format: " + fileSuffix);
            }
        } else {
            return new NetPageLoader(pageView, book);
        }
    }

}
