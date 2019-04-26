package com.monke.monkeybook.help.streamcache.strategy

import java.io.File

/**
 * author : YangBin
 */
interface CacheStrategy {

    /**
     * invoked when cache complete
     * @param file new cached file
     */
    fun onFileCached(file: File)

}