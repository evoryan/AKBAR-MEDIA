import re

with open('app/src/main/java/com/example/ui/data/AdminData.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "data class AdminUser(val id: String, var name: String, var username: String, var role: UserRole)",
    "data class AdminUser(val id: String, var name: String, var username: String, var role: UserRole, val token: String? = null, val db_name: String? = null)"
)

content = content.replace(
    """            putString("user_role", user.role.name)
            apply()""",
    """            putString("user_role", user.role.name)
            putString("user_token", user.token)
            putString("user_db_name", user.db_name)
            apply()"""
)

content = content.replace(
    """        val roleString = sharedPrefs.getString("user_role", null)""",
    """        val roleString = sharedPrefs.getString("user_role", null)
        val token = sharedPrefs.getString("user_token", null)
        val dbName = sharedPrefs.getString("user_db_name", null)"""
)

content = content.replace(
    """            currentUser.value = AdminUser(id, name, username, role)""",
    """            currentUser.value = AdminUser(id, name, username, role, token, dbName)"""
)

with open('app/src/main/java/com/example/ui/data/AdminData.kt', 'w') as f:
    f.write(content)
