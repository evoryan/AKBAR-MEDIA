package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET

interface ApiService {
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummaryResponse
}

@JsonClass(generateAdapter = true)
data class DashboardSummaryResponse(
    val totalCustomers: Int,
    val monthlyRevenue: Double,
    val totalGlobalRevenue: Double = 0.0,
    val activePPPoE: Int,
    val activeHotspot: Int,
    val paidCustomers: Int,
    val unpaidCustomers: Int
)
