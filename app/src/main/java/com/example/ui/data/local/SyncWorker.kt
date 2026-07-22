package com.example.ui.data.local

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ui.data.remote.ApiClient

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting background data sync...")
        return try {
            ApiClient.init(applicationContext)

            val syncResponse = ApiClient.apiService.syncData()
            val db = AppDatabase.getDatabase(applicationContext)

            val pelangganList = syncResponse.customers.map { item ->
                PelangganEntity(
                    id = item.id.toIntOrNull() ?: 0,
                    name = item.name ?: "",
                    phone = item.phone ?: "",
                    area = item.area ?: "",
                    address = item.address,
                    username = item.username ?: "",
                    billingDate = item.billingDate ?: "",
                    status = item.status ?: "",
                    price = item.price ?: "",
                    discount = item.discount ?: "",
                    register_date = item.register_date,
                    isolate_date = item.isolate_date,
                    package_name = item.package_name,
                    pppoe_secret = item.pppoe_secret,
                    odp_id = item.odp_id?.toIntOrNull(),
                    odp_port = item.odp_port,
                    additionalCost1 = item.additionalCost1,
                    additionalCost2 = item.additionalCost2
                )
            }

            val tagihanList = syncResponse.tagihan.map { item ->
                TagihanEntity(
                    id = item.id.toIntOrNull() ?: 0,
                    customer_id = item.customer_id?.toIntOrNull() ?: 0,
                    bulan = item.bulan ?: "",
                    tahun = item.tahun ?: 0,
                    amount = item.amount?.toDoubleOrNull() ?: 0.0,
                    status = item.status ?: "BELUM BAYAR",
                    admin_name = item.admin_name,
                    created_at = item.created_at
                )
            }

            val routerStatusList = syncResponse.routerStatus.map { item ->
                StatusRouterTerakhirEntity(
                    area_id = item.area_id?.toIntOrNull() ?: 0,
                    area_name = item.area_name ?: "",
                    cpu_load = item.cpu_load ?: "-",
                    uptime = item.uptime ?: "-",
                    active_pppoe = item.active_pppoe ?: "-",
                    offline_pppoe = item.offline_pppoe ?: "-",
                    status = item.status ?: "Offline",
                    updated_at = item.updated_at
                )
            }

            // Empty old table rows and perform atomic update via insertAll
            db.pelangganDao().deleteAll()
            db.pelangganDao().insertAll(pelangganList)

            db.tagihanDao().deleteAll()
            db.tagihanDao().insertAll(tagihanList)

            db.statusRouterDao().deleteAll()
            db.statusRouterDao().insertAll(routerStatusList)

            Log.d("SyncWorker", "Data sync successful: " +
                    "${pelangganList.size} pelanggan, " +
                    "${tagihanList.size} tagihan, " +
                    "${routerStatusList.size} router statuses updated.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Data sync failed", e)
            Result.retry()
        }
    }
}
