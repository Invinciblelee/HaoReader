package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;

public interface IAudioBookPlayModel {

    void getChapterList(BookShelfBean bookShelfBean);

    void ensureChapterUrl(ChapterListBean chapter);

    void closeSniffer();

}
