package com.monke.monkeybook.bean;

import java.io.Serializable;
import java.util.List;

public class FileSnapshot implements Serializable {
    private RipeFile parent;
    private List<RipeFile> files;
    private int scrollOffset;

    public RipeFile getParent() {
        return parent;
    }

    public void setParent(RipeFile parent) {
        this.parent = parent;
    }

    public List<RipeFile> getFiles() {
        return files;
    }

    public void setFiles(List<RipeFile> files) {
        this.files = files;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

}