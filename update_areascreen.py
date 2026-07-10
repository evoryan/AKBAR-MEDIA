import re
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace the onClick for Delete in AreaScreen
# The code currently has: onDelete = { areas.remove(area) }
# We want to change it to launch a coroutine, delete from API, then remove from list.

# But wait, onDelete is a lambda passed to AreaItem.
# Inside AreaScreen:
# onDelete = { areas.remove(area) }

target = "onDelete = { areas.remove(area) }"
replacement = """onDelete = { 
                            coroutineScope.launch {
                                try {
                                    ApiClient.apiService.deleteArea(area.id)
                                    areas.remove(area)
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        }"""
content = content.replace(target, replacement)

# We need to make sure coroutineScope is available.
if "val coroutineScope = rememberCoroutineScope()" not in content:
    # Find AreaScreen composable
    target2 = "val bgMain = Color(0xFF05050A)"
    replacement2 = "val coroutineScope = rememberCoroutineScope()\n    val bgMain = Color(0xFF05050A)"
    content = content.replace(target2, replacement2)

with open(filepath, 'w') as f:
    f.write(content)
