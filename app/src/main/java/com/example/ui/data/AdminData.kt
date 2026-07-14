package com.example.ui.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

enum class UserRole {
    SUPER_ADMIN, ADMIN, TEKNISI, COLLECTOR
}

data class AdminUser(val id: String, var name: String, var username: String, var role: UserRole, val token: String? = null, val db_name: String? = null)

object UserSession {
    val currentUser = MutableStateFlow<AdminUser?>(null)
    
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
        
        if (id != null && name != null && username != null && roleString != null) {
            val role = try {
                UserRole.valueOf(roleString)
            } catch (e: Exception) {
                UserRole.ADMIN
            }
            currentUser.value = AdminUser(id, name, username, role, token, dbName)
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
