package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class GithubAsset(
    val name: String? = null,
    val browser_download_url: String,
    val size: Long? = null,
    val updated_at: String? = null,
    val content_type: String? = null
)

@JsonClass(generateAdapter = true)
data class GithubRelease(
    val tag_name: String,
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean? = false,
    val draft: Boolean? = false,
    val published_at: String? = null,
    val assets: List<GithubAsset> = emptyList()
)

interface GithubApiService {
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getAllReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GithubRelease>

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubRelease

    companion object {
        fun create(): GithubApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "AkbarMedia-Android-Client/1.0")
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("Cache-Control", "no-cache, no-store")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(GithubApiService::class.java)
        }
    }
}
