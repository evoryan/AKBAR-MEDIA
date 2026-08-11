package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    val instance: ApiService by lazy {
        val baseUrl = com.example.ui.data.SettingsManager.apiBaseUrl
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        Retrofit.Builder()
            .baseUrl(safeUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
