package com.monke.monkeybook.help;

import android.util.LruCache;

import com.monke.monkeybook.utils.StringUtils;

/**
 * LruCache 内存缓存
 */
public enum MemoryCache {

    INSTANCE;

    private static final int MAX_SIZE;

    static {
        int maxMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
        MAX_SIZE = maxMemory / 8;
    }

    private LruCache<String, Object> mCache;

    @SuppressWarnings("unchecked")
    public final <T> T getCache(String key) {
        if (mCache == null) {
            return null;
        }
        return (T) mCache.get(key);
    }

    public final void putCache(String key, Object value) {
        if (mCache == null) {
            mCache = new LruCache<>(MAX_SIZE);
        }
        if (StringUtils.isNotBlank(key)) {
            mCache.put(key, value);
        }
    }
}
