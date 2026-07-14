import re

with open("app/src/main/java/com/example/ui/data/AdminData.kt", "r") as f:
    content = f.read()

target = """    fun clearSession(context: Context) {
        currentUser.value = null
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val dbName = currentUser.value?.db_name"""

rep = """    fun clearSession(context: Context) {
        val dbName = currentUser.value?.db_name
        currentUser.value = null
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/data/AdminData.kt", "w") as f:
    f.write(content)
