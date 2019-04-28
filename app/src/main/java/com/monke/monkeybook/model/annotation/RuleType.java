package com.monke.monkeybook.model.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        RuleType.DEFAULT,
        RuleType.XPATH,
        RuleType.JSON,
        RuleType.HYBRID
})
@Retention(RetentionPolicy.SOURCE)
public @interface RuleType {
    String DEFAULT = "DEFAULT";
    String XPATH = "XPATH";
    String JSON = "JSON";
    String HYBRID = "HYBRID";
}