package com.monke.monkeybook.help.streamcache

import android.os.Environment
import com.monke.monkeybook.help.streamcache.utils.log
import java.io.File
import java.io.IOException

/**
 * Created by summer on 18-3-1
 */
object CacheGlobalSetting {

    private const val CACHE_FOLDER_NAME = ".MediaCache"

    /**
     * the dir to cache files
     */
    var CACHE_PATH: String? = null

    /**
     * The maximum size that can be cached
     *
     * default is 800MB
     */
    var CACHE_SIZE: Long = 1024 * 1024 * 8

    init {
        CACHE_PATH = try {
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val cacheDir = File(musicDir, CACHE_FOLDER_NAME)
            checkDir(cacheDir)?.path
        } catch (e: SecurityException) {
            throw e
        } catch (e: IOException) {
            log { e.printStackTrace();"init cache folder failed" }
            null
        }
    }

    //get cache dir internal
    internal fun getCacheDir(): File? = CACHE_PATH?.let {
        val file = File(it)
        checkDir(file)
    }

    private fun checkDir(dir: File): File? {
        if (dir.isFile) {
            dir.delete()
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return if (dir.exists()) {
            dir
        } else {
            null
        }
    }
}