package com.monke.monkeybook.model.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        BookType.TEXT,
        BookType.AUDIO
})
@Retention(RetentionPolicy.SOURCE)
public @interface BookType {
    String TEXT = "TEXT";
    String AUDIO = "AUDIO";
}