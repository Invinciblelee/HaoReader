//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.help;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class BitIntentDataManager {

    private Map<String, Object> mBigData;

    private volatile static BitIntentDataManager mInstance = null;

    public static BitIntentDataManager getInstance() {
        if (mInstance == null) {
            synchronized (BitIntentDataManager.class) {
                if (mInstance == null) {
                    mInstance = new BitIntentDataManager();
                }
            }
        }
        return mInstance;
    }

    private BitIntentDataManager() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T def) {
        if (mBigData == null || TextUtils.isEmpty(key)) return null;
        try {
            return (T) mBigData.get(key);
        }catch (Exception e){
            return def;
        }
    }

    public void putData(String key, Object data) {
        if (mBigData == null) {
            mBigData = new HashMap<>();
        }

        if (!TextUtils.isEmpty(key)) {
            mBigData.put(key, data);
        }
    }

    public void cleanData(String key) {
        if (mBigData != null && !TextUtils.isEmpty(key)) {
            mBigData.remove(key);
        }
    }

    public void cleanAllData() {
        if (mBigData != null) {
            mBigData.clear();
        }
    }
}
