package com.monke.monkeybook.bean;

public class RxBusEvent {

    private int id;
    private Object obj;

    public RxBusEvent(int id, Object obj) {
        this.id = id;
        this.obj = obj;
    }

    public RxBusEvent(Object obj) {
        this.obj = obj;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
