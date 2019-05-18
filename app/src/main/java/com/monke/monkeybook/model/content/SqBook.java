package com.monke.monkeybook.model.content;

public class SqBook {
    private String bid;

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFirst_chapter() {
        return first_chapter;
    }

    public void setFirst_chapter(String first_chapter) {
        this.first_chapter = first_chapter;
    }

    public Object getLatest_chapter() {
        return latest_chapter;
    }

    public void setLatest_chapter(Object latest_chapter) {
        this.latest_chapter = latest_chapter;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getLast_chapter_name() {
        return last_chapter_name;
    }

    public void setLast_chapter_name(String last_chapter_name) {
        this.last_chapter_name = last_chapter_name;
    }


    private int page;
    private int count;
    private String last_chapter_name;
    private Object latest_chapter;
    private String first_chapter;
    private String title;
    private String category;
    private String author;
    private String cover;
    private String desc;

}
