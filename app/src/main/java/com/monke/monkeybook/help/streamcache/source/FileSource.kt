package com.monke.monkeybook.help.streamcache.source

import com.monke.monkeybook.help.streamcache.utils.LoggerLevel
import com.monke.monkeybook.help.streamcache.utils.log
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Created by summer on 18-2-23
 *
 * 本地文件资源
 *
 */
internal class FileSource(path: String) : Source() {

    val file = java.io.File(path)

    private var data: RandomAccessFile? = null

    override val size: Long
        get() {
            if (data == null) {
                open(0)
            }
            return data?.length() ?: 0
        }

    override fun open(offset: Long) {
        if (file.exists()) {
            log(LoggerLevel.ERROR) { "file do not exists : ${file.path}" }
            return
        }
        if (data == null) {
            try {
                data = RandomAccessFile(file, "r")
            } catch (e: IOException) {
                //ignore
            }
        }
        if (offset >= 0) {
            data?.seek(offset)
        }
    }

    override fun read(byteArray: ByteArray, off: Int, len: Int): Int {
        return data?.read(byteArray, off, len) ?: -1
    }

    override fun close() {
        try {
            data?.close()
        } catch (e: IOException) {

        } finally {
            data = null
        }
    }


    override fun toString(): String {
        return "source :path=${file.path}"
    }
}