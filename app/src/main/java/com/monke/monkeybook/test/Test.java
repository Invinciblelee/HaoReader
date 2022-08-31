package com.monke.monkeybook.test;

public class Test {

    private Callback callback;

    public Test(Callback callback) {
        this.callback = callback;
    }

    public void do1(){
        callback.do1();
    }

    public void do2(){
        callback.do2();
    }

}
