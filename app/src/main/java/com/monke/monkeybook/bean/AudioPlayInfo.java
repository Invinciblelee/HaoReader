package com.monke.monkeybook.bean;

import java.util.List;

public class AudioPlayInfo {

    private String action;

    private int progress;
    private int duration;
    private boolean isPause;
    private int timerMinute;
    private int timerMinuteUntilFinish;
    private int durChapterIndex;
    private ChapterBean durChapter;
    private String name;
    private String cover;
    private List<ChapterBean> chapterBeans;

    private boolean loading;

    private AudioPlayInfo() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public int getTimerMinute() {
        return timerMinute;
    }

    public void setTimerMinute(int timerMinute) {
        this.timerMinute = timerMinute;
    }

    public int getTimerMinuteUntilFinish() {
        return timerMinuteUntilFinish;
    }

    public void setTimerMinuteUntilFinish(int timerMinuteUntilFinish) {
        this.timerMinuteUntilFinish = timerMinuteUntilFinish;
    }

    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public ChapterBean getDurChapter() {
        return durChapter;
    }

    public void setDurChapter(ChapterBean durChapter) {
        this.durChapter = durChapter;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<ChapterBean> getChapterBeans() {
        return chapterBeans;
    }

    public void setChapterBeans(List<ChapterBean> chapterBeans) {
        this.chapterBeans = chapterBeans;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public static AudioPlayInfo attach(String name, String cover) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setName(name);
        info.setCover(cover);
        return info;
    }

    public static AudioPlayInfo start(int timerMinute, String cover, int durChapterIndex, List<ChapterBean> chapterBeans) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setTimerMinute(timerMinute);
        info.setChapterBeans(chapterBeans);
        info.setCover(cover);
        info.setDurChapterIndex(durChapterIndex);
        return info;
    }

    public static AudioPlayInfo loading(boolean loading) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setLoading(loading);
        return info;
    }


    public static AudioPlayInfo pull(int timerMinute, int timerMinuteUntilFinish, String name, String cover, List<ChapterBean> chapterBeans) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setTimerMinute(timerMinute);
        info.setTimerMinuteUntilFinish(timerMinuteUntilFinish);
        info.setName(name);
        info.setChapterBeans(chapterBeans);
        info.setCover(cover);
        return info;
    }

    public static AudioPlayInfo start(ChapterBean chapterBean) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setDurChapter(chapterBean);
        return info;
    }

    public static AudioPlayInfo play(int progress, int duration) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setProgress(progress);
        info.setDuration(duration);
        return info;
    }

    public static AudioPlayInfo timer(int timerMinute) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setTimerMinute(timerMinute);
        return info;
    }

    public static AudioPlayInfo timerDown(int timerMinuteUntilFinish) {
        AudioPlayInfo info = new AudioPlayInfo();
        info.setTimerMinuteUntilFinish(timerMinuteUntilFinish);
        return info;
    }

    public static AudioPlayInfo empty() {
        return new AudioPlayInfo();
    }
}
