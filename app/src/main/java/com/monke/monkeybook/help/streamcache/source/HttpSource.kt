package com.monke.monkeybook.help.streamcache.source

import com.monke.monkeybook.help.streamcache.utils.HeaderInjector
import com.monke.monkeybook.help.streamcache.utils.HttpHeader
import com.monke.monkeybook.help.streamcache.utils.LoggerLevel
import com.monke.monkeybook.help.streamcache.utils.StreamCacheException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.*


/**
 * Created by summer on 18-2-23
 *
 * 远程服务端资源
 */
internal class HttpSource(
        private val url: String,
        httpHeaderInjector: HeaderInjector
) : Source() {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 30_000

        // from com.squareup.okhttp.internal.http
        private const val HTTP_TEMP_REDIRECT = 307

        private const val MAX_REDIRECTS = 10

        //最大重试次数
        private const val MAX_RETRY_COUNT = 3
    }

    private val header: HttpHeader = HashMap()

    init {
        httpHeaderInjector(url, header)
    }

    private val mAllowCrossDomainRedirect by lazy {
        val value = header["android-allow-cross-domain-redirect"] ?: "0"
        try {
            value.toLong() != 0L
        } catch (e: NumberFormatException) {
            "true".equals(value, true)
                    || "yes".equals(value, true)
        }
    }

    private val mAllowCrossProtocolRedirect by lazy {
        mAllowCrossDomainRedirect
    }

    private var mInputStream: InputStream? = null

    private var mConnection: HttpURLConnection? = null

    private var mUrl = URL(url)

    private var mTotalSize = -1L


    override fun open(offset: Long) {
        var repeat = 0
        while (repeat < MAX_RETRY_COUNT) {
            try {
                openInternal(offset)
                return
            } catch (e: IOException) {
                mTotalSize = -1
                close()
                com.monke.monkeybook.help.streamcache.utils.log(LoggerLevel.DEBUG) { e.printStackTrace();"retry : $repeat" }
            }
            repeat++
        }
    }

    //open url to get the http InputStream
    //copy from Android Open Source Project
    private fun openInternal(offset: Long) {
        close()
        var url = mUrl
        var redirectCount = 0
        while (true) {
            val noProxy = isLocalHost(url)
            val connection = if (noProxy) {
                url.openConnection(Proxy.NO_PROXY)
            } else {
                url.openConnection()
            } as HttpURLConnection
            mConnection = connection
            connection.connectTimeout = CONNECT_TIMEOUT_MS

            connection.instanceFollowRedirects = mAllowCrossDomainRedirect

            header.forEach {
                connection.setRequestProperty(it.key, it.value)
            }

            if (offset > 0) {
                connection.setRequestProperty("Range", "bytes=$offset-")
            }

            val response = connection.responseCode
            if (response != HttpURLConnection.HTTP_MULT_CHOICE &&
                    response != HttpURLConnection.HTTP_MOVED_PERM &&
                    response != HttpURLConnection.HTTP_MOVED_TEMP &&
                    response != HttpURLConnection.HTTP_SEE_OTHER &&
                    response != HTTP_TEMP_REDIRECT) {
                // not a redirect, or redirect handled by HttpURLConnection
                break
            }

            if (++redirectCount > MAX_REDIRECTS) {
                throw NoRouteToHostException("Too many redirects : $redirectCount")
            }

            val method = connection.requestMethod
            if (response == HTTP_TEMP_REDIRECT &&
                    method != "GET" && method != "HEAD") {
                // "If the 307 status code is received in response to a
                // request other than GET or HEAD, the user agent MUST NOT
                // automatically redirect the request"
                throw NoRouteToHostException("Invalid redirect")
            }

            val location = connection["Location"]
                    ?: throw NoRouteToHostException("Invalid redirect")

            url = URL(mUrl /* tricky: don't use url! */, location)
            if (url.protocol != "https" && url.protocol != "http") {
                throw NoRouteToHostException("Unsupported protocol redirect")
            }

            val sameProtocol = mUrl.protocol == url.protocol
            if (!mAllowCrossProtocolRedirect && !sameProtocol) {
                throw NoRouteToHostException("Cross-protocol redirects are disallowed")
            }
            val sameHost = mUrl.host == url.host
            if (!mAllowCrossDomainRedirect && !sameHost) {
                throw NoRouteToHostException("Cross-domain redirects are disallowed")
            }
        }

        val connection = this.mConnection!!
        if (mAllowCrossDomainRedirect) {
            mUrl = connection.url
        }

        val responseCode = connection.responseCode
        when {
            responseCode == HttpURLConnection.HTTP_PARTIAL -> {
                mTotalSize = -1
                connection["Content-Range"]?.let { range ->
                    // format is "bytes xxx-yyy/zzz
                    // where "zzz" is the total number of bytes of the
                    // content or '*' if unknown.
                    val lastIndexOf = range.lastIndexOf('/')
                    if (lastIndexOf >= 0) {
                        val total = range.substring(lastIndexOf + 1)
                        try {
                            mTotalSize = total.toLong()
                        } catch (e: NumberFormatException) {
                        }
                    }
                }
            }
            responseCode != HttpURLConnection.HTTP_OK -> throw IOException()
            else -> mTotalSize = connection.contentLength.toLong()
        }

        if (offset > 0 && responseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw ProtocolException("server not support 'Rang' request")
        }

        mInputStream = BufferedInputStream(connection.inputStream)

    }

    override val size: Long
        get() {
            if (mConnection == null) {
                open(0)
            }
            return mTotalSize
        }

    override fun read(byteArray: ByteArray, off: Int, len: Int): Int {
        return mInputStream?.read(byteArray, off, len)
                ?: throw StreamCacheException("Error reading data from $url , can not correct open connection")
    }

    override fun close() {
        com.monke.monkeybook.help.streamcache.utils.log(LoggerLevel.DEBUG) { "close source :$this" }
        try {
            mInputStream?.close()
        } catch (e: IOException) {
        } finally {
            mInputStream = null
        }
        try {
            mConnection?.disconnect()
        } catch (e: IOException) {
        } finally {
            mConnection = null
        }
    }

    override fun toString(): String {
        return "source : url = $url"
    }

    private fun isLocalHost(url: URL): Boolean {
        val host = url.host ?: return false
        if (host.equals("localhost", true)) {
            return true
        }
        return false
    }

    internal operator fun HttpURLConnection.get(fieldName: String) = getHeaderField(fieldName)

}