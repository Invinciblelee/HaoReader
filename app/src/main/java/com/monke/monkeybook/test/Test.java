package com.monke.monkeybook.test;

import android.widget.ListView;

public class Test {

    private Callback callback;

    public Test(Callback callback) {
        this.callback = callback;
    }

    public void do1(){
        callback.do1();
    }

    public void do2(){
        ListView
        callback.do2();
    }

}
