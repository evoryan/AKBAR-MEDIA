package com.example.ui.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

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
                    wasOffline = false
                }
            }

            override fun onLost(network: Network) {
                if (!wasOffline) {
                    wasOffline = true
                }
            }
        })
        
        // Initial check
        val nw = cm.activeNetwork
        val actNw = cm.getNetworkCapabilities(nw)
        if (actNw == null || !actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            wasOffline = true
        }
    }
}
