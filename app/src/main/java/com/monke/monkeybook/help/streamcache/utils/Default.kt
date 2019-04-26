package com.monke.monkeybook.help.streamcache.utils

import java.security.MessageDigest


internal fun String.md5(): String {

    val md = MessageDigest.getInstance("MD5")
    val array = md.digest(toByteArray())
    val sb = StringBuffer()
    for (i in array.indices) {
        sb.append(Integer.toHexString(array[i].toInt() and 0xFF or 0x100).substring(1, 3))
    }
    return sb.toString()

}

//缓存文件命名生成器
internal typealias CacheNameGenerator = (url: String) -> String

internal typealias HttpHeader = MutableMap<String, String>

/**
 * default name generator, use the url md5 to generator cache name
 */
internal val md5NameGenerator: CacheNameGenerator = { url -> url.md5() }

//to injector http request header for url
internal typealias HeaderInjector = (url: String, header: MutableMap<String, String>) -> MutableMap<String, String>

//空的http头注入器,不做任何操作.
internal val emptyHeaderInjector: HeaderInjector = { _, header -> header }
