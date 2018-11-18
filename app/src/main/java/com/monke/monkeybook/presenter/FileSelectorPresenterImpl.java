package com.monke.monkeybook.presenter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.utils.RxUtils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public class FileSelectorPresenterImpl extends BasePresenterImpl<FileSelectorContract.View> implements FileSelectorContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_ID = 99;

    private String title;
    private FileSelectorContract.MediaType mediaType;
    private String[] suffixes;
    private int orderIndex = 0;
    private LoaderManager loaderManager;

    private Collator collator = Collator.getInstance(java.util.Locale.CHINA);

    private final List<RipeFile> files = new ArrayList<>();

    @Override
    public void init(AppCompatActivity activity) {
        Intent intent = activity.getIntent();
        title = intent.getStringExtra("title");
        mediaType = intent.getParcelableExtra("mediaType");
        ArrayList<String> list = intent.getStringArrayListExtra("suffixes");
        suffixes = new String[list.size()];
        list.toArray(suffixes);

        loaderManager = LoaderManager.getInstance(activity);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public FileSelectorContract.MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public void sort(int orderIndex) {
        this.orderIndex = orderIndex;
        if (files.isEmpty()) {
            return;
        }
        mView.showLoading();
        Single.create((SingleOnSubscribe<List<RipeFile>>) emitter -> {
            sortFiles(files, this.orderIndex);
            emitter.onSuccess(files);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<RipeFile>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<RipeFile> files) {
                        mView.onLoadFinish(files);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideLoading();
                    }
                });
    }

    @Override
    public void query(String query) {
        if (files.isEmpty()) {
            return;
        }

        if (query == null) {
            mView.onLoadFinish(files);
            return;
        }

        mView.showLoading();
        Single.create((SingleOnSubscribe<List<RipeFile>>) emitter -> {
            List<RipeFile> newFiles = new ArrayList<>();
            for (RipeFile file : files) {
                if (file.getName().contains(query)) {
                    newFiles.add(file);
                }
            }
            emitter.onSuccess(newFiles);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<RipeFile>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<RipeFile> files) {
                        mView.onLoadFinish(files);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideLoading();
                    }
                });
    }

    @Override
    public void startLoad() {
        if (loaderManager.getLoader(TASK_ID) == null) {
            loaderManager.initLoader(TASK_ID, null, this);
        } else {
            loaderManager.restartLoader(TASK_ID, null, this);
        }
    }

    @Override
    public void detachView() {
        loaderManager.destroyLoader(TASK_ID);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(mView.getContext(),
                mediaType == FileSelectorContract.MediaType.FIlE ? MediaStore.Files.getContentUri("external")
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.SIZE
                },
                buildSelection(suffixes.length),
                buildSelectionArgs(suffixes),
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        loadCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private void loadCursor(Cursor cursor) {
        Single.create((SingleOnSubscribe<List<RipeFile>>) emitter -> {
            files.clear();

            int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                RipeFile file = new RipeFile();
                file.setPath(cursor.getString(pathIndex));
                file.setName(file.getPath().substring(file.getPath().lastIndexOf("/") + 1));
                file.setSize(cursor.getLong(sizeIndex));
                file.setSuffix(FileHelp.getFileSuffix(file.getPath()).toUpperCase());
                File f = new File(file.getPath());
                file.setDate(f.lastModified());

                files.add(file);

                cursor.moveToNext();
            }
            sortFiles(files, this.orderIndex);
            emitter.onSuccess(files);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<RipeFile>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<RipeFile> files) {
                        mView.onLoadFinish(files);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.hideLoading();
                    }
                });
    }

    private String buildSelection(int selectionArgsLength) {
        StringBuilder builder = new StringBuilder();
        builder.append(MediaStore.Files.FileColumns.SIZE)
                .append(" > 0 and ")
                .append(MediaStore.Files.FileColumns.SIZE)
                .append(" < 5242880 and ");
        for (int i = 0; i < selectionArgsLength; i++) {
            builder.append(MediaStore.Files.FileColumns.DATA).append(" like ? ");
            if (i < selectionArgsLength - 1) {
                builder.append(" or ");
            }
        }
        builder.append("COLLATE NOCASE");
        return builder.toString();
    }

    private String[] buildSelectionArgs(String[] selectionArgs) {
        String[] args = new String[selectionArgs.length];
        for (int i = 0, length = selectionArgs.length; i < length; i++) {
            args[i] = "%." + selectionArgs[i];
        }
        return args;
    }

    private int compare(long val1, long val2) {
        return Long.compare(val1, val2);
    }

    private void sortFiles(List<RipeFile> files, int orderIndex) {
        Collections.sort(files, (o1, o2) -> {
            if (orderIndex == 0) {
                return collator.compare(o1.getName(), o2.getName());
            } else if (orderIndex == 1) {
                return compare(o1.getDate(), o2.getDate());
            } else if (orderIndex == 2) {
                return compare(o1.getSize(), o2.getSize());
            } else if (orderIndex == 3) {
                return compare(o2.getDate(), o1.getDate());
            }
            return compare(o2.getSize(), o1.getSize());
        });
    }
}
