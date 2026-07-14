package com.example.ui.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NetworkMonitor {
    private var isRegistered = false
    private var wasOffline = false

    fun init(context: Context) {
        if (isRegistered) return
        isRegistered = true

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (wasOffline) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context.applicationContext, "Jaringan kembali normal. Mode Online aktif.", Toast.LENGTH_LONG).show()
                    }
                    wasOffline = false
                    SyncManager.triggerSync(context)
                }
            }

            override fun onLost(network: Network) {
                if (!wasOffline) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context.applicationContext, "Koneksi terputus. Beralih ke Mode Offline.", Toast.LENGTH_LONG).show()
                    }
                    wasOffline = true
                }
            }
        })
        
        // Initial check
        val nw = cm.activeNetwork
        val actNw = cm.getNetworkCapabilities(nw)
        if (actNw == null || !actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            wasOffline = true
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context.applicationContext, "Tidak ada jaringan. Beralih ke Mode Offline.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
