package com.monke.monkeybook.widget.page.animation;

public enum Direction {
    NONE(true), NEXT(true), PREV(true), UP(false), DOWN(false);

    public final boolean isHorizontal;

    Direction(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }
}