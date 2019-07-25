package com.monke.monkeybook.presenter;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.rxjava.RxExecutors;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.presenter.contract.CacheManagerContract;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CacheManagerPresenter extends BasePresenterImpl<CacheManagerContract.View> implements CacheManagerContract.Presenter {

    private final CompositeDisposable disposables = new CompositeDisposable();

    private RandomAccessFile accessFile;
    private Iterator<WrappedFile> wrappedFiles;
    private int count;

    @Override
    public void queryBooks() {
        Single.create((SingleOnSubscribe<List<BookShelfBean>>) emitter -> {
            final List<BookShelfBean> allBooks = BookshelfHelp.queryAllBook();
            final List<BookShelfBean> bookShelfBeans = new ArrayList<>();
            if (allBooks != null) {
                for (BookShelfBean bookShelfBean : allBooks) {
                    if (BookshelfHelp.hasCache(bookShelfBean)) {
                        bookShelfBeans.add(bookShelfBean);
                    }
                }
            }
            emitter.onSuccess(bookShelfBeans);
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onSuccess(List<BookShelfBean> bookShelfBeans) {
                        mView.showBookList(bookShelfBeans);
                        mView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideProgress();
                    }
                });
    }

    @Override
    public void extractBookCache(BookShelfBean bookShelfBean, boolean force) {
        cancel();
        Observable.create((ObservableOnSubscribe<List<WrappedFile>>) emitter -> {
            final File bookFolder = FileHelp.getFolder(Constant.AUDIO_BOOK_PATH);
            final File bookFile = new File(bookFolder, bookShelfBean.getBookInfoBean().getName() + ".txt");
            if (bookFile.exists()) {
                if (!force) {
                    emitter.onError(new IllegalAccessException("tip"));
                    return;
                }
                bookFile.delete();
            }
            bookFile.createNewFile();
            final File folder = new File(Constant.BOOK_CHAPTER_PATH, ChapterContentHelp.getCacheFolderPath(bookShelfBean.getBookInfoBean()));
            if (!folder.isDirectory()) {
                emitter.onError(new NullPointerException("delete"));
                return;
            }
            final File[] files = folder.listFiles();
            if (!folder.exists() || files == null || files.length == 0) {
                emitter.onError(new NullPointerException("delete"));
                return;
            }

            final List<File> fileList = new ArrayList<>(Arrays.asList(files));
            sortFile(fileList);

            accessFile = new RandomAccessFile(bookFile, "rw");
            accessFile.seek(0);

            final List<WrappedFile> wrappedFiles = new ArrayList<>();
            for (int i = 0, size = fileList.size(); i < size; i++) {
                wrappedFiles.add(new WrappedFile(i, fileList.get(i)));
            }
            emitter.onNext(wrappedFiles);
            emitter.onComplete();
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<WrappedFile>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<WrappedFile> wrappedFiles) {
                        startExtract(wrappedFiles);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if ("delete".equals(e.getMessage())) {
                            mView.removeItem(bookShelfBean);
                            mView.toast("缓存文件不存在");
                        } else if ("tip".equals(e.getMessage())) {
                            mView.showExtractTip(bookShelfBean);
                        } else {
                            mView.toast("提取失败");
                        }
                    }
                });
    }

    @Override
    public void cancel() {
        disposables.clear();
        if (accessFile != null) {
            IOUtils.close(accessFile);
            accessFile = null;
        }
    }

    private void sortFile(List<File> fileList) {
        Collections.sort(fileList, (o1, o2) -> {
            int i1 = StringUtils.stringToInt(o1.getName().split("-")[0]);
            int i2 = StringUtils.stringToInt(o2.getName().split("-")[0]);
            return Integer.compare(i1, i2);
        });
    }


    @Override
    public void detachView() {
        super.detachView();
        disposables.dispose();
    }

    private void startExtract(List<WrappedFile> files) {
        this.wrappedFiles = files.iterator();
        this.count = files.size();

        startNext();
    }


    private void writeFile(WrappedFile wrappedFile) {
        Single.create((SingleOnSubscribe<Integer>) emitter -> {
            File file = wrappedFile.file;
            String fileName = file.getName().split("-")[1];
            String name = fileName.substring(0, fileName.lastIndexOf("."));
            accessFile.write(name.getBytes());
            accessFile.write("\r\n".getBytes());
            Reader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                accessFile.write(line.getBytes());
                accessFile.write("\r\n".getBytes());
            }
            emitter.onSuccess(wrappedFile.index);
        }).subscribeOn(RxExecutors.getDefault())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        mView.updateProgress(count, integer);
                        startNext();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateProgress(count, wrappedFile.index);
                        startNext();
                    }
                });
    }

    private void startNext() {
        if (wrappedFiles != null && wrappedFiles.hasNext()) {
            writeFile(wrappedFiles.next());
        } else {
            mView.updateProgress(count, count);
        }
    }

    private static class WrappedFile {
        private int index;
        private File file;

        private WrappedFile(int index, File file) {
            this.index = index;
            this.file = file;
        }
    }
}
