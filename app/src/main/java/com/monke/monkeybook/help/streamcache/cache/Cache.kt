package com.monke.monkeybook.help.streamcache.cache

import java.io.Closeable

/**
 * author : YangBin
 *
 * 缓存
 */
abstract class Cache : Closeable {

    abstract val isComplete: Boolean


    abstract fun read(position: Long, byteArray: ByteArray, off: Int = 0, len: Int = byteArray.size): Int

    abstract fun write(byteArray: ByteArray, off: Int = 0, len: Int = byteArray.size)

    /**
     * return the size content length of cache
     */
    open val available: Long = 0

    abstract fun complete()

}