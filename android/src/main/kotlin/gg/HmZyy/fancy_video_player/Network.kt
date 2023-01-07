package gg.HmZyy.fancy_video_player

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
)
lateinit var cache: Cache

lateinit var okHttpClient: OkHttpClient

fun initializeNetwork(context: Context) {
    val dns = 1
    cache = Cache(
        File(context.cacheDir, "http_cache"),
        50L * 1024L * 1024L // 50 MiB
    )
    okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cache(cache)
        .apply {
            when (dns) {
//                1 -> addGoogleDns()
//                2 -> addCloudFlareDns()
//                3 -> addAdGuardDns()
            }
        }
        .build()
}

fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("SSL").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}

//fun OkHttpClient.Builder.addGoogleDns() = (
//        addGenericDns(
//            "https://dns.google/dns-query",
//            listOf(
//                "8.8.4.4",
//                "8.8.8.8"
//            )
//        ))
//
//fun OkHttpClient.Builder.addCloudFlareDns() = (
//        addGenericDns(
//            "https://cloudflare-dns.com/dns-query",
//            listOf(
//                "1.1.1.1",
//                "1.0.0.1",
//                "2606:4700:4700::1111",
//                "2606:4700:4700::1001"
//            )
//        ))
//
//fun OkHttpClient.Builder.addAdGuardDns() = (
//        addGenericDns(
//            "https://dns.adguard.com/dns-query",
//            listOf(
//                // "Non-filtering"
//                "94.140.14.140",
//                "94.140.14.141",
//            )
//        ))
