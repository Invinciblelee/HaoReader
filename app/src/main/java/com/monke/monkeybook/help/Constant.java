package com.monke.monkeybook.help;

import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.model.annotation.RuleType;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;

/**
 * Created by newbiechen on 17-4-16.
 */

public class Constant {

    //Book Date Convert Format
    public static final String FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    public static final String BUGLY_APP_ID = "d4ef8e5fa4";

    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CHAPTER_PATH = FileUtil.getSdCardPath() + File.separator
            + "HaoYue" + File.separator + "chapters" + File.separator;
    public static String AUDIO_CACHE_PATH = FileUtil.getSdCardPath() + File.separator
            + "HaoYue" + File.separator + "audios" + File.separator;
    public static String AUDIO_BOOK_PATH = FileUtil.getSdCardPath() + File.separator
            + "HaoYue" + File.separator + "books" + File.separator;
    public static String READ_FONT_PATH = FileUtil.getSdCardPath() + File.separator
            + "HaoYue" + File.separator + "fonts" + File.separator;


    public static final String[] BOOK_TYPES = {
            BookType.TEXT, BookType.AUDIO
    };


    public static final String[] RULE_TYPES = {
            RuleType.DEFAULT, RuleType.XPATH, RuleType.JSON, RuleType.CSS, RuleType.HYBRID
    };

    public static final int GROUP_ZHUIGENG = 0;
    public static final int GROUP_YANGFEI = 1;
    public static final int GROUP_WANJIE = 2;
    public static final int GROUP_BENDI = 3;
    public static final int GROUP_AUDIO = 4;
}
