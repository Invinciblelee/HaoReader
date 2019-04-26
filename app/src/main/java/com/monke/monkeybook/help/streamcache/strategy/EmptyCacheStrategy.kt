package com.monke.monkeybook.help.streamcache.strategy

import java.io.File

/**
 * author : YangBin
 */
object EmptyCacheStrategy : CacheStrategy {

    override fun onFileCached(file: File) {
        //do nothing
    }

}