package com.monke.monkeybook.widget.page;

import android.text.TextUtils;

import com.monke.basemvplib.EncodingDetect;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.utils.IOUtils;
import com.monke.monkeybook.utils.MD5Utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.monke.monkeybook.help.FileHelp.BLANK;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CATEGORY_EMPTY;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_CHANGE_CHARSET;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_PARING;
import static com.monke.monkeybook.widget.page.PageStatus.STATUS_PARSE_ERROR;

/**
 * Created by newbiechen on 17-7-1.
 * 问题:
 * 1. 异常处理没有做好
 */

public class LocalPageLoader extends PageLoader {
    //默认从文件中获取数据的长度
    private final static int BUFFER_SIZE = 512 * 1024;
    //没有标题的时候，每个章节的最大长度
    private final static int MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024;

    //从序章找作者名称
    private static final String AUTHOR_PATTERN = "(?<=作者[:：])(.*?)(\r\n|,|。|；)";

    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private static final String[] CHAPTER_PATTERNS = new String[]{"^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\f\t])(.{0,30})$",
            "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"};

    //章节解析模式
    private Pattern mChapterPattern = null;
    //获取书本的文件
    private File mBookFile;
    //编码类型
    private Charset mCharset;

    private Disposable mChapterDisp = null;

    LocalPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
        if (getCollBook().getChapterListSize() == 0) {
            setCurrentStatus(STATUS_PARING, false);
        }
    }

    /**
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     *
     * @throws IOException
     */
    private List<ChapterBean> loadChapters() throws IOException {
        List<SubChapter> chapters = new ArrayList<>();
        //获取文件流
        RandomAccessFile bookStream = new RandomAccessFile(mBookFile, "r");
        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        boolean hasChapter = checkChapterType(bookStream);
        //加载章节
        byte[] buffer = new byte[BUFFER_SIZE];
        //获取到的块起始点，在文件中的位置
        int curOffset = 0;
        //block的个数
        int blockPos = 0;
        //读取的长度
        int length;

        //获取文件中的数据到buffer，直到没有数据为止
        while ((length = bookStream.read(buffer, 0, buffer.length)) > 0) {
            ++blockPos;
            //如果存在Chapter
            if (hasChapter) {
                //将数据转换成String
                String blockContent = new String(buffer, 0, length, mCharset);
                //当前Block下使过的String的指针
                int seekPos = 0;
                //进行正则匹配
                Matcher matcher = mChapterPattern.matcher(blockContent);
                //如果存在相应章节
                while (matcher.find()) {
                    //获取匹配到的字符在字符串中的起始位置
                    int chapterStart = matcher.start();

                    //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                    //第一种情况一定是序章 第二种情况可能是上一个章节的内容
                    if (seekPos == 0 && chapterStart != 0) {
                        //获取当前章节的内容
                        String chapterContent = blockContent.substring(seekPos, chapterStart);
                        //设置指针偏移
                        seekPos += chapterContent.length();

                        if (curOffset == 0 && chapters.isEmpty()) { //如果当前对整个文件的偏移位置为0的话，那么就是序章
                            //创建序章
                            SubChapter preChapter = new SubChapter();
                            preChapter.title = "序章";
                            preChapter.start = 0;
                            preChapter.end = chapterContent.getBytes(mCharset).length; //获取String的byte值,作为最终值

                            setupAuthorName(chapterContent);

                            //如果序章大小大于30才添加进去
                            if (preChapter.end - preChapter.start > 30) {
                                chapters.add(preChapter);
                            }

                            //创建当前章节
                            SubChapter curChapter = new SubChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = preChapter.end;
                            chapters.add(curChapter);
                        } else {  //否则就block分割之后，上一个章节的剩余内容
                            //获取上一章节
                            SubChapter lastChapter = chapters.get(chapters.size() - 1);
                            //将当前段落添加上一章去
                            lastChapter.end += chapterContent.getBytes(mCharset).length;

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter);
                            }

                            //创建当前章节
                            SubChapter curChapter = new SubChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = lastChapter.end;
                            chapters.add(curChapter);
                        }
                    } else {
                        //是否存在章节
                        if (chapters.size() != 0) {
                            //获取章节内容
                            String chapterContent = blockContent.substring(seekPos, matcher.start());
                            seekPos += chapterContent.length();

                            //获取上一章节
                            SubChapter lastChapter = chapters.get(chapters.size() - 1);
                            lastChapter.end = lastChapter.start + chapterContent.getBytes(mCharset).length;

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter);
                            }

                            //创建当前章节
                            SubChapter curChapter = new SubChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = lastChapter.end;
                            chapters.add(curChapter);
                        }
                        //如果章节不存在则创建章节
                        else {
                            SubChapter curChapter = new SubChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = 0;
                            chapters.add(curChapter);
                        }
                    }
                }
            }
            //进行本地虚拟分章
            else {
                //章节在buffer的偏移量
                int chapterOffset = 0;
                //当前剩余可分配的长度
                int strLength = length;
                //分章的位置
                int chapterPos = 0;

                while (strLength > 0) {
                    ++chapterPos;
                    //是否长度超过一章
                    if (strLength > MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        int end = length;
                        //寻找换行符作为终止点
                        for (int i = chapterOffset + MAX_LENGTH_WITH_NO_CHAPTER; i < length; ++i) {
                            if (buffer[i] == BLANK) {
                                end = i;
                                break;
                            }
                        }
                        SubChapter chapter = new SubChapter();
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")";
                        chapter.start = curOffset + chapterOffset + 1;
                        chapter.end = curOffset + end;
                        chapters.add(chapter);
                        //减去已经被分配的长度
                        strLength = strLength - (end - chapterOffset);
                        //设置偏移的位置
                        chapterOffset = end;
                    } else {
                        SubChapter chapter = new SubChapter();
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")";
                        chapter.start = curOffset + chapterOffset + 1;
                        chapter.end = curOffset + length;
                        chapters.add(chapter);
                        strLength = 0;
                    }
                }
            }

            //block的偏移点
            curOffset += length;

            if (hasChapter) {
                //设置上一章的结尾
                SubChapter lastChapter = chapters.get(chapters.size() - 1);
                lastChapter.end = curOffset;
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc();
                System.runFinalization();
            }
        }

        List<ChapterBean> chapterList = new ArrayList<>();
        for (int i = 0, size = chapters.size(); i < size; i++) {
            SubChapter chapter = chapters.get(i);
            ChapterBean bean = new ChapterBean();
            bean.setDurChapterIndex(i);
            bean.setDurChapterUrl(MD5Utils.strToMd5By16(mBookFile.getAbsolutePath() + i + chapter.title));
            bean.setDurChapterName(chapter.title);
            bean.setStart(chapter.start);
            bean.setEnd(chapter.end);
            bean.setNoteUrl(mBookFile.getAbsolutePath());
            chapterList.add(bean);
        }
        IOUtils.close(bookStream);

        System.gc();
        System.runFinalization();

        return chapterList;
    }

    /**
     * 从序章找作者名称
     */
    private void setupAuthorName(String firstChapter) {
        String author = getCollBook().getBookInfoBean().getAuthor();
        if (TextUtils.isEmpty(author)) {
            Pattern pattern = Pattern.compile(AUTHOR_PATTERN);
            Matcher m = pattern.matcher(firstChapter);
            if (m.find()) {
                getCollBook().getBookInfoBean().setAuthor(m.group().trim());
            }
        }
    }

    /**
     * 从文件中提取一章的内容
     */
    private byte[] getChapterContent(ChapterBean chapter) {
        RandomAccessFile bookStream = null;
        try {
            bookStream = new RandomAccessFile(mBookFile, "r");
            bookStream.seek(chapter.getStart());
            int extent = chapter.getEnd() - chapter.getStart();
            byte[] content = new byte[extent];
            bookStream.read(content, 0, extent);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bookStream);
        }

        return new byte[0];
    }

    /**
     * 1. 检查文件中是否存在章节名
     * 2. 判断文件中使用的章节名类型的正则表达式
     *
     * @return 是否存在章节名
     */
    private boolean checkChapterType(RandomAccessFile bookStream) throws IOException {
        //首先获取128k的数据
        byte[] buffer = new byte[BUFFER_SIZE / 4];
        int length = bookStream.read(buffer, 0, buffer.length);
        //进行章节匹配
        for (String str : CHAPTER_PATTERNS) {
            Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(new String(buffer, 0, length, mCharset));
            //如果匹配存在，那么就表示当前章节使用这种匹配方式
            if (matcher.find()) {
                mChapterPattern = pattern;
                //重置指针位置
                bookStream.seek(0);
                return true;
            }
        }

        //重置指针位置
        bookStream.seek(0);
        return false;
    }

    @Override
    public void closeBook() {
        super.closeBook();
        if (mChapterDisp != null) {
            mChapterDisp.dispose();
            mChapterDisp = null;
        }
    }

    @Override
    public void refreshChapterList() {
        // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
        mBookFile = new File(getCollBook().getNoteUrl());
        //获取文件编码
        String charsetName = getCollBook().getBookInfoBean().getCharset();
        if (TextUtils.isEmpty(charsetName)) {
            charsetName = EncodingDetect.getJavaEncode(mBookFile);
            mCharset = Charset.forName(charsetName);
            getCollBook().getBookInfoBean().setCharset(charsetName);
        } else {
            mCharset = Charset.forName(charsetName);
        }
        // 判断文件是否已经加载过，并具有缓存
        if (!getCollBook().getHasUpdate() && !getCollBook().realChapterListEmpty()) {
            setChapterListPrepared();

            // 加载并显示当前章节
            skipToChapter(getCollBook().getDurChapter(), getCollBook().getDurChapterPage());

            //提示目录加载完成
            dispatchCategoryFinishEvent(getCollBook().getChapterList());
        } else {
            // 通过RxJava异步处理分章事件
            Observable.create((ObservableOnSubscribe<List<ChapterBean>>) e -> {
                List<ChapterBean> chapterList = loadChapters();
                e.onNext(chapterList);
                e.onComplete();
            }).subscribeOn(Schedulers.single())
                    .flatMap(chapterList -> {
                        getCollBook().setChapterList(chapterList, true);
                        return Observable.just(getCollBook());
                    })
                    .doAfterNext(bookShelfBean -> {
                        // 存储章节到数据库
                        bookShelfBean.setHasUpdate(false);
                        bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                        if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                            BookshelfHelp.saveBookToShelf(bookShelfBean);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mChapterDisp = d;
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            if (bookShelfBean.realChapterListEmpty()) {
                                setCurrentStatus(STATUS_CATEGORY_EMPTY);
                            } else {
                                setChapterListPrepared();

                                // 加载并显示当前章节
                                skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());

                                // 提示目录加载完成
                                dispatchCategoryFinishEvent(bookShelfBean.getChapterList());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            setCurrentStatus(STATUS_PARSE_ERROR);
                        }
                    });
        }
    }

    @Override
    protected BufferedReader getChapterReader(ChapterBean chapter) throws Exception {
        //从文件中获取数据
        byte[] content = getChapterContent(chapter);
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        return new BufferedReader(new InputStreamReader(bais, mCharset));
    }

    @Override
    protected boolean chapterNotCached(ChapterBean chapter) {
        return false;
    }

    public void updateCharset() {
        setCurrentStatus(STATUS_CHANGE_CHARSET);
        refreshChapterList();
    }

    private class SubChapter {
        String title;
        int start;
        int end;
    }
}
