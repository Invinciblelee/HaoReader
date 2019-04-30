package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookContentBean;
import com.monke.monkeybook.bean.BookInfoBean;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookmarkBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.dao.BookInfoBeanDao;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.BookmarkBeanDao;
import com.monke.monkeybook.dao.ChapterBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.service.DownloadService;

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

    public static String getCacheFolderPath(DownloadBookBean book) {
        return getChapterFolderName(book.getName(), book.getAuthor()) + File.separator + formatFileName(book.getTag()) + File.separator;
    }

    public static String getCacheFolderPath(BookInfoBean book) {
        return getChapterFolderName(book.getName(), book.getAuthor()) + File.separator + formatFileName(book.getTag()) + File.separator;
    }

    public static String getCacheFileName(ChapterBean chapter) {
        return String.format(Locale.getDefault(), "%d-%s", chapter.getDurChapterIndex(), chapter.getDurChapterName());
    }

    public static String getCacheFileName(BookContentBean content) {
        return String.format(Locale.getDefault(), "%d-%s", content.getDurChapterIndex(), content.getDurChapterName());
    }

    public static String getChapterFolderName(String name, String author) {
        return formatFileName(String.format(Locale.getDefault(), "%s(%s)", name, author));
    }

    public static boolean isChapterCached(DownloadBookBean book, ChapterBean chapter) {
        return isChapterCached(getCacheFolderPath(book), getCacheFileName(chapter));
    }

    public static boolean isChapterCached(BookInfoBean book, ChapterBean chapter) {
        return isChapterCached(getCacheFolderPath(book), getCacheFileName(chapter));
    }

    /**
     * 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存过)
     */
    // be careful to use this method, the storage path (folderName) has been changed
    private static boolean isChapterCached(String folderName, String fileName) {
        File file = new File(Constant.BOOK_CACHE_PATH + folderName, formatFileName(fileName) + FileHelp.SUFFIX_NB);
        return file.exists();
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
        return FileHelp.getFile(Constant.BOOK_CACHE_PATH + folderName, formatFileName(fileName) + FileHelp.SUFFIX_NB);
    }

    private static String formatFileName(String fileName) {
        return fileName.replace("/", "")
                .replace(":", "")
                .replace(".", "");
    }

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
        DownloadService.removeDownload(MApplication.getInstance(), bookShelfBean.getNoteUrl());
        cleanBookCache(bookShelfBean);
    }

    public static void cleanBookCache(BookShelfBean bookShelfBean) {
        BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
        if (bookInfo != null) {
            FileHelp.deleteFile(Constant.BOOK_CACHE_PATH + getChapterFolderName(bookInfo.getName(), bookInfo.getAuthor()));
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
            DbHelper.getInstance().getDaoSession().getChapterBeanDao().insertOrReplaceInTx(bookShelfBean.getChapterList());
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
        return DbHelper.getInstance().getDaoSession().getChapterBeanDao().queryBuilder()
                .where(ChapterBeanDao.Properties.NoteUrl.eq(noteUrl))
                .build()
                .list();
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
