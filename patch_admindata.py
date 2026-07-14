import re

with open("app/src/main/java/com/example/ui/data/AdminData.kt", "r") as f:
    content = f.read()

# patch saveSession
target_save = """        user.db_name?.let { dbName ->
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
    }"""

rep_save = """        user.db_name?.let { dbName ->
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
    }"""
content = content.replace(target_save, rep_save)

# patch loadSession
target_load = """            dbName?.let {
                val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
                val topicName = "tenant_$safeTopic"
                FirebaseMessaging.getInstance().subscribeToTopic(topicName)
            }
            return true
        }"""

rep_load = """            dbName?.let {
                val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
                val topicName = "tenant_$safeTopic"
                FirebaseMessaging.getInstance().subscribeToTopic(topicName)
            }
            if (role == UserRole.SUPER_ADMIN) {
                FirebaseMessaging.getInstance().subscribeToTopic("tenant_superadmin")
            }
            return true
        }"""
content = content.replace(target_load, rep_load)

# patch clearSession
target_clear = """    fun clearSession(context: Context) {
        val dbName = currentUser.value?.db_name"""

rep_clear = """    fun clearSession(context: Context) {
        val dbName = currentUser.value?.db_name
        val role = currentUser.value?.role"""
content = content.replace(target_clear, rep_clear)

target_clear2 = """            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }
        sharedPrefs.edit().clear().apply()
    }"""

rep_clear2 = """            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }
        if (role == UserRole.SUPER_ADMIN) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("tenant_superadmin")
        }
        sharedPrefs.edit().clear().apply()
    }"""
content = content.replace(target_clear2, rep_clear2)


with open("app/src/main/java/com/example/ui/data/AdminData.kt", "w") as f:
    f.write(content)

