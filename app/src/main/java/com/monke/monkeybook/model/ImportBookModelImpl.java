//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.basemvplib.CharsetDetector;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IImportBookModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

public class ImportBookModelImpl extends BaseModelImpl implements IImportBookModel {

    private ImportBookModelImpl() {

    }

    private static class InstanceHolder {
        private static final ImportBookModelImpl SINGLETON = new ImportBookModelImpl();
    }

    public static ImportBookModelImpl getInstance() {
        return InstanceHolder.SINGLETON;
    }

    @Override
    public Observable<LocBookShelfBean> importBook(final File file) {
        return Observable.create(e -> {
            String suffix = FileHelp.getFileSuffix(file);
            LocBookShelfBean locBookShelfBean;
            if (suffix.equalsIgnoreCase(FileHelp.SUFFIX_TXT)) {
                locBookShelfBean = getBookForTxtFile(file);
            } else if (suffix.equalsIgnoreCase(FileHelp.SUFFIX_EPUB)) {
                locBookShelfBean = getBookForEpubFile(file);
            } else {
                locBookShelfBean = null;
            }
            if (locBookShelfBean != null) {
                e.onNext(locBookShelfBean);
            } else {
                e.onError(new Exception("书籍导入失败"));
            }
            e.onComplete();
        });
    }


    private LocBookShelfBean getBookForTxtFile(File file) {
        //判断文件是否存在
        boolean isNew = false;
        BookShelfBean bookShelfBean;
        bookShelfBean = BookshelfHelp.getBookByUrl(file.getAbsolutePath());
        if (bookShelfBean == null) {
            isNew = true;
            bookShelfBean = new BookShelfBean();
            bookShelfBean.setGroup(3);
            bookShelfBean.setHasUpdate(true);
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookShelfBean.setDurChapter(0);
            bookShelfBean.setDurChapterPage(0);
            bookShelfBean.setTag(BookShelfBean.LOCAL_TAG);
            bookShelfBean.setNoteUrl(file.getAbsolutePath());

            String fileName = file.getName().replace(".txt", "").replace(".TXT", "");
            int authorIndex = fileName.indexOf("作者");
            if (authorIndex != -1) {
                bookShelfBean.getBookInfoBean().setAuthor(FormatWebText.getAuthor(fileName.substring(authorIndex)));
                bookShelfBean.getBookInfoBean().setName(fileName.substring(0, authorIndex));
            } else {
                bookShelfBean.getBookInfoBean().setAuthor("");
                bookShelfBean.getBookInfoBean().setName(fileName);
            }

            bookShelfBean.getBookInfoBean().setFinalRefreshData(file.lastModified());
            bookShelfBean.getBookInfoBean().setCoverUrl("");
            bookShelfBean.getBookInfoBean().setNoteUrl(file.getAbsolutePath());
            bookShelfBean.getBookInfoBean().setTag(BookShelfBean.LOCAL_TAG);
        }
        return new LocBookShelfBean(isNew, bookShelfBean);
    }

    private LocBookShelfBean getBookForEpubFile(File file) {
        //判断文件是否存在
        boolean isNew = false;
        BookShelfBean bookShelfBean;
        bookShelfBean = BookshelfHelp.getBookByUrl(file.getAbsolutePath());
        if (bookShelfBean == null) {
            isNew = true;
            bookShelfBean = new BookShelfBean();
            bookShelfBean.setGroup(3);
            bookShelfBean.setHasUpdate(true);
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookShelfBean.setDurChapter(0);
            bookShelfBean.setDurChapterPage(0);
            bookShelfBean.setTag(BookShelfBean.LOCAL_TAG);
            bookShelfBean.setNoteUrl(file.getAbsolutePath());

            EpubReader reader = new EpubReader();

            try {
                Book book = reader.readEpub(new FileInputStream(file));
                Metadata metadata = book.getMetadata();
                bookShelfBean.getBookInfoBean().setName(book.getTitle());
                List<Author> authors = metadata.getAuthors();
                if (authors != null && !authors.isEmpty()) {
                    bookShelfBean.getBookInfoBean().setAuthor(authors.get(0).getFirstname() + authors.get(0).getLastname());
                }

                Resource coverRes = book.getCoverImage();

                File dir = FileHelp.getFolder(Constant.BOOK_COVER_PATH);
                File imageFile = new File(dir, generateFileName(bookShelfBean.getBookInfoBean()));
                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(coverRes.getInputStream());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }

                bookShelfBean.getBookInfoBean().setFinalRefreshData(file.lastModified());
                bookShelfBean.getBookInfoBean().setCoverUrl(imageFile.getAbsolutePath());
                bookShelfBean.getBookInfoBean().setNoteUrl(file.getAbsolutePath());
                bookShelfBean.getBookInfoBean().setTag(BookShelfBean.LOCAL_TAG);
            } catch (IOException e) {
                return null;
            }
        }

        return new LocBookShelfBean(isNew, bookShelfBean);
    }

    private String generateFileName(BookInfoBean bookInfoBean) {
        if (!TextUtils.isEmpty(bookInfoBean.getName())) {
            return "Image-" + bookInfoBean.getName() + ".jpg";
        }
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        return "Image-" + time + ".jpg";
    }
}
