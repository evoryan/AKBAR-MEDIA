import re

with open("app/src/main/java/com/example/ui/data/AdminData.kt", "r") as f:
    content = f.read()

import_statement = "import kotlinx.coroutines.flow.MutableStateFlow\nimport com.google.firebase.messaging.FirebaseMessaging\nimport android.util.Log"
content = content.replace("import kotlinx.coroutines.flow.MutableStateFlow", import_statement)

save_target = """        sharedPrefs.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.name)
            putString("user_username", user.username)
            putString("user_role", user.role.name)
            putString("user_token", user.token)
            putString("user_db_name", user.db_name)
            apply()
        }"""

save_rep = """        sharedPrefs.edit().apply {
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
        }"""

content = content.replace(save_target, save_rep)

load_target = """            currentUser.value = AdminUser(id, name, username, role, token, dbName)
            return true
        }"""

load_rep = """            currentUser.value = AdminUser(id, name, username, role, token, dbName)
            dbName?.let {
                val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
                val topicName = "tenant_$safeTopic"
                FirebaseMessaging.getInstance().subscribeToTopic(topicName)
            }
            return true
        }"""

content = content.replace(load_target, load_rep)

clear_target = """        sharedPrefs.edit().clear().apply()
    }"""

clear_rep = """        val dbName = currentUser.value?.db_name
        dbName?.let {
            val safeTopic = it.replace(Regex("[^a-zA-Z0-9-_~]"), "")
            val topicName = "tenant_$safeTopic"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }
        sharedPrefs.edit().clear().apply()
    }"""

content = content.replace(clear_target, clear_rep)

with open("app/src/main/java/com/example/ui/data/AdminData.kt", "w") as f:
    f.write(content)
