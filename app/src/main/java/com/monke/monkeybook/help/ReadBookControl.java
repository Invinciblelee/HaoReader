//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.help;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.monke.monkeybook.utils.BitmapUtil;
import com.monke.monkeybook.widget.page.PageMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadBookControl {
    private static final int DEFAULT_BG = 1;

    private final List<Map<String, Integer>> textDrawables;
    private final List<Map<String, Integer>> pageSpaces;
    private int animSpeed;
    private int speechRate;
    private boolean speechRateFollowSys;
    private int textSize;
    private int textColor;
    private boolean bgIsColor;
    private int bgColor;
    private int lineSpacing;
    private int paragraphSpacing;
    private int pageMode;
    private String bgPath;
    private Bitmap bgBitmap;

    private int textDrawableIndex = DEFAULT_BG;

    private Boolean hideStatusBar;
    private String fontPath;
    private int textConvert;
    private Boolean textBold;
    private Boolean canClickTurn;
    private Boolean canKeyTurn;
    private Boolean readAloudCanKeyTurn;
    private int clickSensitivity;
    private Boolean showTitle;
    private Boolean clickAllNext;
    private Boolean showTimeBattery;
    private String lastNoteUrl;
    private Boolean darkStatusIcon;
    private Boolean showBatteryNumber;
    private Boolean showBottomLine;
    private int spaceModeIndex;
    private int screenTimeOut;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;

    {
        textDrawables = new ArrayList<>();
        Map<String, Integer> temp1 = new HashMap<>();
        temp1.put("textColor", Color.parseColor("#3E3D3B"));
        temp1.put("bgIsColor", 1);
        temp1.put("textBackground", Color.parseColor("#F3F3F3"));
        temp1.put("darkStatusIcon", 1);
        textDrawables.add(temp1);

        Map<String, Integer> temp2 = new HashMap<>();
        temp2.put("textColor", Color.parseColor("#5E432E"));
        temp2.put("bgIsColor", 1);
        temp2.put("textBackground", Color.parseColor("#C6BAA1"));
        temp2.put("darkStatusIcon", 1);
        textDrawables.add(temp2);

        Map<String, Integer> temp3 = new HashMap<>();
        temp3.put("textColor", Color.parseColor("#22482C"));
        temp3.put("bgIsColor", 1);
        temp3.put("textBackground", Color.parseColor("#E1F1DA"));
        temp3.put("darkStatusIcon", 1);
        textDrawables.add(temp3);

        Map<String, Integer> temp4 = new HashMap<>();
        temp4.put("textColor", Color.parseColor("#FFFFFF"));
        temp4.put("bgIsColor", 1);
        temp4.put("textBackground", Color.parseColor("#015A86"));
        temp4.put("darkStatusIcon", 0);
        textDrawables.add(temp4);

        Map<String, Integer> temp5 = new HashMap<>();
        temp5.put("textColor", Color.parseColor("#a3a3a3"));
        temp5.put("bgIsColor", 1);
        temp5.put("textBackground", Color.parseColor("#212121"));
        temp5.put("darkStatusIcon", 0);
        textDrawables.add(temp5);

        pageSpaces = new ArrayList<>();
        Map<String, Integer> temp6 = new HashMap<>();
        temp6.put("lineSpacing", 4);
        temp6.put("paragraphSpacing", 8);
        temp6.put("paddingLeft", 24);
        temp6.put("paddingTop", 0);
        temp6.put("paddingRight", 24);
        temp6.put("paddingBottom", 0);
        pageSpaces.add(temp6);

        Map<String, Integer> temp7 = new HashMap<>();
        temp7.put("lineSpacing", 8);
        temp7.put("paragraphSpacing", 12);
        temp7.put("paddingLeft", 24);
        temp7.put("paddingTop", 0);
        temp7.put("paddingRight", 24);
        temp7.put("paddingBottom", 0);
        pageSpaces.add(temp7);

        Map<String, Integer> temp8 = new HashMap<>();
        temp8.put("lineSpacing", 12);
        temp8.put("paragraphSpacing", 24);
        temp8.put("paddingLeft", 24);
        temp8.put("paddingTop", 0);
        temp8.put("paddingRight", 24);
        temp8.put("paddingBottom", 0);
        pageSpaces.add(temp8);

        Map<String, Integer> temp9 = new HashMap<>();
        temp9.put("lineSpacing", 6);
        temp9.put("paragraphSpacing", 16);
        temp9.put("paddingLeft", 24);
        temp9.put("paddingTop", 0);
        temp9.put("paddingRight", 24);
        temp9.put("paddingBottom", 0);
        pageSpaces.add(temp9);
    }

    private SharedPreferences readPreference;

    private volatile static ReadBookControl readBookControl;

    public static ReadBookControl getInstance() {
        if (readBookControl == null) {
            synchronized (ReadBookControl.class) {
                if (readBookControl == null) {
                    readBookControl = new ReadBookControl();
                }
            }
        }
        return readBookControl;
    }

    private ReadBookControl() {
        readPreference = AppConfigHelper.get().getPreferences();
        this.hideStatusBar = readPreference.getBoolean("hide_status_bar", false);
        this.textSize = readPreference.getInt("textSize", 18);
        this.canClickTurn = readPreference.getBoolean("canClickTurn", true);
        this.canKeyTurn = readPreference.getBoolean("canKeyTurn", true);
        this.readAloudCanKeyTurn = readPreference.getBoolean("readAloudCanKeyTurn", true);
        this.lineSpacing = readPreference.getInt("lineSpacing", 6);
        this.paragraphSpacing = readPreference.getInt("paragraphSpacing", 16);
        this.animSpeed = readPreference.getInt("animSpeed", 300);
        this.clickSensitivity = readPreference.getInt("clickSensitivity", 50);
        if (this.clickSensitivity < 5) {
            this.clickSensitivity = 5;
        }
        this.clickAllNext = readPreference.getBoolean("clickAllNext", false);
        this.fontPath = readPreference.getString("fontPath", null);
        this.textConvert = readPreference.getInt("textConvertInt", 0);
        this.textBold = readPreference.getBoolean("textBold", false);
        this.speechRate = readPreference.getInt("speechRate", 10);
        this.speechRateFollowSys = readPreference.getBoolean("speechRateFollowSys", true);
        this.showTimeBattery = readPreference.getBoolean("showTimeBattery", true);
        this.showBatteryNumber = readPreference.getBoolean("showBatteryNumber", false);
        this.showBottomLine = readPreference.getBoolean("showBottomLine", false);
        this.lastNoteUrl = readPreference.getString("lastNoteUrl", "");
        this.screenTimeOut = readPreference.getInt("screenTimeOut", 0);
        this.paddingLeft = readPreference.getInt("paddingLeft", 24);
        this.paddingTop = readPreference.getInt("paddingTop", 0);
        this.paddingRight = readPreference.getInt("paddingRight", 24);
        this.paddingBottom = readPreference.getInt("paddingBottom", 0);
        this.pageMode = readPreference.getInt("pageMode", 0);
        this.spaceModeIndex = readPreference.getInt("spaceModeIndex", 3);
        this.showTitle = readPreference.getBoolean("showTitle", true);

        initPageConfiguration();
    }

    public void initPageConfiguration() {
        if (getIsNightTheme()) {
            textDrawableIndex = readPreference.getInt("textDrawableIndexNight", 4);
        } else {
            textDrawableIndex = readPreference.getInt("textDrawableIndex", DEFAULT_BG);
        }
        if (textDrawableIndex < 0) {
            textDrawableIndex = DEFAULT_BG;
        }
        setPageStyle();

        setTextDrawable();
    }

    private void setPageStyle() {
        try {
            bgColor = getConfigValue(textDrawableIndex, "textBackground");
            if (getBgCustom(textDrawableIndex) == 2 && getBgPath(textDrawableIndex) != null) {
                bgIsColor = false;
                bgPath = getBgPath(textDrawableIndex);
                bgBitmap = resetBgBitmap();
            } else if (getBgCustom(textDrawableIndex) == 1) {
                bgIsColor = true;
                bgColor = getBgColor(textDrawableIndex);
            } else {
                bgIsColor = true;
                bgColor = getConfigValue(textDrawableIndex, "textBackground");
            }
        } catch (Exception e) {
            bgIsColor = true;
            bgColor = getConfigValue(textDrawableIndex, "textBackground");
        }
    }

    private void setTextDrawable() {
        darkStatusIcon = getDarkStatusIcon(textDrawableIndex);
        textColor = getTextColor(textDrawableIndex);
    }

    public int getTextColor(int textDrawableIndex) {
        return readPreference.getInt("textColor" + textDrawableIndex, getDefaultTextColor(textDrawableIndex));
    }

    public void setTextColor(int textDrawableIndex, int textColor) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textColor" + textDrawableIndex, textColor);
        editor.apply();
    }

    public Drawable getBgDrawable(int textDrawableIndex, Context context) {
        try {
            switch (getBgCustom(textDrawableIndex)) {
                case 2:
                    Bitmap bitmap = BitmapFactory.decodeFile(getBgPath(textDrawableIndex));
                    if (bitmap != null) {
                        return new BitmapDrawable(context.getResources(), bitmap);
                    }
                    break;
                case 1:
                    return new ColorDrawable(getBgColor(textDrawableIndex));
            }
            if (getConfigValue(textDrawableIndex, "bgIsColor") != 0) {
                return new ColorDrawable(getConfigValue(textDrawableIndex, "textBackground"));
            } else {
                return getDefaultBgDrawable(textDrawableIndex, context);
            }
        } catch (Exception e) {
            if (getConfigValue(textDrawableIndex, "bgIsColor") != 0) {
                return new ColorDrawable(getConfigValue(textDrawableIndex, "textBackground"));
            } else {
                return getDefaultBgDrawable(textDrawableIndex, context);
            }
        }
    }

    public Drawable getDefaultBgDrawable(int textDrawableIndex, Context context) {
        if (getConfigValue(textDrawableIndex, "bgIsColor") != 0) {
            return new ColorDrawable(getConfigValue(textDrawableIndex, "textBackground"));
        } else {
            return context.getResources().getDrawable(getDefaultBgColor(textDrawableIndex));
        }
    }

    public Drawable getBgDrawable(Context context) {
        return getBgDrawable(textDrawableIndex, context);
    }

    public int getBgCustom(int textDrawableIndex) {
        return readPreference.getInt("bgCustom" + textDrawableIndex, 0);
    }

    public void setBgCustom(int textDrawableIndex, int bgCustom) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("bgCustom" + textDrawableIndex, bgCustom);
        editor.apply();
    }

    public String getBgPath(int textDrawableIndex) {
        return readPreference.getString("bgPath" + textDrawableIndex, null);
    }

    public void setBgPath(int textDrawableIndex, String bgUri) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putString("bgPath" + textDrawableIndex, bgUri);
        editor.apply();
    }

    public int getDefaultTextColor(int textDrawableIndex) {
        return getConfigValue(textDrawableIndex, "textColor");
    }

    public int getDefaultBgColor(int textDrawableIndex) {
        return getConfigValue(textDrawableIndex, "textBackground");
    }

    public int getBgColor(int index) {
        return readPreference.getInt("bgColor" + index, getDefaultBgColor(index));
    }

    public void setBgColor(int index, int bgColor) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("bgColor" + index, bgColor);
        editor.apply();
    }

    public void setPageSpaceMode(int index) {
        if (index >= 0 && index < pageSpaces.size()) {
            Map<String, Integer> temp = pageSpaces.get(index);
            Integer value = temp.get("lineSpacing");
            lineSpacing = value == null ? 6 : value;
            value = temp.get("paragraphSpacing");
            paragraphSpacing = value == null ? 16 : value;
            value = temp.get("paddingTop");
            paddingTop = value == null ? 0 : value;
            value = temp.get("paddingLeft");
            paddingLeft = value == null ? 24 : value;
            value = temp.get("paddingRight");
            paddingRight = value == null ? 24 : value;
            value = temp.get("paddingBottom");
            paddingBottom = value == null ? 0 : value;
            readPreference.edit().putInt("lineSpacing", lineSpacing)
                    .putInt("paragraphSpacing", paragraphSpacing)
                    .putInt("paddingTop", paddingTop)
                    .putInt("paddingLeft", paddingLeft)
                    .putInt("paddingRight", paddingRight)
                    .putInt("paddingBottom", paddingBottom)
                    .putInt("spaceModeIndex", index)
                    .apply();
        }
    }

    public int getPageSpaceMode() {
        return spaceModeIndex;
    }

    public int getSpacingByKey(String key) {
        return readPreference.getInt(key, 0);
    }

    public boolean getIsNightTheme() {
        return readPreference.getBoolean("nightTheme", false);
    }

    public boolean getImmersionStatusBar() {
        return readPreference.getBoolean("immersionStatusBar", false);
    }

    public void setImmersionStatusBar(boolean immersionStatusBar) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("immersionStatusBar", immersionStatusBar);
        editor.apply();
    }

    public int getAnimSpeed() {
        return animSpeed;
    }

    public void setAnimSpeed(int animSpeed) {
        this.animSpeed = animSpeed;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("animSpeed", animSpeed);
        editor.apply();
    }

    public String getLastNoteUrl() {
        return lastNoteUrl;
    }

    public void setLastNoteUrl(String lastNoteUrl) {
        this.lastNoteUrl = lastNoteUrl;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putString("lastNoteUrl", lastNoteUrl);
        editor.apply();
    }



    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textSize", textSize);
        editor.apply();
    }

    public int smallerTextSize() {
        if (this.textSize <= 10) {
            this.textSize = 10;
        } else {
            this.textSize -= 1;
        }
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textSize", textSize);
        editor.apply();
        return this.textSize;
    }

    public int largerTextSize() {
        if (this.textSize >= 40) {
            this.textSize = 40;
        } else {
            this.textSize += 1;
        }
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textSize", textSize);
        editor.apply();
        return this.textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean bgIsColor() {
        return bgIsColor;
    }

    public int getDefaultBgColor() {
        return getDefaultBgColor(textDrawableIndex);
    }

    public int getBgColor() {
        return bgColor;
    }

    public String getBgPath() {
        return bgPath;
    }

    public Bitmap getBgBitmap() {
        if (bgBitmap == null || bgBitmap.isRecycled()) {
            bgBitmap = resetBgBitmap();
        }
        return bgBitmap;
    }

    private Bitmap resetBgBitmap() {
        if (!TextUtils.isEmpty(bgPath)) {
            return BitmapUtil.getBitmap(bgPath, 1080, 1920);
        }
        return null;
    }

    public int getTextDrawableIndex() {
        return textDrawableIndex;
    }

    public void setTextDrawableIndex(int textDrawableIndex) {
        this.textDrawableIndex = textDrawableIndex;
        SharedPreferences.Editor editor = readPreference.edit();
        if (getIsNightTheme()) {
            editor.putInt("textDrawableIndexNight", textDrawableIndex);
        } else {
            editor.putInt("textDrawableIndex", textDrawableIndex);
        }
        editor.apply();
        setTextDrawable();
    }

    public void setTextConvert(int textConvert) {
        this.textConvert = textConvert;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("textConvertInt", textConvert);
        editor.apply();
    }

    public void setTextBold(boolean textBold) {
        this.textBold = textBold;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("textBold", textBold);
        editor.apply();
    }

    public void setReadBookFont(String fontPath) {
        this.fontPath = fontPath;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putString("fontPath", fontPath);
        editor.apply();
    }

    public String getFontPath() {
        return fontPath;
    }

    public int getTextConvert() {
        return textConvert == -1 ? 2 : textConvert;
    }

    public Boolean getTextBold() {
        return textBold;
    }

    public Boolean getCanKeyTurn(Boolean isPlay) {
        if (!canKeyTurn) {
            return false;
        } else if (readAloudCanKeyTurn) {
            return true;
        } else {
            return !isPlay;
        }
    }

    public Boolean getCanKeyTurn() {
        return canKeyTurn;
    }

    public void setCanKeyTurn(Boolean canKeyTurn) {
        this.canKeyTurn = canKeyTurn;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("canKeyTurn", canKeyTurn);
        editor.apply();
    }

    public Boolean getAloudCanKeyTurn() {
        return readAloudCanKeyTurn;
    }

    public void setAloudCanKeyTurn(Boolean canAloudKeyTurn) {
        this.readAloudCanKeyTurn = canAloudKeyTurn;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("readAloudCanKeyTurn", canAloudKeyTurn);
        editor.apply();
    }

    public Boolean getCanClickTurn() {
        return canClickTurn;
    }

    public void setCanClickTurn(Boolean canClickTurn) {
        this.canClickTurn = canClickTurn;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("canClickTurn", canClickTurn);
        editor.apply();
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("lineSpacing", lineSpacing).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public float getParagraphSpacing() {
        return paragraphSpacing;
    }

    public void setParagraphSpacing(int paragraphSpacing) {
        this.paragraphSpacing = paragraphSpacing;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paragraphSpacing", paragraphSpacing).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public int getClickSensitivity() {
        return clickSensitivity;
    }

    public void setClickSensitivity(int clickSensitivity) {
        this.clickSensitivity = clickSensitivity;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("clickSensitivity", clickSensitivity);
        editor.apply();
    }

    public Boolean getClickAllNext() {
        return clickAllNext;
    }

    public void setClickAllNext(Boolean clickAllNext) {
        this.clickAllNext = clickAllNext;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("clickAllNext", clickAllNext);
        editor.apply();
    }

    public int getSpeechRate() {
        return speechRate;
    }

    public void setSpeechRate(int speechRate) {
        this.speechRate = speechRate;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("speechRate", speechRate);
        editor.apply();
    }

    public boolean isSpeechRateFollowSys() {
        return speechRateFollowSys;
    }

    public void setSpeechRateFollowSys(boolean speechRateFollowSys) {
        this.speechRateFollowSys = speechRateFollowSys;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("speechRateFollowSys", speechRateFollowSys);
        editor.apply();
    }

    public Boolean getShowTimeBattery() {
        return showTimeBattery;
    }

    public void setShowTimeBattery(Boolean showTimeBattery) {
        this.showTimeBattery = showTimeBattery;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showTimeBattery", showTimeBattery);
        editor.apply();
    }

    public Boolean getShowBatteryNumber() {
        return showBatteryNumber;
    }

    public void setShowBatteryNumber(Boolean showBatteryNumber) {
        this.showBatteryNumber = showBatteryNumber;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showBatteryNumber", showBatteryNumber);
        editor.apply();
    }

    public Boolean getShowBottomLine() {
        return showBottomLine;
    }

    public void setShowBottomLine(Boolean showBottomLine) {
        this.showBottomLine = showBottomLine;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showBottomLine", showBottomLine);
        editor.apply();
    }

    public Boolean getHideStatusBar() {
        return hideStatusBar;
    }

    public void setHideStatusBar(Boolean hideStatusBar) {
        this.hideStatusBar = hideStatusBar;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("hide_status_bar", hideStatusBar);
        editor.apply();
    }

    public boolean getDarkStatusIcon() {
        return darkStatusIcon;
    }

    public boolean getDarkStatusIcon(int textDrawableIndex) {
        return readPreference.getBoolean("darkStatusIcon" + textDrawableIndex, getConfigValue(textDrawableIndex, "darkStatusIcon") != 0);
    }

    public void setDarkStatusIcon(int textDrawableIndex, Boolean darkStatusIcon) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("darkStatusIcon" + textDrawableIndex, darkStatusIcon);
        editor.apply();
    }

    public int getScreenTimeOut() {
        return screenTimeOut;
    }

    public void setScreenTimeOut(int screenTimeOut) {
        this.screenTimeOut = screenTimeOut;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("screenTimeOut", screenTimeOut);
        editor.apply();
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingLeft", paddingLeft).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingTop", paddingTop).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingRight", paddingRight).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("paddingBottom", paddingBottom).putInt("spaceModeIndex", spaceModeIndex = 4);
        editor.apply();
    }

    public int getPageMode() {
        return pageMode;
    }

    public PageMode getPageMode(int pageMode) {
        switch (pageMode) {
            case 0:
                return PageMode.COVER;
            case 1:
                return PageMode.SIMULATION;
            case 2:
                return PageMode.SLIDE;
            case 3:
                return PageMode.NONE;
            default:
                return PageMode.COVER;
        }
    }

    public void setPageMode(int pageMode) {
        this.pageMode = pageMode;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("pageMode", pageMode);
        editor.apply();
    }


    public void saveLight(int light, boolean isFollowSys) {
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putInt("light", light);
        editor.putBoolean("isfollowsys", isFollowSys);
        editor.apply();
    }

    public int getScreenLight(int defaultVal) {
        return readPreference.getInt("light", defaultVal);
    }

    public boolean getLightIsFollowSys() {
        return readPreference.getBoolean("isfollowsys", true);
    }

    public void setShowTitle(boolean showTitle){
        this.showTitle = showTitle;
        SharedPreferences.Editor editor = readPreference.edit();
        editor.putBoolean("showTitle", showTitle);
        editor.apply();
    }

    public boolean getShowTitle(){
        return showTitle;
    }

    @NonNull
    private Integer getConfigValue(int index, String key) {
        Map<String, Integer> configMap = textDrawables.get(index);
        if (key != null) {
            Integer value = configMap.get(key);
            return value == null ? 0 : value;
        }
        return 0;
    }
}
