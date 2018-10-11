package com.monke.monkeybook.bean;

public class SearchSuggestionBean implements Comparable<SearchSuggestionBean>{

    private int category; //0 历史 1作者 2书名
    private String value;

    public SearchSuggestionBean(int category, String value) {
        this.category = category;
        this.value = value;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(SearchSuggestionBean o) {
        return this.category - o.category;
    }
}
