package com.monke.monkeybook.help.streamcache.strategy

import com.monke.monkeybook.help.streamcache.CacheGlobalSetting
import com.monke.monkeybook.help.streamcache.utils.LoggerLevel
import com.monke.monkeybook.help.streamcache.utils.log
import java.io.File

/**
 * author : YangBin
 */
object LruCacheStrategy : CacheStrategy {

    override fun onFileCached(file: File) {
        if (!file.exists()) {
            log(LoggerLevel.ERROR) { "file has been cached , but still not exists" }
            return
        }
        file.setLastModified(System.currentTimeMillis())
        val files = CacheGlobalSetting.getCacheDir()?.listFiles() ?: return
        clearUp(files)
    }

    //to clear up cache
    private fun clearUp(files: Array<File>) {
        files.sortBy { it.lastModified() }
        var totalSize = files.calculateTotalSize()
        files.forEach { file ->
            if (totalSize > CacheGlobalSetting.CACHE_SIZE) {
                val size = file.length()
                if (file.delete()) {
                    totalSize -= size
                } else {
                    log(LoggerLevel.ERROR) { "error to delete cache" }
                }
            } else {
                return@forEach
            }
        }
    }

    /**
     * calculate a list of file's size
     */
    private fun Array<File>.calculateTotalSize() = fold(0L) { acc, file ->
        if (file.isDirectory) {
            //delete ?
            acc
        } else {
            acc + file.length()
        }
    }

}