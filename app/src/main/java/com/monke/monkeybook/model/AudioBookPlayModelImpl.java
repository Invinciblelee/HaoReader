package com.monke.monkeybook.model;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.model.impl.IAudioBookPlayModel;
import com.monke.monkeybook.model.content.AudioSniffer;

public class AudioBookPlayModelImpl implements IAudioBookPlayModel {

    private AudioSniffer sniffer;

    public AudioBookPlayModelImpl() {
    }

    @Override
    public void ensureChapterList(BookShelfBean bookShelfBean) {

    }

    @Override
    public void ensureChapterUrl(ChapterListBean chapter) {

    }

    @Override
    public void closeSniffer() {

    }
}
