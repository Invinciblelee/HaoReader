package com.monke.monkeybook.help.streamcache

import android.annotation.TargetApi
import android.media.MediaDataSource
import android.net.Uri
import android.os.Build

@TargetApi(Build.VERSION_CODES.M)
class MediaDataSourceDelegate(uri: Uri)
    : MediaDataSource() {

    private val dataSource: CachedDataSource = CachedDataSource(uri)

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return dataSource.readAt(position, buffer, offset, size)
    }

    override fun getSize(): Long {
        return dataSource.getSize()
    }

    override fun close() {
        dataSource.close()
    }
}