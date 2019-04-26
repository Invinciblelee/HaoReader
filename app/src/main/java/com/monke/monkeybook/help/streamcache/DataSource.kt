package com.monke.monkeybook.help.streamcache

import java.io.Closeable
import java.io.IOException

/**
 * @see android.media.MediaDataSource
 */
interface DataSource : Closeable {

    @Throws(IOException::class)
    fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int

    @Throws(IOException::class)
    fun getSize(): Long

}