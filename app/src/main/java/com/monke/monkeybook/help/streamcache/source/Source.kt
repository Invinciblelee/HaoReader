package com.monke.monkeybook.help.streamcache.source

import java.io.Closeable

/**
 * Created by summer on 18-2-23
 *
 * 资源
 *
 */
abstract class Source : Closeable {

    /**
     * total available bytes count
     */
    abstract val size: Long

    // the bytes count has been read
    protected var offset: Long = -1
        private set(value) {
            field = value
        }

    fun read(byteArray: ByteArray, position: Long, off: Int = 0, len: Int = byteArray.size): Int {
        if (position < 0) {
            return -1
        }
        if (offset != position) {
            open(position)
            offset = position
        }
        val read = read(byteArray, off, len)
        if (read >= 0) {
            offset += read
        }
        return read
    }

    protected abstract fun read(byteArray: ByteArray, off: Int, len: Int): Int

    /**
     * open this source
     *
     * @param offset 距离资源起点的偏移量
     */
    protected abstract fun open(offset: Long = 0)


}