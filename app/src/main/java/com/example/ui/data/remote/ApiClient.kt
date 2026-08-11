package com.example.ui.data.remote

import android.content.Context
import androidx.room.Room
import com.example.ui.data.local.OfflineDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private var database: OfflineDatabase? = null
    private var client: OkHttpClient? = null
    
    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                OfflineDatabase::class.java,
                "offline_cache.db"
            )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries() // Allowed for generic interceptor ease
            .build()
            
            client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context))
                .build()
                
            updateBaseUrl(com.example.ui.data.SettingsManager.apiBaseUrl)
        }
    }
    
    fun updateBaseUrl(newUrl: String) {
        val safeUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        val currentClient = client ?: OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(currentClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            
        apiService = retrofit.create(ApiService::class.java)
    }
    
    fun getDatabase(): OfflineDatabase? = database
}
