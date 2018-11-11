package com.monke.monkeybook.help;

import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.BookSourceBeanDao;
import com.monke.monkeybook.dao.BookmarkBeanDao;
import com.monke.monkeybook.dao.ChapterListBeanDao;
import com.monke.monkeybook.dao.DbHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by GKF on 2018/1/18.
 * 添加删除Book
 */

public class BookshelfHelp {

    public static String getCachePathName(ChapterListBean chapter) {
        return formatFileName(chapter.getBookName() + "-" + chapter.getTag());
    }

    public static String getCachePathName(BookInfoBean book) {
        return formatFileName(book.getName() + "-" + book.getTag());
    }

    public static String getCacheFileName(int chapterIndex, String chapterName) {
        return String.format(Locale.getDefault(), "%d-%s", chapterIndex, chapterName);
    }

    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存过)
     */
    // be careful to use this method, the storage path (folderName) has been changed
    public static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
        return file.exists();
    }

    public static boolean isChapterCached(BookInfoBean book, ChapterListBean chapter) {
        return isChapterCached(getCachePathName(book), getCacheFileName(chapter.getDurChapterIndex(), chapter.getDurChapterName()));
    }

    /**
     * 删除章节文件
     */
    public static void delChapter(String folderName, String fileName) {
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    /**
     * 存储章节
     */
    public static boolean saveChapterInfo(String folderName, String fileName, String content) {
        if (content == null) {
            return false;
        }
        File file = getBookFile(folderName, formatFileName(fileName));
        //获取流并存储
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建或获取存储文件
     */
    public static File getBookFile(String folderName, String fileName) {
        return FileHelp.getFile(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    private static String formatFileName(String fileName) {
        return fileName.replace("/", "")
                .replace(":", "")
                .replace(".", "");
    }

    public static List<BookShelfBean> getAllBook() {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
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

    public static List<BookShelfBean> getBooksByGroup(int group) {
        List<BookShelfBean> bookShelfList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.Group.eq(group))
                .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
        for (int i = 0; i < bookShelfList.size(); i++) {
            BookShelfBean bookShelfBean = bookShelfList.get(i);
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).unique();
            if (bookInfoBean != null) {
                bookShelfBean.setBookInfoBean(bookInfoBean);
            } else {
                DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfBean);
                bookShelfList.remove(i);
                i--;
            }
        }
        return bookShelfList;
    }

    public static List<BookShelfBean> queryBooks(String query) {
        List<BookInfoBean> bookInfoBeans = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                .whereOr(BookInfoBeanDao.Properties.Name.like("%" + query + "%"), BookInfoBeanDao.Properties.Author.like("%" + query + "%"))
                .list();
        List<BookShelfBean> bookShelfBeans = new ArrayList<>();
        if (bookInfoBeans != null) {
            for (BookInfoBean bookInfoBean : bookInfoBeans) {
                BookShelfBean bookShelfBean = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                        .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookInfoBean.getNoteUrl())).build().unique();
                bookShelfBean.setBookInfoBean(bookInfoBean);
                bookShelfBeans.add(bookShelfBean);
            }
        }
        return bookShelfBeans;
    }

    public static BookShelfBean getBookByUrl(String bookUrl, boolean needChapters) {
        BookShelfBean bookShelfBean = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl)).build().unique();
        if (bookShelfBean != null) {
            BookInfoBean bookInfoBean = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                    .where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).unique();
            bookShelfBean.setBookInfoBean(bookInfoBean);
            if (bookInfoBean != null && needChapters) {
                bookShelfBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookShelfBean.upChapterListSize();
                bookShelfBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
            }
            return bookShelfBean;
        }
        return null;
    }

    public static BookShelfBean getBookByName(String name, String author) {
        List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder()
                .where(BookInfoBeanDao.Properties.Name.eq(name),
                        BookInfoBeanDao.Properties.Author.eq(author),
                        BookInfoBeanDao.Properties.Tag.notEq(BookShelfBean.LOCAL_TAG)).list();
        if (temp != null && temp.size() > 0) {
            BookInfoBean bookInfoBean = temp.get(0);
            BookShelfBean bookShelfBean = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookInfoBean.getNoteUrl())).build().unique();

            if (bookShelfBean != null) {
                bookShelfBean.setChapterList(getChapterList(bookInfoBean.getNoteUrl()));
                bookShelfBean.upChapterListSize();
                bookShelfBean.setBookmarkList(getBookmarkList(bookInfoBean.getName()));
                bookShelfBean.setBookInfoBean(bookInfoBean);
                return bookShelfBean;
            }
        }
        return null;
    }

    public static void removeFromBookShelf(BookShelfBean bookShelfBean) {
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteByKey(bookShelfBean.getNoteUrl());
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteByKey(bookShelfBean.getBookInfoBean().getNoteUrl());
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelfBean.getChapterList());
        cleanBookCache(bookShelfBean);
    }

    public static void cleanBookCache(BookShelfBean bookShelfBean) {
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + getCachePathName(bookShelfBean.getBookInfoBean()));
    }

    public static void saveBookToShelf(BookShelfBean bookShelfBean) {
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelfBean.getBookInfoBean());
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
        if (!bookShelfBean.realChapterListEmpty()) {
            DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
        }
    }

    public static boolean isInBookShelf(String bookUrl) {
        if (bookUrl == null) {
            return false;
        }

        long count = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                .where(BookShelfBeanDao.Properties.NoteUrl.eq(bookUrl))
                .count();
        return count > 0;
    }

    public static BookSourceBean getBookSourceByTag(String tag) {
        if (tag == null)
            return null;
        return DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().queryBuilder()
                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(tag)).unique();
    }

    public static void saveBookSource(BookSourceBean sourceBean) {
        if (sourceBean != null) {
            DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().insertOrReplace(sourceBean);
        }
    }

    public static BookShelfBean getBookFromSearchBook(SearchBookBean searchBookBean) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBookBean.getTag());
        bookShelfBean.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        BookInfoBean bookInfo = new BookInfoBean();
        bookInfo.setNoteUrl(searchBookBean.getNoteUrl());
        bookInfo.setAuthor(searchBookBean.getAuthor());
        bookInfo.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfo.setName(searchBookBean.getName());
        bookInfo.setTag(searchBookBean.getTag());
        bookInfo.setOrigin(searchBookBean.getOrigin());
        bookInfo.setIntroduce(searchBookBean.getIntroduce());
        bookInfo.setChapterUrl(searchBookBean.getChapterUrl());
        bookShelfBean.setBookInfoBean(bookInfo);
        return bookShelfBean;
    }

    public static List<ChapterListBean> getChapterList(String noteUrl) {
        return DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder()
                .where(ChapterListBeanDao.Properties.NoteUrl.eq(noteUrl))
                .build()
                .list();
    }

    public static void saveBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
    }

    public static void delBookmark(BookmarkBean bookmarkBean) {
        DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
    }

    public static List<BookmarkBean> getBookmarkList(String bookName) {
        return DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().queryBuilder()
                .where(BookmarkBeanDao.Properties.BookName.eq(bookName))
                .build()
                .list();
    }

    public static String getReadProgress(BookShelfBean bookShelfBean) {
        return getReadProgress(bookShelfBean.getDurChapter(), bookShelfBean.getChapterListSize(), 0, 0);
    }

    public static String getReadProgress(int durChapterIndex, int chapterAll, int durPageIndex, int durPageAll) {
        DecimalFormat df = new DecimalFormat("0.0%");
        if (chapterAll == 0) {
            return "0.0%";
        } else if (durPageAll == 0) {
            return df.format(durChapterIndex * 1.0f / chapterAll);
        }
        String percent = df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll);
        if (percent.equals("100.0%") && (durChapterIndex + 1 != chapterAll || durPageIndex + 1 != durPageAll)) {
            percent = "99.9%";
        }
        return percent;
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
        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteAll();
        DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteAll();
        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteAll();
        cleanCaches();
    }

    public static void cleanCaches() {
        FileHelp.deleteFile(Constant.BOOK_CACHE_PATH);
        FileHelp.getFolder(Constant.BOOK_CACHE_PATH);
    }
}
