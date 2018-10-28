package com.monke.monkeybook.widget.page;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;

import java.io.BufferedReader;

public class PDFPageLoader extends PageLoader {

    public PDFPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
    }

    @Override
    public void refreshChapterList() {

    }

    @Override
    protected BufferedReader getChapterReader(ChapterListBean chapter) throws Exception {
        return null;
    }

    @Override
    protected boolean hasChapterData(ChapterListBean chapter) {
        return false;
    }
}
