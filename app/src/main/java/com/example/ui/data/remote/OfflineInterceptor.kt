package com.example.ui.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.ui.data.local.CacheEntity
import com.example.ui.data.local.OfflineDatabase
import com.example.ui.data.local.SyncDao
import com.example.ui.data.local.SyncQueueEntity
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.launch

class OfflineInterceptor(
    private val db: OfflineDatabase,
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isOnline = isNetworkAvailable(context)
        val urlPath = request.url.encodedPath

        if (request.method == "GET") {
            if (urlPath.contains("/api/login")) return chain.proceed(request)
            if (isOnline) {
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful) {
                        val bodyString = response.peekBody(Long.MAX_VALUE).string()
                        db.cacheDao().insert(CacheEntity(urlPath, bodyString))
                    }
                    return response
                } catch (e: Exception) {
                    // fallback to offline on exception
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        android.widget.Toast.makeText(context, "Server error, beralih ke data offline.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Offline GET
            val cached = db.cacheDao().get(urlPath)
            if (cached != null) {
                var json = cached.json
                json = mergeSyncQueue(json, urlPath, db.syncDao())

                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            } else {
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200) // Return 200 with empty array to prevent crashes
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
        } else {
            // POST, PUT, DELETE
            if (urlPath.contains("/api/login")) return chain.proceed(request)
            if (isOnline) {
                try {
                    return chain.proceed(request)
                } catch (e: Exception) {
                    // fallback to offline queue
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        android.widget.Toast.makeText(context, "Server error, aksi disimpan secara offline.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Queue for offline sync
            val bodyString = request.body?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                buffer.readUtf8()
            } ?: ""

            db.syncDao().insert(SyncQueueEntity(method = request.method, url = urlPath, body = bodyString))

            // Trigger sync (if we have connection later, SyncManager will handle it)
            SyncManager.triggerSync(context)

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200) // fake success
                .message("OK")
                .body("{\"message\": \"Tersimpan offline\", \"id\": \"offline_dummy\"}".toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }
    }

    private fun mergeSyncQueue(json: String, url: String, syncDao: SyncDao): String {
        try {
            if (!json.trim().startsWith("[")) return json // Only merge arrays

            val array = JSONArray(json)
            
            // 1. Process Pending POSTs (Additions)
            val pendingPosts = syncDao.getPendingForUrl(url).filter { it.method == "POST" }
            for (item in pendingPosts) {
                try {
                    val newItem = JSONObject(item.body)
                    // Ensure it has an id so it renders properly in Compose UI
                    if (!newItem.has("id") || newItem.getString("id").isEmpty()) {
                        newItem.put("id", "offline_" + item.id)
                    }
                    array.put(newItem)
                } catch(e: Exception) {}
            }

            // 2. Process Pending DELETEs
            // Deletes usually hit /api/customers/{id}
            val pendingDeletes = syncDao.getPendingByPrefix(url + "/")
                .filter { it.method == "DELETE" }
            for (del in pendingDeletes) {
                val idToDelete = del.url.substringAfterLast("/")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.has("id") && obj.getString("id") == idToDelete) {
                        array.remove(i)
                        break
                    }
                }
            }
            
            // 2.5 Process Pending Billing Pay/Delete
            val pendingPays = syncDao.getPendingForUrl("/api/billing/pay").filter { it.method == "POST" }
            for (pay in pendingPays) {
                try {
                    val payObj = JSONObject(pay.body)
                    val customerId = payObj.getString("customerId")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.has("id") && obj.getString("id") == customerId) {
                            obj.put("status", "LUNAS CASH")
                            array.put(i, obj)
                            break
                        }
                    }
                } catch(e: Exception) {}
            }
            val pendingBillingDeletes = syncDao.getPendingForUrl("/api/billing/delete").filter { it.method == "POST" }
            for (del in pendingBillingDeletes) {
                try {
                    val delObj = JSONObject(del.body)
                    val customerId = delObj.getString("customerId")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.has("id") && obj.getString("id") == customerId) {
                            obj.put("status", "BELUM BAYAR")
                            array.put(i, obj)
                            break
                        }
                    }
                } catch(e: Exception) {}
            }

            // 3. Process Pending PUTs (Updates)
            val pendingPuts = syncDao.getPendingByPrefix(url + "/")
                .filter { it.method == "PUT" }
            for (put in pendingPuts) {
                val idToUpdate = put.url.substringAfterLast("/")
                try {
                    val updatedObj = JSONObject(put.body)
                    if (!updatedObj.has("id")) updatedObj.put("id", idToUpdate)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.has("id") && obj.getString("id") == idToUpdate) {
                            array.put(i, updatedObj)
                            break
                        }
                    }
                } catch(e: Exception) {}
            }

            return array.toString()
        } catch (e: Exception) {
            return json 
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
