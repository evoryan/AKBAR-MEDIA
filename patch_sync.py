content = """package com.example.ui.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.ui.data.local.OfflineDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object SyncManager {
    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isSyncing = false

    fun triggerSync(context: Context) {
        if (isSyncing) return
        
        syncJob = scope.launch {
            if (!isNetworkAvailable(context)) return@launch
            isSyncing = true
            
            try {
                val db = ApiClient.getDatabase() ?: return@launch
                val syncDao = db.syncDao()
                val pending = syncDao.getAllPending()
                
                if (pending.isEmpty()) {
                    isSyncing = false
                    return@launch
                }

                // Simple OkHttpClient for raw requests
                val client = OkHttpClient()

                for (item in pending) {
                    // Try to send to API
                    val requestBuilder = Request.Builder()
                        .url("http://103.253.245.25:4500" + item.url)

                    when (item.method) {
                        "POST" -> requestBuilder.post(item.body.toRequestBody("application/json".toMediaTypeOrNull()))
                        "PUT" -> requestBuilder.put(item.body.toRequestBody("application/json".toMediaTypeOrNull()))
                        "DELETE" -> requestBuilder.delete()
                    }

                    try {
                        val response = client.newCall(requestBuilder.build()).execute()
                        if (response.isSuccessful) {
                            syncDao.delete(item.id)
                        } else {
                            // If it fails with 400 or 500, we might want to delete it eventually to avoid loop,
                            // but for simple offline-first, if it's 404 or 400 we just delete. 
                            if (response.code in 400..599) {
                                syncDao.delete(item.id)
                            }
                        }
                    } catch (e: Exception) {
                        // Network error during sync, stop syncing and wait for next trigger
                        break
                    }
                }
            } finally {
                isSyncing = false
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
"""
with open('app/src/main/java/com/example/ui/data/remote/SyncManager.kt', 'w') as f:
    f.write(content)
