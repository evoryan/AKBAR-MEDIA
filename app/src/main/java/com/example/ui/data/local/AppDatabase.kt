package com.example.ui.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pelanggan")
data class PelangganEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val phone: String,
    val area: String,
    val address: String? = null,
    val username: String,
    val billingDate: String,
    val status: String,
    val price: String,
    val discount: String,
    val register_date: String? = null,
    val isolate_date: String? = null,
    val package_name: String? = null,
    val pppoe_secret: String? = null,
    val odp_id: Int? = null,
    val odp_port: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null
)

@Entity(tableName = "tagihan")
data class TagihanEntity(
    @PrimaryKey val id: Int,
    val customer_id: Int,
    val bulan: String,
    val tahun: Int,
    val amount: Double,
    val status: String,
    val admin_name: String? = null,
    val created_at: String? = null
)

@Entity(tableName = "status_router_terakhir")
data class StatusRouterTerakhirEntity(
    @PrimaryKey val area_id: Int,
    val area_name: String,
    val cpu_load: String,
    val uptime: String,
    val active_pppoe: String,
    val offline_pppoe: String,
    val status: String,
    val updated_at: String? = null
)

@Entity(tableName = "gangguan")
data class GangguanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String, // opsional
    val description: String,
    val status: String, // "OTW", "SELESAI"
    val date: String,
    val reporter: String,
    val teknisi: String = "",
    val biaya: Double = 0.0,
    val resolverAdmin: String? = null
)

@Dao
interface PelangganDao {
    @Query("SELECT * FROM pelanggan ORDER BY name ASC")
    fun getAllPelanggan(): Flow<List<PelangganEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pelanggan: List<PelangganEntity>)

    @Query("DELETE FROM pelanggan WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE pelanggan SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("DELETE FROM pelanggan")
    suspend fun deleteAll()
}

@Dao
interface TagihanDao {
    @Query("SELECT * FROM tagihan ORDER BY id DESC")
    fun getAllTagihan(): Flow<List<TagihanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tagihan: List<TagihanEntity>)

    @Query("DELETE FROM tagihan")
    suspend fun deleteAll()
}

@Dao
interface StatusRouterTerakhirDao {
    @Query("SELECT * FROM status_router_terakhir")
    fun getAllStatusRouter(): Flow<List<StatusRouterTerakhirEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statusRouter: List<StatusRouterTerakhirEntity>)

    @Query("DELETE FROM status_router_terakhir")
    suspend fun deleteAll()
}

@Dao
interface GangguanDao {
    @Query("SELECT * FROM gangguan ORDER BY id DESC")
    fun getAllGangguan(): Flow<List<GangguanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGangguan(gangguan: GangguanEntity)

    @Query("UPDATE gangguan SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE gangguan SET status = :status, resolverAdmin = :resolverAdmin WHERE id = :id")
    suspend fun updateStatusAndResolver(id: Int, status: String, resolverAdmin: String)

    @Query("DELETE FROM gangguan WHERE id = :id")
    suspend fun deleteGangguan(id: Int)
}

@Database(entities = [PelangganEntity::class, TagihanEntity::class, StatusRouterTerakhirEntity::class, GangguanEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pelangganDao(): PelangganDao
    abstract fun tagihanDao(): TagihanDao
    abstract fun statusRouterDao(): StatusRouterTerakhirDao
    abstract fun gangguanDao(): GangguanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akbar_media_room.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
