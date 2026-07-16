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
    private const val BASE_URL = "http://103.253.245.25:4500/"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private var database: OfflineDatabase? = null
    
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
            
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context))
                .build()
                
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                
            apiService = retrofit.create(ApiService::class.java)
            
        }
    }
    
    fun getDatabase(): OfflineDatabase? = database
}
