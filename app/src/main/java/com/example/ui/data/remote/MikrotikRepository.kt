package com.example.ui.data.remote

import com.example.ui.screens.Area
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

object MikrotikRepository {
    /**
     * Fetches statuses for a list of Mikrotik routers/areas in parallel.
     * Uses Kotlin's 'async/awaitAll' pattern so that requests to different routers execute in parallel,
     * preventing one offline router from delaying the entire request batch.
     */
    suspend fun getMikrotikStatusesInParallel(areas: List<Area>): Map<String, Result<MikrotikStatus>> = withContext(Dispatchers.IO) {
        areas.map { area ->
            async {
                try {
                    val status = ApiClient.apiService.getMikrotikStatus(area.id)
                    area.id to Result.success(status)
                } catch (e: Exception) {
                    area.id to Result.failure(e)
                }
            }
        }.awaitAll().toMap()
    }
}
