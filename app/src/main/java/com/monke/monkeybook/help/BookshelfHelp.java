package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.BookmarkBeanDao;
import com.monke.monkeybook.dao.ChapterBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.analyzeRule.assit.Global;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.utils.MD5Utils;
import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.monke.monkeybook.help.ChapterContentHelp.getCacheFolderPath;
import static com.monke.monkeybook.help.ChapterContentHelp.getChapterFolderName;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    private static final Type CHAPTER_LIST = new TypeToken<List<ChapterBean>>() {
    }.getType();

    public static List<BookShelfBean> queryAllBook() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfList.get(i).getNoteUrl())).unique();
            if (bookInfoBean != null) {
                bookShelfList.get(i).setBookInfoBean(bookInfoBean);
            } else {
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public static List<BookShelfBean> queryBooksByGroup(int group) {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.Group.eq(group))
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookShelfBean bookShelfBean = bookShelfList.get(i);
            BookInfoBean bookInfoBean = DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).unique();
            if (bookInfoBean != null) {
                bookShelfBean.setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().delete(bookShelfBean);
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public static List<BookShelfBean> queryBooks(String query) {
        List<BookInfoBean> bookInfoBeans = DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().queryBuilder()
                .whereOr(BookInfoBeanDao.Properties.Name.like("%" + query + "%"),
                        BookInfoBeanDao.Properties.Author.like("%" + query + "%"))
                .list();
        List<BookShelfBean> bookShelfBeans = new ArrayList<>();
        if (bookInfoBeans != null) {
            for (BookInfoBean bookInfoBean : bookInfoBeans) {
                BookShelfBean bookShelfBean = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                        .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookInfoBean.getNoteUrl())).build().unique();
                bookShelfBean.setBookInfoBean(bookInfoBean);
                bookShelfBeans.add(bookShelfBean);
            }
        }
        return bookShelfBeans;
    }

    public static BookShelfBean queryBookByUrl(String bookUrl) {
        BookShelfBean bookShelfBean = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl)).build().unique();
        if (bookShelfBean != null) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).unique();
            bookShelfBean.setBookInfoBean(bookInfoBean);

            bookShelfBean.setChapterList(queryChapterList(bookInfoBean.getNoteUrl()));
            bookShelfBean.setBookmarkList(queryBookmarkList(bookInfoBean.getName()));
            return bookShelfBean;
        }
        return null;
    }

    public static BookShelfBean querySimpleBookByUrl(String bookUrl) {
        return DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl)).build().unique();
    }

    public static void removeFromBookShelf(BookShelfBean bookShelfBean) {
        DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().deleteByKey(bookShelfBean.getNoteUrl());
        DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().deleteByKey(bookShelfBean.getBookInfoBean().getNoteUrl());
        DbHelper.getInstance().getDaoSession().getChapterBeanDao().deleteInTx(bookShelfBean.getChapterList());
        //如果正在下载，则移除任务
        DownloadService.removeDownload(ContextHolder.getContext(), bookShelfBean.getNoteUrl());
        cleanBookCache(bookShelfBean);
    }

    public static void cleanBookCache(BookShelfBean bookShelfBean) {
        BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
        if (bookInfo != null) {
            FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + getChapterFolderName(bookInfo.getName(), bookInfo.getAuthor()));
        }
    }

    public static void delChapterList(List<ChapterBean> chapterBeanList) {
        if(chapterBeanList != null && !chapterBeanList.isEmpty()) {
            DbHelper.getInstance().getDaoSession().getChapterBeanDao().deleteInTx(chapterBeanList);
        }
    }

    public static boolean hasCache(BookShelfBean bookShelfBean) {
        return getCacheChapterCount(bookShelfBean) > 0;
    }

    public static int getCacheChapterCount(BookShelfBean bookShelfBean) {
        BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
        File folder = new File(Constant.BOOK_CACHE_PATH, getCacheFolderPath(bookInfo));
        if (folder.exists() && folder.isDirectory()) {
            String[] files = folder.list();
            if (files != null) {
                return files.length;
            }
        }
        return 0;
    }

    public static void saveBookToShelf(BookShelfBean bookShelfBean) {
        if (TextUtils.equals(bookShelfBean.getBookInfoBean().getBookType(), BookType.AUDIO)) {
            bookShelfBean.setGroup(Constant.GROUP_AUDIO);
        }
        DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
        DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
        if (!bookShelfBean.realChapterListEmpty()) {
            saveChapters(bookShelfBean.getNoteUrl(), bookShelfBean.getChapterList());
        }
    }

    public static boolean isInBookShelf(String bookUrl) {
        if (bookUrl == null) {
            return false;
        }

        long count = DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl))
                .count();
        return count > 0;
    }

    public static BookShelfBean getBookFromSearchBook(SearchBookBean searchBookBean) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBookBean.getTag());
        bookShelfBean.setNoteUrl(searchBookBean.getRealNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setLastChapterName(searchBookBean.getLastChapter());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        bookShelfBean.setGroup(0);
        bookShelfBean.setVariableString(searchBookBean.getVariableString());
        BookInfoBean bookInfo = new BookInfoBean();
        bookInfo.setBookType(searchBookBean.getBookType());
        bookInfo.setNoteUrl(searchBookBean.getRealNoteUrl());
        bookInfo.setAuthor(searchBookBean.getAuthor());
        bookInfo.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfo.setName(searchBookBean.getName());
        bookInfo.setTag(searchBookBean.getTag());
        bookInfo.setOrigin(searchBookBean.getOrigin());
        bookInfo.setIntroduce(searchBookBean.getIntroduce());
        bookShelfBean.setBookInfoBean(bookInfo);
        return bookShelfBean;
    }

    public static List<ChapterBean> queryChapterList(String noteUrl) {
        if (chaptersUseDiskCache()) {
            try {
                File file = new File(FileHelp.getCachePath(), getChapterKey(noteUrl) + FileHelp.SUFFIX_CHAP);
                if (file.exists()) {
                    return Global.GSON.fromJson(new FileReader(file), CHAPTER_LIST);
                }
            } catch (Exception ignore) {
            }
            return new ArrayList<>();
        } else {
            return DbHelper.getInstance().getDaoSession().getChapterBeanDao().queryBuilder()
                    .where(ChapterBeanDao.Properties.NoteUrl.eq(noteUrl))
                    .build()
                    .list();
        }
    }


    private static void saveChapters(String noteUrl, List<ChapterBean> chapterBeans) {
        if (chaptersUseDiskCache()) {
            File file = FileHelp.getFile(FileHelp.getCachePath(), getChapterKey(noteUrl) + FileHelp.SUFFIX_CHAP);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(Global.GSON.toJson(chapterBeans));
                writer.flush();
            } catch (Exception ignore) {
            }
        } else {
            DbHelper.getInstance().getDaoSession().getChapterBeanDao().insertOrReplaceInTx(chapterBeans);
        }
    }

    private static String getChapterKey(String noteUrl) {
        return StringUtils.checkNull(MD5Utils.strToMd5By32(noteUrl), noteUrl);
    }

    private static boolean chaptersUseDiskCache() {
        return AppConfigHelper.get().getBoolean(ContextHolder.getContext().getString(R.string.pk_chapter_disk_cache), false);
    }

    public static void saveBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
    }

    public static void delBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
    }

    public static List<BookmarkBean> queryBookmarkList(String bookName) {
        return DbHelper.getInstance().getDaoSession().getBookmarkBeanDao().queryBuilder()
                .where(BookmarkBeanDao.Properties.BookName.eq(bookName))
                .build()
                .list();
    }

    /**
     * 排序
     */
    public static void order(List<BookShelfBean> books, int bookshelfOrder) {
        if (books == null || books.size() == 0) {
            return;
        }
        switch (bookshelfOrder) {
            case 0:
                Collections.sort(books, (o1, o2) -> Long.compare(o2.getFinalDate(), o1.getFinalDate()));
                break;
            case 1:
                Collections.sort(books, (o1, o2) -> Long.compare(o2.getFinalRefreshData(), o1.getFinalRefreshData()));
                break;
            case 2:
                Collections.sort(books, (o1, o2) -> Integer.compare(o1.getSerialNumber(), o2.getSerialNumber()));
                break;
        }
    }

    public static void clearBookshelf() {
        DbHelper.getInstance().getDaoSession().getBookShelfBeanDao().deleteAll();
        DbHelper.getInstance().getDaoSession().getBookInfoBeanDao().deleteAll();
        DbHelper.getInstance().getDaoSession().getChapterBeanDao().deleteAll();
        cleanCaches();
    }

    public static void cleanCaches() {
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH);
        FileHelp.getFolder(Constant.BOOK_CACHE_PATH);
    }
}
