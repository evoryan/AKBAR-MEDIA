package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Manual initialization of Firebase as a robust guarantee
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:959734436856:android:a0a29f38a62d13b90dfc0d")
                    .setApiKey("AIzaSyBUNCM9Esm-dCFMw7HNq9EUkif9cEf6p-o")
                    .setProjectId("akbar-media-cae03")
                    .setStorageBucket("akbar-media-cae03.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                android.util.Log.d("MyApplication", "Firebase manually initialized successfully as a fallback.")
            } else {
                android.util.Log.d("MyApplication", "Firebase already initialized automatically.")
            }
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Failed to initialize Firebase", e)
        }
    }
}
