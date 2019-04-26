package com.monke.monkeybook.help.streamcache.utils

import com.monke.monkeybook.help.streamcache.CacheGlobalSetting
import java.io.File


fun checkIsCached(fileName: String): Boolean {
    return getCachedFile(fileName) != null
}


fun getCachedFile(fileName: String): File? {
    val cacheDir = CacheGlobalSetting.getCacheDir() ?: return null
    val file = File(cacheDir, fileName)
    return if (file.exists()) {
        file
    } else {
        null
    }
}