import re

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content = f.read()

target_pwd = """                            val res = ApiClient.apiService.acsAction(device.id, mapOf("action" to "set_password", "value" to newPass))"""
rep_pwd = """                            val res = ApiClient.apiService.acsAction(device.id, mapOf("action" to "set_password", "value" to newPass, "areaName" to device.areaName))"""
content = content.replace(target_pwd, rep_pwd)

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
    f.write(content)
