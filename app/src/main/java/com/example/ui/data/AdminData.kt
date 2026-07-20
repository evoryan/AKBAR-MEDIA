package com.example.ui.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

enum class UserRole {
    SUPER_ADMIN, ADMIN, TEKNISI, COLLECTOR
}

data class AdminUser(val id: String, var name: String, var username: String, var role: UserRole, val token: String? = null, val db_name: String? = null, var area_id: String? = null)

object UserSession {
    val currentUser = MutableStateFlow<AdminUser?>(null)
    var hasCheckedForUpdate = false
    
    fun hasDeletePrivilege(): Boolean {
        return currentUser.value?.role == UserRole.SUPER_ADMIN
    }
    
    fun saveSession(context: Context, user: AdminUser) {
        currentUser.value = user
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
        
        user.db_name?.let { dbName ->
            val safeTopic = dbName.replace(Regex("[^a-zA-Z0-9-_~]"), "")
            val topicName = "tenant_$safeTopic"
            FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "Berhasil subscribe ke topik $topicName")
                    } else {
                        Log.e("FCM", "Gagal subscribe ke topik $topicName")
                    }
                }
        }
        if (user.role == UserRole.SUPER_ADMIN) {
            FirebaseMessaging.getInstance().subscribeToTopic("tenant_superadmin")
        }
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
            dbName?.let {
                val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
                val topicName = "tenant_$safeTopic"
                FirebaseMessaging.getInstance().subscribeToTopic(topicName)
            }
            if (role == UserRole.SUPER_ADMIN) {
                FirebaseMessaging.getInstance().subscribeToTopic("tenant_superadmin")
            }
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
        val dbName = currentUser.value?.db_name
        val role = currentUser.value?.role
        currentUser.value = null
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        dbName?.let {
            val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
            val topicName = "tenant_$safeTopic"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }
        if (role == UserRole.SUPER_ADMIN) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("tenant_superadmin")
        }
        sharedPrefs.edit().clear().apply()
    }
}
