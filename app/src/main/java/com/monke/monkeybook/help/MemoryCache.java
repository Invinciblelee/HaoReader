package com.monke.monkeybook.help;

/**
 * LruCache 内存缓存
 */
public class MemoryCache extends androidx.collection.LruCache<String, Object> {

    private static MemoryCache mInstance;

    private static final int MAX_SIZE;

    static {
        int maxMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
        MAX_SIZE = maxMemory / 8;
    }

    private MemoryCache() {
        super(MAX_SIZE);
    }

    public static MemoryCache getInstance() {
        if (mInstance == null) {
            synchronized (MemoryCache.class) {
                if (mInstance == null) {
                    mInstance = new MemoryCache();
                }
            }
        }
        return mInstance;
    }

    @SuppressWarnings("unchecked")
    public final <T> T getCache(String key) {
        return (T) super.get(key);
    }

    public final void putCache(String key, Object value) {
        put(key, value);
    }
}
