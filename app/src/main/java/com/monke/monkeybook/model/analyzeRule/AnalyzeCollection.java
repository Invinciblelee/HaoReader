package com.monke.monkeybook.model.analyzeRule;

import java.util.List;
import java.util.ListIterator;

final class AnalyzeCollection {

    private ListIterator mIterator;

    AnalyzeCollection(List rawList) {
        this.mIterator = rawList.listIterator();
    }

    boolean hasNext() {
        return mIterator.hasNext();
    }

    Object next() {
        return mIterator.next();
    }
}
