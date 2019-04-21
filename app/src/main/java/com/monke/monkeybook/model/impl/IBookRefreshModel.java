package com.monke.monkeybook.model.impl;

public interface IBookRefreshModel {

    void queryBooks(int group, boolean refresh);

    void startRefreshBook();

    void stopRefreshBook();
}
