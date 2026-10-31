package com.example.ui.data.remote

import android.content.Context
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException

class HostFallbackInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        try {
            return chain.proceed(originalRequest)
        } catch (e: Exception) {
            val isHostError = e is UnknownHostException ||
                    (e is IOException && e.message?.contains("Unable to resolve host", ignoreCase = true) == true)
            
            val currentHost = originalRequest.url.host
            val fallbackIpUrl = "http://103.253.245.25:4100/"
            
            if (isHostError && !currentHost.contains("103.253.245.25")) {
                android.util.Log.w("HostFallbackInterceptor", "Gagal me-resolve host [$currentHost]. Mencoba fallback sementara ke VPS IP: $fallbackIpUrl")
                
                val originalPathAndQuery = originalRequest.url.encodedPath +
                        if (originalRequest.url.encodedQuery != null) "?${originalRequest.url.encodedQuery}" else ""
                val newUrlString = "http://103.253.245.25:4100${if (originalPathAndQuery.startsWith("/")) "" else "/"}$originalPathAndQuery"
                
                val newHttpUrl = newUrlString.toHttpUrlOrNull()
                if (newHttpUrl != null) {
                    val newRequest = originalRequest.newBuilder()
                        .url(newHttpUrl)
                        .build()
                    return chain.proceed(newRequest)
                }
            }
            throw e
        }
    }
}
