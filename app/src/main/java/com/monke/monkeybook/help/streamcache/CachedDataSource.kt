package com.monke.monkeybook.help.streamcache

import android.net.Uri
import com.monke.monkeybook.help.streamcache.cache.Cache
import com.monke.monkeybook.help.streamcache.cache.FileCache
import com.monke.monkeybook.help.streamcache.source.HttpSource
import com.monke.monkeybook.help.streamcache.source.Source
import com.monke.monkeybook.help.streamcache.strategy.CacheStrategy
import com.monke.monkeybook.help.streamcache.strategy.LruCacheStrategy
import com.monke.monkeybook.help.streamcache.utils.CacheNameGenerator
import com.monke.monkeybook.help.streamcache.utils.HeaderInjector
import com.monke.monkeybook.help.streamcache.utils.emptyHeaderInjector
import com.monke.monkeybook.help.streamcache.utils.md5NameGenerator
import com.monke.monkeybook.utils.ObjectsCompat
import java.util.*

/**
 * author : YangBin
 *
 */
open class CachedDataSource internal constructor(
        private val cache: Cache,
        private val source: Source
) : DataSource {


    companion object {

        /**
         * 使用此方法来构造 CachedDataSource
         *
         * @param uri 文件地址
         * @param cacheNameGenerator 缓存文件命名生成器,默认使用MD5值命名.
         * @param httpHeaderInjector http请求头参数,默认不做任何处理
         * @param cacheStrategy 缓存策略,默认使用[LruCacheStrategy]
         */
        operator fun invoke(uri: Uri,
                            cacheNameGenerator: CacheNameGenerator = md5NameGenerator,
                            httpHeaderInjector: HeaderInjector = emptyHeaderInjector,
                            cacheStrategy: CacheStrategy = LruCacheStrategy): CachedDataSource {
            val cacheName = cacheNameGenerator(uri.toString())
            val fileCache = FileCache(cacheName, cacheStrategy)
            val source = HttpSource(uri.toString(), httpHeaderInjector)
            return CachedDataSource(fileCache, source)
        }
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (size == 0) {
            return 0
        }
        return if (isUseCache(position)) {
            readByCache(position, buffer, offset, size)
        } else {
            readDirectly(position, buffer, offset, size)
        }
    }

    private fun readByCache(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        while (!cache.isComplete && cache.available < position + size) {
            //todo 是一次性读取所有source到cache中，还是一次只读取一点点呢？？
            //read data from origin source
            val bytes = source.read(buffer, cache.available)
            if (bytes == -1) {
                cache.complete()
                break
            }
            cache.write(buffer, 0, bytes)
        }
        return cache.read(position, buffer, offset, size)
    }

    //直接从source读取资源
    private fun readDirectly(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return source.read(buffer, position, offset, size)
    }

    /**
     *
     * 判断读取此position的资源时,是否可以使用缓存或者等待缓存
     *
     * @param position 距离资源起点的偏移
     */
    private fun isUseCache(position: Long): Boolean {
        val isSourceLengthKnown = source.size > 0
        return !isSourceLengthKnown || position < cache.available + source.size * .2f
    }

    override fun getSize(): Long {
        return source.size
    }

    override fun close() {
        source.close()
        cache.close()
    }

//    private fun readSource() = launch {
//        source.open(cache.available)
//        val buffer = ByteArray(SIZE_BUFFER)
//        var bytes = source.read(buffer)
//        while (bytes > 0) {
//            cache.write(buffer, 0, bytes)
//            notifyNewDataAvailable()
//            bytes = source.read(buffer)
//        }
//        cache.complete()
//    }
//
//    private fun notifyNewDataAvailable() = synchronized(lock) {
//        lock.notifyAll()
//    }
//
//
//    private fun waitForSource() = synchronized(lock) {
//        lock.wait(1000)
//    }
//
//    private val lock = java.lang.Object()

}