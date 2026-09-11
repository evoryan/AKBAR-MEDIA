package com.example.ui.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import android.util.Log

enum class UserRole {
    SUPER_ADMIN, ADMIN, TEKNISI, COLLECTOR
}

data class AdminUser(val id: String, var name: String, var username: String, var role: UserRole, val token: String? = null, val db_name: String? = null, var area_id: String? = null)

object UserSession {
    val currentUser = MutableStateFlow<AdminUser?>(null)
    var hasCheckedForUpdate = false
    
    private val subscribedTopics = mutableSetOf<String>()

    fun getTenantTopic(dbName: String?): String? {
        if (dbName.isNullOrBlank()) return null
        val safeTopic = dbName.replace(Regex("[^a-zA-Z0-9-_~]"), "")
        return if (safeTopic.isNotBlank()) "tenant_$safeTopic" else null
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return try {
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Throwable) {
            false
        }
    }

    fun safeSubscribeToTopic(context: Context, topicName: String) {
        if (subscribedTopics.contains(topicName)) {
            return
        }
        if (!isGooglePlayServicesAvailable(context)) {
            Log.d("FCM", "Google Play Services tidak tersedia, melewati subscribe topik $topicName")
            return
        }
        try {
            val fcm = FirebaseMessaging.getInstance()
            fcm.token.addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful && !tokenTask.result.isNullOrBlank()) {
                    fcm.subscribeToTopic(topicName)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                subscribedTopics.add(topicName)
                                Log.d("FCM", "Berhasil subscribe terisolasi ke topik $topicName")
                            } else {
                                Log.w("FCM", "Gagal subscribe ke topik $topicName: ${task.exception?.message}")
                            }
                        }
                } else {
                    Log.d("FCM", "FCM Token belum tersedia atau registrasi tidak aktif di lingkungan ini: ${tokenTask.exception?.message}")
                }
            }
        } catch (e: Throwable) {
            Log.w("FCM", "Gagal menginisialisasi subscribe ke topik $topicName: ${e.message}")
        }
    }

    fun safeUnsubscribeFromTopic(context: Context, topicName: String) {
        subscribedTopics.remove(topicName)
        if (!isGooglePlayServicesAvailable(context)) {
            return
        }
        try {
            val fcm = FirebaseMessaging.getInstance()
            fcm.token.addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful && !tokenTask.result.isNullOrBlank()) {
                    fcm.unsubscribeFromTopic(topicName)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("FCM", "Berhasil unsubscribe dari topik $topicName")
                            } else {
                                Log.w("FCM", "Gagal unsubscribe dari topik $topicName: ${task.exception?.message}")
                            }
                        }
                }
            }
        } catch (e: Throwable) {
            Log.w("FCM", "Gagal menginisialisasi unsubscribe dari topik $topicName: ${e.message}")
        }
    }

    private fun updateTenantNotificationSubscription(context: Context, newDbName: String?) {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val lastTopic = sharedPrefs.getString("last_subscribed_tenant_topic", null)
        val newTopic = getTenantTopic(newDbName)

        // Hapus sisa subscribe topik global lama agar tidak pernah bocor antar tenant
        safeUnsubscribeFromTopic(context, "tenant_superadmin")

        // Jika ada database sebelumnya yang berbeda, segera unsubscribe
        if (!lastTopic.isNullOrBlank() && lastTopic != newTopic) {
            safeUnsubscribeFromTopic(context, lastTopic)
            sharedPrefs.edit().remove("last_subscribed_tenant_topic").apply()
        }

        // Daftarkan ke topik database tenant yang baru secara terisolasi
        if (!newTopic.isNullOrBlank()) {
            safeSubscribeToTopic(context, newTopic)
            sharedPrefs.edit().putString("last_subscribed_tenant_topic", newTopic).apply()
            Log.d("FCM", "Isolasi Notifikasi: Terdaftar eksklusif untuk database tenant [topik: $newTopic]")
        }
    }

    fun hasDeletePrivilege(): Boolean {
        return currentUser.value?.role == UserRole.SUPER_ADMIN
    }
    
    fun saveSession(context: Context, user: AdminUser) {
        currentUser.value = user
        cachedAreas = emptyList() // clear cache on session switch
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.name)
            putString("user_username", user.username)
            putString("user_role", user.role.name)
            putString("user_token", user.token)
            putString("user_db_name", user.db_name)
            putString("user_area_id", user.area_id)
            apply()
        }
        
        // Isolasi notifikasi pembayaran secara ketat per database tenant
        updateTenantNotificationSubscription(context, user.db_name)
    }
    
    fun loadSession(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = sharedPrefs.getString("user_id", null)
        val name = sharedPrefs.getString("user_name", null)
        val username = sharedPrefs.getString("user_username", null)
        val roleString = sharedPrefs.getString("user_role", null)
        val token = sharedPrefs.getString("user_token", null)
        val dbName = sharedPrefs.getString("user_db_name", null)
        val areaId = sharedPrefs.getString("user_area_id", "semua")
        
        if (id != null && name != null && username != null && roleString != null) {
            val role = try {
                UserRole.valueOf(roleString)
            } catch (e: Exception) {
                UserRole.ADMIN
            }
            currentUser.value = AdminUser(id, name, username, role, token, dbName, areaId)
            
            // Sinkronkan kembali isolasi notifikasi per database tenant saat session aktif dimuat
            updateTenantNotificationSubscription(context, dbName)
            return true
        }
        return false
    }

    var cachedAreas: List<com.example.ui.screens.Area> = emptyList()

    suspend fun getOrFetchAreas(): List<com.example.ui.screens.Area> {
        if (cachedAreas.isEmpty()) {
            try {
                cachedAreas = com.example.ui.data.remote.ApiClient.apiService.getAreas()
            } catch (e: Exception) {
                Log.e("UserSession", "Error fetching areas", e)
            }
        }
        return cachedAreas
    }

    fun isAreaIdAllowed(areaId: String): Boolean {
        val user = currentUser.value ?: return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        val allowedAreaIds = user.area_id?.split(",")?.filter { it.isNotBlank() } ?: return true
        if (allowedAreaIds.contains("semua")) return true
        return allowedAreaIds.contains(areaId)
    }

    fun isAreaNameAllowed(areaName: String): Boolean {
        val user = currentUser.value ?: return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        val allowedAreaIds = user.area_id?.split(",")?.filter { it.isNotBlank() } ?: return true
        if (allowedAreaIds.contains("semua")) return true
        // Map ID to Name
        val allowedNames = allowedAreaIds.mapNotNull { id ->
            cachedAreas.find { it.id == id }?.name
        }
        if (allowedNames.isEmpty() && cachedAreas.isNotEmpty()) {
            // fallback: check if we should fetch/cache. If cachedAreas is empty, we might not have it yet.
            return true
        }
        return allowedNames.any { it.equals(areaName, ignoreCase = true) }
    }

    fun canManageAdmin(otherAdmin: AdminUser): Boolean {
        val user = currentUser.value ?: return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        if (user.role != UserRole.ADMIN) return false // teknisi/collector can't manage anyone
        // Super admins (id="1" or SUPER_ADMIN role) cannot be managed by other admins
        if (otherAdmin.role == UserRole.SUPER_ADMIN || otherAdmin.id == "1") return false
        
        // Check if their areas overlap
        val myAllowed = user.area_id?.split(",")?.filter { it.isNotBlank() } ?: return true
        if (myAllowed.contains("semua")) return true
        
        val otherAllowed = otherAdmin.area_id?.split(",")?.filter { it.isNotBlank() } ?: return false
        if (otherAllowed.contains("semua")) return false // standard admin cannot manage an admin who has access to all areas
        
        return otherAllowed.any { myAllowed.contains(it) }
    }
    
    fun clearSession(context: Context) {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val lastTopic = sharedPrefs.getString("last_subscribed_tenant_topic", null)
        val dbName = currentUser.value?.db_name ?: sharedPrefs.getString("user_db_name", null)
        
        currentUser.value = null
        cachedAreas = emptyList() // clear cache on logout

        // Unsubscribe dari topik tenant database aktif
        val currentTopic = getTenantTopic(dbName) ?: lastTopic
        if (!currentTopic.isNullOrBlank()) {
            safeUnsubscribeFromTopic(context, currentTopic)
        }
        if (!lastTopic.isNullOrBlank() && lastTopic != currentTopic) {
            safeUnsubscribeFromTopic(context, lastTopic)
        }
        safeUnsubscribeFromTopic(context, "tenant_superadmin")
        sharedPrefs.edit().clear().apply()
    }
}
