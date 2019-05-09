package com.monke.monkeybook.model.analyzeRule.pattern;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

public class Patterns {


    public static final Pattern PATTERN_HEADER = Pattern.compile("@header:\\{.+?\\}", Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_PAGE = Pattern.compile("\\{.*?\\}");

    public static final Pattern PATTERN_GET = Pattern.compile("@get:\\{.+?\\}", Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_JS = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);

    public static final Pattern PATTERN_JSON = Pattern.compile("(?<=\\{)\\$\\..+?(?=\\})");
    public static final Pattern PATTERN_EXP = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}");

    public static final String REGEX_OPERATOR = "(?i)@operate:";
    public static final String REGEX_REDIRECT = "(?i)@redirect:";

    public static final String RULE_AJAX = "$";
    public static final String RULE_SNIFF = "$$";
    public static final String RULE_KEEP = "^";
    public static final String RULE_REVERSE = "-";
    public static final String RULE_REGEX = "#";
    public static final String RULE_REGEX_TRAIT = "##";
    public static final String RULE_BODY = "outerBody";

    public static final String RULE_WHOLE = "@Whole:";

    public static final String RULE_JSON = "@JSon:";
    public static final String RULE_JSON_TRAIT = "$.";
    public static final String RULE_XPATH = "@XPath:";
    public static final String RULE_XPATH_TRAIT = "//";
    public static final String RULE_CSS = "@CSS:";

    public static final Type STRING_MAP = new TypeToken<Map<String, String>>() {
    }.getType();
}
