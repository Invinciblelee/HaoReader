package com.monke.monkeybook.bean;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.utils.StringUtils;

import java.io.File;
import java.io.Serializable;

public class RipeFile implements Serializable {

    private boolean isSelected;
    private File file;
    private String name;
    private String size;
    private String date;
    private String suffix;
    private String path;
    private Boolean isDirectory;
    private Integer childCount;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        if (!isDirectory()) {
            isSelected = selected;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.path = file.getAbsolutePath();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        if (name == null) {
            name = file.getName();
        }
        return name;
    }

    public String getSize() {
        if (size == null) {
            size = FileHelp.getFileSize(file.length());
        }
        return size;
    }


    public String getDate() {
        if (date == null) {
            date = StringUtils.dateConvert(file.lastModified(), Constant.FORMAT_FILE_DATE);
        }
        return date;
    }

    public boolean isDirectory() {
        if (isDirectory == null) {
            isDirectory = file.isDirectory();
        }
        return isDirectory;
    }

    public String getPath() {
        if (path == null) {
            path = file.getAbsolutePath();
        }
        return path;
    }

    public String getSuffix() {
        if (suffix == null) {
            suffix = FileHelp.getFileSuffix(file).toUpperCase();
        }
        return suffix;
    }

    public int getChildCount() {
        if (childCount == null) {
            childCount = file.list().length;
        }
        return childCount;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof RipeFile) {
            return TextUtils.equals(((RipeFile) obj).path, this.path);
        }
        return super.equals(obj);
    }
}
