package com.monke.monkeybook.presenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.bean.FileSnapshot;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.utils.FileUtil;
import com.monke.monkeybook.utils.RxUtils;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public class FileSelectorPresenterImpl extends BasePresenterImpl<FileSelectorContract.View> implements FileSelectorContract.Presenter, FileFilter {


    private int orderIndex = 0;
    private String[] suffixes;
    private boolean isSingleChoice;
    private boolean checkBookAdded;
    private boolean isImage;

    private boolean sortChanged;
    private FileSnapshot current;

    private final Stack<FileSnapshot> snapshots = new Stack<>();

    private final Collator collator = Collator.getInstance(java.util.Locale.CHINA);


    @Override
    public void init(Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        assert bundle != null;
        isSingleChoice = bundle.getBoolean("isSingleChoice");
        checkBookAdded = bundle.getBoolean("checkBookAdded");
        isImage = bundle.getBoolean("isImage");
        ArrayList<String> list = bundle.getStringArrayList("suffixes");
        assert list != null;
        suffixes = new String[list.size()];
        list.toArray(suffixes);
    }

    @Override
    public Comparator<RipeFile> sort(int orderIndex) {
        if (this.orderIndex != orderIndex) {
            this.orderIndex = orderIndex;
            sortChanged = true;
        }

        return new FileComparator(orderIndex);
    }

    @SuppressLint("SdCardPath")
    @Override
    public void startLoad() {
        mView.showLoading();

        final File root;
        if (FileUtil.checkSDCardAvailable()) {
            root = new File(Environment.getExternalStorageDirectory().getPath());

            mView.showSubtitle(root.getAbsolutePath());
        }else {
            root = null;

            mView.showSubtitle("/sdcard");
        }

        Single.create((SingleOnSubscribe<FileSnapshot>) emitter -> {
            if(root != null) {
                loadRootFiles(root);
            }
            if (current != null) {
                emitter.onSuccess(current);
            } else {
                emitter.onError(new Exception("file load failed!"));
            }
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<FileSnapshot>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(FileSnapshot snapshot) {
                        mView.onShow(snapshot, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideLoading();
                    }
                });

    }

    @Override
    public boolean pop() {
        if (snapshots.empty()) {
            return false;
        }
        current = snapshots.pop();
        if (current != null) {
            if(sortChanged){
                sortFiles(current.getFiles(), orderIndex);
            }
            mView.onShow(current, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean push(RipeFile folder, int offset) {
        current.setScrollOffset(offset);
        snapshots.push(current);
        if (folder.isDirectory()) {
            File[] files = folder.getFile().listFiles(this);
            if (files != null) {
                current = new FileSnapshot();
                current.setParent(folder);
                List<RipeFile> fileList = new ArrayList<>();
                RipeFile ripeFile;
                for (File file : files) {
                    ripeFile = new RipeFile();
                    ripeFile.setFile(file);
                    fileList.add(ripeFile);
                }
                sortFiles(fileList, orderIndex);
                current.setFiles(fileList);
                current.setScrollOffset(offset);
                mView.onShow(current, false);
                return true;
            } else {
                mView.onShow(null, false);
            }
        }
        return false;
    }

    @Override
    public boolean canGoBack() {
        return !snapshots.empty();
    }

    @Override
    public boolean isSingleChoice() {
        return isSingleChoice;
    }

    @Override
    public boolean checkBookAdded() {
        return checkBookAdded;
    }

    @Override
    public boolean isImage() {
        return isImage;
    }

    @Override
    public boolean accept(java.io.File pathname) {
        String fileName = pathname.getName();
        if (fileName.startsWith(".")) {
            return false;
        }
        //文件夹内部数量为0
        if (pathname.isDirectory() && pathname.list().length == 0) {
            return false;
        }

        if (pathname.isDirectory()) {
            return true;
        }

        for (String suffix : suffixes) {
            if (fileName.toUpperCase().endsWith("." + suffix.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void detachView() {

    }

    private void loadRootFiles(File root) {
        File[] files = root.listFiles(FileSelectorPresenterImpl.this);
        if (files != null) {
            current = new FileSnapshot();
            RipeFile parent = new RipeFile();
            parent.setFile(root);
            current.setParent(parent);
            List<RipeFile> fileList = new ArrayList<>();
            RipeFile ripeFile;
            for (File file : files) {
                ripeFile = new RipeFile();
                ripeFile.setFile(file);
                fileList.add(ripeFile);
            }
            sortFiles(fileList, orderIndex);
            current.setFiles(fileList);
        }
    }

    private void sortFiles(List<RipeFile> files, int orderIndex) {
        if (files != null) {
            Collections.sort(files, new FileComparator(orderIndex));
            sortChanged = false;
        }
    }

private class FileComparator implements Comparator<RipeFile> {

    int orderIndex;

    FileComparator(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public int compare(RipeFile file1, RipeFile file2) {
        File o1 = file1.getFile();
        File o2 = file2.getFile();
        if (o1.isDirectory() && o2.isFile()) {
            return -1;
        }
        if (o2.isDirectory() && o1.isFile()) {
            return 1;
        }
        if (orderIndex == 0) {
            return collator.compare(o1.getName(), o2.getName());
        } else if (orderIndex == 1) {
            return collator.compare(o2.getName(), o1.getName());
        } else if (orderIndex == 2) {
            return Long.compare(o1.lastModified(), o2.lastModified());
        } else if (orderIndex == 3) {
            return Long.compare(o2.lastModified(), o1.lastModified());
        } else if (orderIndex == 4) {
            return Long.compare(o1.length(), o2.length());
        }
        return Long.compare(o2.length(), o1.length());
    }
}
}
