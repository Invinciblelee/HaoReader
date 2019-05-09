package com.monke.monkeybook.model.analyzeRule;

import java.util.Iterator;
import java.util.List;

final class AnalyzeCollection {

    private Iterator mIterator;

    AnalyzeCollection(List rawList) {
        this.mIterator = rawList.iterator();
    }

    boolean hasNext() {
        return mIterator.hasNext();
    }

    Object next() {
        return mIterator.next();
    }

}
