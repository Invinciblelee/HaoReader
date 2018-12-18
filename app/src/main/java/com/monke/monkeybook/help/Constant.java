package com.monke.monkeybook.help;

import android.support.annotation.StringDef;

import com.monke.monkeybook.utils.FileUtil;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.monke.monkeybook.help.Constant.RuleType.DEFAULT;

/**
 * Created by newbiechen on 17-4-16.
 */

public class Constant {

    //Book Date Convert Format
    public static final String FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";

    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileUtil.getSdCardPath() + File.separator
            + "YueDu" + File.separator + "chapters" + File.separator;
    public static String APP_CRASH_PATH = FileUtil.getSdCardPath() + File.separator
            + "YueDu" + File.separator + "crashes" + File.separator;

    //BookType
    @StringDef({
            BookType.TEXT,
            BookType.AUDIO,
            BookType.DOWNLOAD,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BookType {
        String TEXT = "TEXT";
        String AUDIO = "AUDIO";
        String DOWNLOAD = "DOWNLOAD";
    }

    public static final String[] BOOK_TYPES = {
            BookType.TEXT, BookType.AUDIO, BookType.DOWNLOAD
    };


    //BookType
    @StringDef({
            RuleType.DEFAULT,
            RuleType.XPATH,
            RuleType.JSON
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RuleType {
        String DEFAULT = "DEFAULT";
        String XPATH = "XPATH";
        String JSON = "JSON";
    }

    public static final String[] RULE_TYPES = {
            RuleType.DEFAULT, RuleType.XPATH, RuleType.JSON
    };
}
