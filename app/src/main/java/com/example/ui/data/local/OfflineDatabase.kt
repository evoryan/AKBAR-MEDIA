package com.example.ui.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "http_cache")
data class CacheEntity(
    @PrimaryKey val url: String,
    val json: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String,
    val url: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM http_cache WHERE url = :url")
    fun get(url: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cacheEntity: CacheEntity)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue WHERE url = :url ORDER BY timestamp ASC")
    fun getPendingForUrl(url: String): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE url LIKE :urlPrefix || '%' ORDER BY timestamp ASC")
    fun getPendingByPrefix(urlPrefix: String): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getAllPending(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(syncQueueEntity: SyncQueueEntity)
    
    @Query("DELETE FROM sync_queue WHERE id = :id")
    fun delete(id: Int)
}

@Database(entities = [CacheEntity::class, SyncQueueEntity::class], version = 1, exportSchema = false)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    abstract fun syncDao(): SyncDao
}
