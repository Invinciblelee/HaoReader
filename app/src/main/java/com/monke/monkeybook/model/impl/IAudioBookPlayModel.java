package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;

public interface IAudioBookPlayModel {

    void getChapterList(BookShelfBean bookShelfBean);

    void ensureChapterUrl(ChapterBean chapter);

    void closeSniffer();

}
