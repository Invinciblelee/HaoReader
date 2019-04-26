package com.monke.monkeybook.help.streamcache

import android.net.Uri
import com.monke.monkeybook.help.streamcache.source.FileSource
import com.monke.monkeybook.help.streamcache.source.HttpSource
import com.monke.monkeybook.help.streamcache.source.Source
import com.monke.monkeybook.help.streamcache.utils.emptyHeaderInjector

/**
 * Created by summer on 18-2-23
 *
 * 直接从Source获取数据,不提供缓存支持.
 */
class DirectDataSource(private val source: Source) : DataSource {

    companion object {

        operator fun invoke(uri: Uri): DirectDataSource {
            val source = if (uri.scheme == "file") {
                FileSource(uri.toString())
            } else {
                HttpSource(uri.toString(), emptyHeaderInjector)
            }
            return DirectDataSource(source)
        }

    }


    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return source.read(buffer, position, offset, size)
    }

    override fun getSize(): Long {
        return source.size
    }

    override fun close() {
        source.close()
    }
}