package com.monke.monkeybook.help.streamcache.utils

import java.io.IOException

open class StreamCacheException(msg: String) : IOException(msg)

class StreamOpenException(msg: String) : StreamCacheException(msg)