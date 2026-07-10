package com.example.ui.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.example.ui.data.UserSession

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        
        var token = UserSession.currentUser.value?.token
        if (token.isNullOrEmpty()) {
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            token = sharedPrefs.getString("user_token", null)
        }
        
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        
        val request = builder.build()
        return chain.proceed(request)
    }
}
