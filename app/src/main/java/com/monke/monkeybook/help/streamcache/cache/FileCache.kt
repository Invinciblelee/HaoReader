package com.monke.monkeybook.help.streamcache.cache

import com.monke.monkeybook.help.streamcache.CacheGlobalSetting
import com.monke.monkeybook.help.streamcache.strategy.CacheStrategy
import com.monke.monkeybook.help.streamcache.utils.LoggerLevel
import com.monke.monkeybook.help.streamcache.utils.log
import java.io.File
import java.io.RandomAccessFile

/**
 * author : YangBin
 *
 * provide method to write and read file bytes
 *
 * @param filename the name of cache file
 * @param cacheStrategy the strategy when file is cached
 */
internal class FileCache(
        filename: String,
        private val cacheStrategy: CacheStrategy
) : Cache() {

    companion object {
        const val SUFFIX = ".download"
    }

    private var cachedFile: File

    private val data: RandomAccessFile

    init {
        val file = File(CacheGlobalSetting.getCacheDir(), filename)
        val mode: String
        if (file.exists()) {
            cachedFile = file
            log { "file has been cached : ${cachedFile.path}" }
            mode = "r"
        } else {
            cachedFile = File(CacheGlobalSetting.getCacheDir(), filename + SUFFIX)
            if (!cachedFile.exists()) {
                cachedFile.createNewFile()
            }
            log { "create new file : ${cachedFile.path}" }
            mode = "rw"
        }
        log { "cached filed ${cachedFile.name} size = ${cachedFile.length() / 1024}" }
        data = RandomAccessFile(cachedFile, mode)
    }

    override val available: Long
        get() = data.length()

    override val isComplete: Boolean
        get() = (cachedFile.exists() && !cachedFile.path.endsWith(SUFFIX))

    @Synchronized
    override fun read(position: Long, byteArray: ByteArray, off: Int, len: Int): Int {
        data.seek(position)
        return data.read(byteArray, off, len)
    }

    @Synchronized
    override fun write(byteArray: ByteArray, off: Int, len: Int) {
        if (isComplete) {
            return
        }
        data.seek(available)
        data.write(byteArray, off, len)
    }

    @Synchronized
    override fun close() {
        data.close()
    }

    /**
     * let cache makes it complete
     */
    override fun complete() {
        val file = File(cachedFile.path.removeSuffix(SUFFIX))
        if (cachedFile.renameTo(file)) {
            cacheStrategy.onFileCached(file)
            if (file.exists()) {
                cachedFile = file
            }
        } else {
            log(LoggerLevel.ERROR) { "close file ${cachedFile.path} failed" }
        }
    }
}