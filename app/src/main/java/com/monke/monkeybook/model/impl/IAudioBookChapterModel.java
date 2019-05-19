package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.bean.ChapterBean;

import io.reactivex.Observable;

public interface IAudioBookChapterModel {


    /**
     * 处理听书章节
     */
    Observable<ChapterBean> getAudioBookContent(String chapterUrl, ChapterBean chapter);

}
