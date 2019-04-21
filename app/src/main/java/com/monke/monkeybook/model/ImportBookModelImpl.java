//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.model;

import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.LocBookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.impl.IImportBookModel;

import java.io.File;

import io.reactivex.Observable;

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
            String fileName = file.getName();
            LocBookShelfBean locBookShelfBean;
            if (fileName.toLowerCase().endsWith(FileHelp.SUFFIX_TXT)) {
                locBookShelfBean = getBookForTxtFile(file);
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
        BookShelfBean bookShelfBean = BookshelfHelp.querySimpleBookByUrl(file.getAbsolutePath());
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
}
